package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.UserProfile
import com.example.data.model.WishlistItem
import com.example.data.repository.BazaarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class SortOption(val label: String) {
    FEATURED("Featured"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    TOP_RATED("Highest Rated")
}

class BazaarViewModel(
    private val repository: BazaarRepository,
    val currentUserId: String,
    val currentUserName: String,
    val currentUserEmail: String
) : ViewModel() {

    // Filter & Search States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.FEATURED)
    val selectedSort: StateFlow<SortOption> = _selectedSort.asStateFlow()

    private val _onlyInStock = MutableStateFlow(false)
    val onlyInStock: StateFlow<Boolean> = _onlyInStock.asStateFlow()

    private val _maxPriceFilter = MutableStateFlow(50000f)
    val maxPriceFilter: StateFlow<Float> = _maxPriceFilter.asStateFlow()

    // Transient UI Banner / Feedback Message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _promoCode = MutableStateFlow("")
    val promoCode: StateFlow<String> = _promoCode.asStateFlow()

    private val _appliedPromoDiscountRate = MutableStateFlow(0.0)
    val appliedPromoDiscountRate: StateFlow<Double> = _appliedPromoDiscountRate.asStateFlow()

    // Two-tier Kotlin Flow Error Handling Streams
    val productsState: StateFlow<UiState<List<Product>>> = repository.observeActiveProducts()
        .map<List<Product>, UiState<List<Product>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing products", error)
            emit(UiState.Error(error.message ?: "Failed to load products"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val filteredProducts: StateFlow<List<Product>> = combine(
        productsState,
        _searchQuery,
        _selectedCategory,
        _selectedSort,
        combine(_onlyInStock, _maxPriceFilter) { inStock, maxPrice -> inStock to maxPrice }
    ) { state, query, category, sort, (inStock, maxPrice) ->
        val all = (state as? UiState.Success)?.data ?: emptyList()
        all.filter { prod ->
            val matchesCategory = category == "All" || prod.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                prod.title.contains(query, ignoreCase = true) ||
                prod.brand.contains(query, ignoreCase = true) ||
                prod.description.contains(query, ignoreCase = true) ||
                prod.category.contains(query, ignoreCase = true)
            val matchesStock = !inStock || prod.stock > 0L
            val matchesPrice = prod.price <= maxPrice.toDouble()
            matchesCategory && matchesQuery && matchesStock && matchesPrice
        }.let { list ->
            when (sort) {
                SortOption.FEATURED -> list.sortedByDescending { it.featured }
                SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
                SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
                SortOption.TOP_RATED -> list.sortedByDescending { it.ratingAvg }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    val cartState: StateFlow<UiState<List<CartItem>>> = repository.observeCart(currentUserId)
        .map<List<CartItem>, UiState<List<CartItem>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing cart", error)
            emit(UiState.Error(error.message ?: "Failed to load cart"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val wishlistState: StateFlow<UiState<List<WishlistItem>>> = repository.observeWishlist(currentUserId)
        .map<List<WishlistItem>, UiState<List<WishlistItem>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing wishlist", error)
            emit(UiState.Error(error.message ?: "Failed to load wishlist"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val ordersState: StateFlow<UiState<List<Order>>> = repository.observeUserOrders(currentUserId)
        .map<List<Order>, UiState<List<Order>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing orders", error)
            emit(UiState.Error(error.message ?: "Failed to load orders"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val reviewsState: StateFlow<UiState<List<Review>>> = repository.observePublicReviews()
        .map<List<Review>, UiState<List<Review>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing reviews", error)
            emit(UiState.Error(error.message ?: "Failed to load reviews"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val profileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing profile", error)
            emit(UiState.Error(error.message ?: "Failed to load profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    init {
        initializeStoreAndProfile()
    }

    private fun initializeStoreAndProfile() {
        viewModelScope.launch {
            repository.seedCatalogIfEmpty()
            val profile = repository.getUserProfile(currentUserId).getOrNull()
            if (profile == null) {
                repository.saveUserProfile(
                    displayName = currentUserName.ifBlank { "BazaarPK Shopper" },
                    email = currentUserEmail.ifBlank { "shopper@bazaarpk.com" },
                    phone = "0300-1234567",
                    shippingAddress = "House 24, Main Boulevard, Gulberg III",
                    city = "Lahore",
                    preferredPayment = "JazzCash"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectSort(sortOption: SortOption) {
        _selectedSort.value = sortOption
    }

    fun toggleOnlyInStock(enabled: Boolean) {
        _onlyInStock.value = enabled
    }

    fun updateMaxPriceFilter(maxPrice: Float) {
        _maxPriceFilter.value = maxPrice
    }

    fun updatePromoCode(code: String) {
        _promoCode.value = code
    }

    fun applyPromoCode() {
        val normalized = _promoCode.value.trim().uppercase()
        when (normalized) {
            "BAZAAR10" -> {
                _appliedPromoDiscountRate.value = 0.10
                showMessage("Promo BAZAAR10 applied: 10% OFF!")
            }
            "PK20", "EID20" -> {
                _appliedPromoDiscountRate.value = 0.20
                showMessage("Promo $normalized applied: 20% OFF!")
            }
            else -> {
                _appliedPromoDiscountRate.value = 0.0
                showMessage("Invalid promo code. Try BAZAAR10 or PK20.")
            }
        }
    }

    fun clearMessage() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun addToCart(
        product: Product,
        quantity: Long = 1L,
        selectedSize: String = "Standard",
        selectedColor: String = "Default"
    ) {
        viewModelScope.launch {
            val res = repository.addToCart(product, quantity, selectedSize, selectedColor)
            if (res.isSuccess) {
                showMessage("Added ${product.title} to Cart")
            } else {
                showMessage(res.exceptionOrNull()?.localizedMessage ?: "Could not add to cart")
            }
        }
    }

    fun updateCartItemQuantity(cartItemId: String, newQuantity: Long) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, newQuantity)
        }
    }

    fun removeCartItem(cartItemId: String) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
            showMessage("Removed item from cart")
        }
    }

    fun toggleWishlist(product: Product) {
        val currentWishlist = (wishlistState.value as? UiState.Success)?.data ?: emptyList()
        val isSaved = currentWishlist.any { it.productId == product.id }
        viewModelScope.launch {
            val res = repository.toggleWishlist(product, isSaved)
            if (res.isSuccess) {
                val added = res.getOrDefault(false)
                showMessage(if (added) "Saved to Wishlist" else "Removed from Wishlist")
            }
        }
    }

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        shippingAddress: String,
        city: String,
        paymentMethod: String,
        paymentAccountRef: String,
        onOrderSuccess: (String) -> Unit
    ) {
        val items = (cartState.value as? UiState.Success)?.data ?: emptyList()
        if (items.isEmpty()) {
            showMessage("Your cart is empty")
            return
        }
        val subtotal = items.sumOf { it.lineTotal }
        val discount = subtotal * _appliedPromoDiscountRate.value

        _isBusy.value = true
        viewModelScope.launch {
            val res = repository.placeOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                shippingAddress = shippingAddress,
                city = city,
                paymentMethod = paymentMethod,
                paymentAccountRef = paymentAccountRef,
                cartItems = items,
                discountAmount = discount
            )
            _isBusy.value = false
            if (res.isSuccess) {
                _appliedPromoDiscountRate.value = 0.0
                _promoCode.value = ""
                val orderId = res.getOrThrow()
                showMessage("Order placed successfully!")
                onOrderSuccess(orderId)
            } else {
                showMessage(res.exceptionOrNull()?.localizedMessage ?: "Failed to place order")
            }
        }
    }

    fun submitReview(product: Product, rating: Long, comment: String) {
        if (comment.trim().length < 2) {
            showMessage("Please write a brief review comment.")
            return
        }
        val profile = (profileState.value as? UiState.Success)?.data
        val authorName = profile?.displayName?.ifBlank { currentUserName } ?: currentUserName.ifBlank { "Verified Shopper" }

        viewModelScope.launch {
            val res = repository.addReview(
                product = product,
                userName = authorName,
                rating = rating,
                comment = comment
            )
            if (res.isSuccess) {
                showMessage("Thank you! Your review has been published.")
            } else {
                showMessage(res.exceptionOrNull()?.localizedMessage ?: "Failed to submit review")
            }
        }
    }

    fun saveProfile(
        displayName: String,
        email: String,
        phone: String,
        shippingAddress: String,
        city: String,
        preferredPayment: String
    ) {
        _isBusy.value = true
        viewModelScope.launch {
            val res = repository.saveUserProfile(
                displayName = displayName,
                email = email,
                phone = phone,
                shippingAddress = shippingAddress,
                city = city,
                preferredPayment = preferredPayment
            )
            _isBusy.value = false
            if (res.isSuccess) {
                showMessage("Profile & checkout settings saved!")
            } else {
                showMessage(res.exceptionOrNull()?.localizedMessage ?: "Failed to update profile")
            }
        }
    }

    // --- ADMIN DASHBOARD OPERATIONS ---

    fun adminSaveProduct(
        existingProduct: Product?,
        title: String,
        description: String,
        category: String,
        price: Double,
        originalPrice: Double,
        stock: Long,
        imageUrl: String,
        featured: Boolean,
        brand: String
    ) {
        _isBusy.value = true
        viewModelScope.launch {
            val res = if (existingProduct == null) {
                repository.createProduct(
                    title = title,
                    description = description,
                    category = category,
                    price = price,
                    originalPrice = originalPrice,
                    stock = stock,
                    imageUrl = imageUrl,
                    featured = featured,
                    brand = brand
                ).map { }
            } else {
                repository.updateProduct(
                    existingProduct.copy(
                        title = title,
                        description = description,
                        category = category,
                        price = price,
                        originalPrice = originalPrice,
                        stock = stock,
                        imageUrl = imageUrl,
                        featured = featured,
                        brand = brand
                    )
                )
            }
            _isBusy.value = false
            if (res.isSuccess) {
                showMessage(if (existingProduct == null) "Product added to catalog!" else "Product updated!")
            } else {
                showMessage(res.exceptionOrNull()?.localizedMessage ?: "Failed to save product")
            }
        }
    }

    fun adminQuickAdjustStockAndPrice(product: Product, newStock: Long, newPrice: Double) {
        viewModelScope.launch {
            val res = repository.updateProduct(
                product.copy(
                    stock = newStock.coerceAtLeast(0L),
                    price = newPrice.coerceAtLeast(1.0)
                )
            )
            if (res.isSuccess) {
                showMessage("Updated ${product.title} stock & price")
            } else {
                showMessage(res.exceptionOrNull()?.localizedMessage ?: "Update failed")
            }
        }
    }

    fun adminDeleteProduct(productId: String) {
        viewModelScope.launch {
            val res = repository.deleteProduct(productId)
            if (res.isSuccess) {
                showMessage("Product deleted from catalog")
            } else {
                showMessage(res.exceptionOrNull()?.localizedMessage ?: "Could not delete product")
            }
        }
    }

    fun adminUpdateOrderStatus(order: Order, nextStatus: String, nextPaymentStatus: String) {
        viewModelScope.launch {
            val res = repository.updateOrderStatus(
                orderId = order.id,
                newOrderStatus = nextStatus,
                newPaymentStatus = nextPaymentStatus
            )
            if (res.isSuccess) {
                showMessage("Order #${order.trackingNumber} updated to $nextStatus")
            } else {
                showMessage(res.exceptionOrNull()?.localizedMessage ?: "Could not update order status")
            }
        }
    }

    fun adminRestoreDefaultCatalog() {
        _isBusy.value = true
        viewModelScope.launch {
            val res = repository.seedCatalogIfEmpty()
            _isBusy.value = false
            val count = res.getOrDefault(0)
            if (count > 0) {
                showMessage("Restored $count sample products!")
            } else {
                showMessage("Catalog already has active products.")
            }
        }
    }

    private companion object {
        const val TAG = "BazaarVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
