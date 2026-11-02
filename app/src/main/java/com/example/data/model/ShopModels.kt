package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val shippingAddress: String = "",
    val city: String = "",
    val preferredPayment: String = "Cash on Delivery",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Electronics",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val stock: Long = 0L,
    val imageUrl: String = "",
    val featured: Boolean = false,
    val ratingAvg: Double = 4.5,
    val reviewCount: Long = 0L,
    val brand: String = "",
    val sellerId: String = "",
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val discountPercentage: Int
        get() = if (originalPrice > price && originalPrice > 0) {
            (((originalPrice - price) / originalPrice) * 100).toInt()
        } else 0
}

@IgnoreExtraProperties
data class CartItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productTitle: String = "",
    val productImage: String = "",
    val category: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Long = 1L,
    val selectedSize: String = "Standard",
    val selectedColor: String = "Default",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val lineTotal: Double
        get() = unitPrice * quantity
}

@IgnoreExtraProperties
data class WishlistItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val productTitle: String = "",
    val productImage: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val brand: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val shippingAddress: String = "",
    val city: String = "",
    val paymentMethod: String = "Cash on Delivery",
    val paymentAccountRef: String = "",
    val paymentStatus: String = "Pending",
    val orderStatus: String = "Placed",
    val trackingNumber: String = "",
    val itemsSummary: String = "",
    val itemCount: Long = 1L,
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 250.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Review(
    @DocumentId val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    val verifiedPurchase: Boolean = true,
    @get:PropertyName("isPublic")
    @set:PropertyName("isPublic")
    var isPublic: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

object CatalogSeedData {
    val CATEGORIES = listOf("All", "Electronics", "Fashion", "Shoes", "Beauty", "Accessories")
    val PRODUCT_CATEGORIES = listOf("Electronics", "Fashion", "Shoes", "Beauty", "Accessories")
    val PAYMENT_METHODS = listOf("Cash on Delivery", "JazzCash", "EasyPaisa", "Bank Transfer", "Online Card")
    val ORDER_STATUS_STEPS = listOf("Placed", "Confirmed", "Packed", "Shipped", "Out for Delivery", "Delivered")

    fun defaultProducts(sellerId: String): List<Product> = listOf(
        Product(
            id = "seed_elec_1",
            title = "SonicPulse ANC Pro Wireless Headphones",
            description = "Studio-grade hybrid Active Noise Cancellation with 45-hour battery life, spatial audio head tracking, plush memory-foam ear cushions, and ultra-fast USB-C charging.",
            category = "Electronics",
            price = 18500.0,
            originalPrice = 24000.0,
            stock = 28L,
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80",
            featured = true,
            ratingAvg = 4.9,
            reviewCount = 42L,
            brand = "SonicPulse",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_elec_2",
            title = "ChronoApex Titanium AMOLED Smartwatch",
            description = "Aerospace-grade titanium bezel with 1.5-inch Sapphire LTPO AMOLED display, dual-band GPS, ECG & SpO2 health tracking, and 10-day endurance battery.",
            category = "Electronics",
            price = 26900.0,
            originalPrice = 32500.0,
            stock = 19L,
            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80",
            featured = true,
            ratingAvg = 4.8,
            reviewCount = 31L,
            brand = "ChronoApex",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_elec_3",
            title = "AeroBuds Ultra True Wireless Earbuds",
            description = "Hi-Res lossless wireless audio with dual dynamic drivers, 6-mic AI clear calls, IPX5 sweat resistance, and wireless charging case.",
            category = "Electronics",
            price = 8900.0,
            originalPrice = 11500.0,
            stock = 45L,
            imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=800&q=80",
            featured = false,
            ratingAvg = 4.7,
            reviewCount = 19L,
            brand = "SonicPulse",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_shoes_1",
            title = "Velocity Nitro Carbon Running Sneakers",
            description = "Responsive nitrogen-infused supercritical foam midsole with breathable engineered mesh upper and high-traction continental rubber outsole.",
            category = "Shoes",
            price = 14200.0,
            originalPrice = 18000.0,
            stock = 22L,
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80",
            featured = true,
            ratingAvg = 4.9,
            reviewCount = 54L,
            brand = "StrideLab",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_shoes_2",
            title = "Milan Handcrafted Suede Chelsea Boots",
            description = "Full-grain Italian water-repellent suede upper with Blake-stitched leather sole and cushioned orthopedic footbed for timeless formal & smart-casual wear.",
            category = "Shoes",
            price = 16800.0,
            originalPrice = 21000.0,
            stock = 14L,
            imageUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=800&q=80",
            featured = false,
            ratingAvg = 4.8,
            reviewCount = 23L,
            brand = "VeroModa Atelier",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_fash_1",
            title = "Artisan Embroidered Lawn Festive 3-Piece Suit",
            description = "Premium Egyptian cotton lawn shirt with intricate zari & resham embroidery, pure chiffon digital print dupatta, and dyed cambric trousers.",
            category = "Fashion",
            price = 11500.0,
            originalPrice = 14900.0,
            stock = 30L,
            imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80",
            featured = true,
            ratingAvg = 4.9,
            reviewCount = 38L,
            brand = "ZariHaus",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_fash_2",
            title = "Nordic Tailored Merino Wool Bomber Jacket",
            description = "Structured water-resistant wool-blend bomber silhouette with brushed satin lining, matte gunmetal YKK zippers, and ribbed cuffs.",
            category = "Fashion",
            price = 13900.0,
            originalPrice = 17500.0,
            stock = 18L,
            imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80",
            featured = false,
            ratingAvg = 4.7,
            reviewCount = 16L,
            brand = "Knit & Loom",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_beauty_1",
            title = "24K Gold & Hyaluronic Radiance Elixir Serum",
            description = "Concentrated botanical brightening serum infused with multi-molecular hyaluronic acid, niacinamide 10%, and peptides for glass-skin hydration.",
            category = "Beauty",
            price = 4850.0,
            originalPrice = 6200.0,
            stock = 50L,
            imageUrl = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=800&q=80",
            featured = true,
            ratingAvg = 4.8,
            reviewCount = 67L,
            brand = "Lumiere Botanics",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_beauty_2",
            title = "Oud Imperial Eau De Parfum 100ml",
            description = "Rich artisanal oriental fragrance blending Cambodian agarwood, Damascus rose, warm ambergris, and Madagascar vanilla bean.",
            category = "Beauty",
            price = 7900.0,
            originalPrice = 9800.0,
            stock = 35L,
            imageUrl = "https://images.unsplash.com/photo-1541643600914-78b084683601?auto=format&fit=crop&w=800&q=80",
            featured = false,
            ratingAvg = 4.9,
            reviewCount = 44L,
            brand = "Maison Oud",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_acc_1",
            title = "Heritage Full-Grain Leather Weekender Tote",
            description = "Vegetable-tanned top-grain cowhide leather tote with padded 16-inch laptop compartment, solid brass hardware, and detachable shoulder strap.",
            category = "Accessories",
            price = 12400.0,
            originalPrice = 15500.0,
            stock = 16L,
            imageUrl = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=800&q=80",
            featured = true,
            ratingAvg = 4.8,
            reviewCount = 29L,
            brand = "SaddleCraft",
            sellerId = sellerId,
            isActive = true
        ),
        Product(
            id = "seed_acc_2",
            title = "Polarized Tortoiseshell Acetate Sunglasses",
            description = "Hand-polished Italian Mazzucchelli acetate frames with UV400 anti-reflective polarized lenses and five-barrel stainless hinges.",
            category = "Accessories",
            price = 5600.0,
            originalPrice = 7500.0,
            stock = 40L,
            imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=800&q=80",
            featured = false,
            ratingAvg = 4.7,
            reviewCount = 21L,
            brand = "Solace Eyewear",
            sellerId = sellerId,
            isActive = true
        )
    )
}
