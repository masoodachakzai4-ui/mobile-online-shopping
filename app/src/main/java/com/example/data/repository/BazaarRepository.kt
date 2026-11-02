package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CartItem
import com.example.data.model.CatalogSeedData
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.UserProfile
import com.example.data.model.WishlistItem
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BazaarRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. PRODUCTS CATALOG & ADMIN PRODUCT MANAGEMENT ---

    fun observeActiveProducts(): Flow<List<Product>> = flow {
        val path = "products"
        emitAll(
            db.collection("products")
                .whereEqualTo("isActive", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getActiveProducts(): Result<List<Product>> = runCatching {
        val path = "products"
        try {
            val snap = db.collection("products")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            snap.documents.mapNotNull {
                it.toObject(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun seedCatalogIfEmpty(): Result<Int> = runCatching {
        val uid = requireUserId()
        val existing = getActiveProducts().getOrDefault(emptyList())
        if (existing.isNotEmpty()) return@runCatching 0

        val defaults = CatalogSeedData.defaultProducts(uid)
        for (prod in defaults) {
            createProduct(
                title = prod.title,
                description = prod.description,
                category = prod.category,
                price = prod.price,
                originalPrice = prod.originalPrice,
                stock = prod.stock,
                imageUrl = prod.imageUrl,
                featured = prod.featured,
                brand = prod.brand,
                ratingAvg = prod.ratingAvg,
                reviewCount = prod.reviewCount,
                customId = prod.id
            ).getOrThrow()
        }
        defaults.size
    }

    suspend fun createProduct(
        title: String,
        description: String,
        category: String,
        price: Double,
        originalPrice: Double,
        stock: Long,
        imageUrl: String,
        featured: Boolean,
        brand: String,
        ratingAvg: Double = 4.8,
        reviewCount: Long = 1L,
        customId: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = customId ?: "prod_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("products").document(docId)
        val payload = mapOf(
            "title" to title.trim(),
            "description" to description.trim(),
            "category" to category,
            "price" to price,
            "originalPrice" to originalPrice.coerceAtLeast(price),
            "stock" to stock.coerceAtLeast(0L),
            "imageUrl" to imageUrl.trim(),
            "featured" to featured,
            "ratingAvg" to ratingAvg,
            "reviewCount" to reviewCount,
            "brand" to brand.trim().ifEmpty { "BazaarPK" },
            "sellerId" to uid,
            "isActive" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        requireUserId()
        require(product.id.isNotBlank()) { "Product ID must not be blank" }
        val docRef = db.collection("products").document(product.id)
        val updates = mapOf(
            "title" to product.title.trim(),
            "description" to product.description.trim(),
            "category" to product.category,
            "price" to product.price,
            "originalPrice" to product.originalPrice.coerceAtLeast(product.price),
            "stock" to product.stock.coerceAtLeast(0L),
            "imageUrl" to product.imageUrl.trim(),
            "featured" to product.featured,
            "brand" to product.brand.trim().ifEmpty { "BazaarPK" },
            "isActive" to product.isActive,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("products").document(productId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 2. USER PROFILE & SETTINGS ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile?> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun saveUserProfile(
        displayName: String,
        email: String,
        phone: String,
        shippingAddress: String,
        city: String,
        preferredPayment: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val updates = mapOf(
                    "displayName" to displayName.trim().ifEmpty { "Shopper" },
                    "email" to email.trim().ifEmpty { "user@bazaarpk.com" },
                    "phone" to phone.trim(),
                    "shippingAddress" to shippingAddress.trim(),
                    "city" to city.trim(),
                    "preferredPayment" to preferredPayment,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
            } else {
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to displayName.trim().ifEmpty { "Shopper" },
                    "email" to email.trim().ifEmpty { "user@bazaarpk.com" },
                    "phone" to phone.trim(),
                    "shippingAddress" to shippingAddress.trim(),
                    "city" to city.trim(),
                    "preferredPayment" to preferredPayment,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- 3. SHOPPING CART ---

    fun observeCart(userId: String): Flow<List<CartItem>> = flow {
        val path = "users/$userId/cart"
        emitAll(
            db.collection("users").document(userId).collection("cart")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull {
                        it.toObject(CartItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addToCart(
        product: Product,
        quantity: Long = 1L,
        selectedSize: String = "Standard",
        selectedColor: String = "Default"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val cartItemId = "${product.id}_${selectedSize.replace(" ", "")}_${selectedColor.replace(" ", "")}"
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "")
            .take(100)
        val docRef = db.collection("users").document(uid).collection("cart").document(cartItemId)
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val currentQty = existing.getLong("quantity") ?: 1L
                val newQty = (currentQty + quantity).coerceIn(1L, 99L)
                docRef.update(
                    mapOf(
                        "quantity" to newQty,
                        "unitPrice" to product.price,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "productId" to product.id,
                    "productTitle" to product.title,
                    "productImage" to product.imageUrl,
                    "category" to product.category,
                    "unitPrice" to product.price,
                    "quantity" to quantity.coerceIn(1L, 99L),
                    "selectedSize" to selectedSize,
                    "selectedColor" to selectedColor,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
            cartItemId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateCartQuantity(cartItemId: String, newQuantity: Long): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("cart").document(cartItemId)
        try {
            if (newQuantity <= 0L) {
                docRef.delete().await()
            } else {
                docRef.update(
                    mapOf(
                        "quantity" to newQuantity.coerceIn(1L, 99L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun removeFromCart(cartItemId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("cart").document(cartItemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 4. WISHLIST ---

    fun observeWishlist(userId: String): Flow<List<WishlistItem>> = flow {
        val path = "users/$userId/wishlist"
        emitAll(
            db.collection("users").document(userId).collection("wishlist")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull {
                        it.toObject(WishlistItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun toggleWishlist(product: Product, currentlyWishlisted: Boolean): Result<Boolean> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("wishlist").document(product.id)
        try {
            if (currentlyWishlisted) {
                docRef.delete().await()
                false
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "productId" to product.id,
                    "productTitle" to product.title,
                    "productImage" to product.imageUrl,
                    "category" to product.category,
                    "price" to product.price,
                    "originalPrice" to product.originalPrice,
                    "brand" to product.brand,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- 5. ORDERS & CHECKOUT ---

    fun observeUserOrders(userId: String): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            db.collection("orders")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull {
                        it.toObject(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getOrderById(orderId: String): Result<Order> = runCatching {
        val docRef = db.collection("orders").document(orderId)
        try {
            val snap = docRef.get().await()
            checkNotNull(
                snap.toObject(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ) { "Order not found" }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        shippingAddress: String,
        city: String,
        paymentMethod: String,
        paymentAccountRef: String,
        cartItems: List<CartItem>,
        discountAmount: Double = 0.0
    ): Result<String> = runCatching {
        val uid = requireUserId()
        require(cartItems.isNotEmpty()) { "Cart cannot be empty" }

        val orderId = "ord_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val trackingNumber = "BZP-${(100000..999999).random()}"
        val subtotal = cartItems.sumOf { it.lineTotal }
        val deliveryFee = if (subtotal >= 15000.0) 0.0 else 250.0
        val totalAmount = (subtotal + deliveryFee - discountAmount).coerceAtLeast(1.0)
        val totalUnits = cartItems.sumOf { it.quantity }
        val summary = cartItems.joinToString(", ") { "${it.quantity}x ${it.productTitle}" }.take(950)
        val paymentStatus = if (paymentMethod == "Cash on Delivery") "Pending" else "Paid"

        val orderRef = db.collection("orders").document(orderId)
        val payload = mapOf(
            "userId" to uid,
            "customerName" to customerName.trim(),
            "customerPhone" to customerPhone.trim(),
            "shippingAddress" to shippingAddress.trim(),
            "city" to city.trim(),
            "paymentMethod" to paymentMethod,
            "paymentAccountRef" to paymentAccountRef.trim(),
            "paymentStatus" to paymentStatus,
            "orderStatus" to "Placed",
            "trackingNumber" to trackingNumber,
            "itemsSummary" to summary,
            "itemCount" to totalUnits,
            "subtotal" to subtotal,
            "deliveryFee" to deliveryFee,
            "discountAmount" to discountAmount,
            "totalAmount" to totalAmount,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            orderRef.set(payload).await()
            // Clear cart items after successful order placement
            for (item in cartItems) {
                if (item.id.isNotBlank()) {
                    db.collection("users").document(uid).collection("cart").document(item.id).delete().await()
                }
            }
            orderId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, orderRef.path)
            throw e
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        newOrderStatus: String,
        newPaymentStatus: String
    ): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "orderStatus" to newOrderStatus,
                    "paymentStatus" to newPaymentStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 6. CUSTOMER REVIEWS & RATINGS ---

    fun observePublicReviews(): Flow<List<Review>> = flow {
        val path = "reviews"
        emitAll(
            db.collection("reviews")
                .whereEqualTo("isPublic", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull {
                        it.toObject(Review::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addReview(
        product: Product,
        userName: String,
        rating: Long,
        comment: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val reviewId = "rev_${UUID.randomUUID().toString().replace("-", "").take(14)}"
        val reviewRef = db.collection("reviews").document(reviewId)
        val payload = mapOf(
            "productId" to product.id,
            "userId" to uid,
            "userName" to userName.trim().ifEmpty { "Verified Buyer" },
            "rating" to rating.coerceIn(1L, 5L),
            "comment" to comment.trim(),
            "verifiedPurchase" to true,
            "isPublic" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            reviewRef.set(payload).await()
            // Update product aggregate rating
            val newCount = product.reviewCount + 1L
            val newAvg = ((product.ratingAvg * product.reviewCount) + rating) / newCount.toDouble()
            val roundedAvg = (Math.round(newAvg * 10.0) / 10.0).coerceIn(1.0, 5.0)
            db.collection("products").document(product.id).update(
                mapOf(
                    "ratingAvg" to roundedAvg,
                    "reviewCount" to newCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            reviewId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reviewRef.path)
            throw e
        }
    }
}
