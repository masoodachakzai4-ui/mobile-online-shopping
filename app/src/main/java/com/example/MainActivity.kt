package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.Product
import com.example.data.repository.BazaarRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeCatalogScreen
import com.example.ui.screens.OrdersHistoryAndTrackingScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileAndSettingsScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.BazaarPKTheme
import com.example.ui.viewmodel.BazaarViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(false) }
            BazaarPKTheme(darkTheme = darkTheme) {
                AppNavigation(
                    darkThemeEnabled = darkTheme,
                    onToggleDarkTheme = { darkTheme = it }
                )
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

enum class MainDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    WISHLIST("Wishlist", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_wishlist"),
    CART("Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_cart"),
    ORDERS("Orders", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping, "nav_orders"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
}

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth,
    darkThemeEnabled: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {}
) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        BazaarMainWorkspace(
            currentUserId = user.uid,
            currentUserName = user.displayName ?: "BazaarPK Shopper",
            currentUserEmail = user.email ?: "shopper@bazaarpk.com",
            darkThemeEnabled = darkThemeEnabled,
            onToggleDarkTheme = onToggleDarkTheme
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazaarMainWorkspace(
    currentUserId: String,
    currentUserName: String,
    currentUserEmail: String,
    darkThemeEnabled: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val viewModel: BazaarViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                BazaarViewModel(
                    repository = BazaarRepository(db),
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    currentUserEmail = currentUserEmail
                )
            }
        }
    )

    val productsState by viewModel.productsState.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val cartState by viewModel.cartState.collectAsStateWithLifecycle()
    val wishlistState by viewModel.wishlistState.collectAsStateWithLifecycle()
    val ordersState by viewModel.ordersState.collectAsStateWithLifecycle()
    val reviewsState by viewModel.reviewsState.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val onlyInStock by viewModel.onlyInStock.collectAsStateWithLifecycle()
    val maxPriceFilter by viewModel.maxPriceFilter.collectAsStateWithLifecycle()
    val promoCode by viewModel.promoCode.collectAsStateWithLifecycle()
    val discountRate by viewModel.appliedPromoDiscountRate.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    var currentTab by rememberSaveable { mutableStateOf(MainDestination.HOME) }
    var selectedProductId by rememberSaveable { mutableStateOf<String?>(null) }
    var isCheckoutOpen by rememberSaveable { mutableStateOf(false) }
    var isAdminDashboardOpen by rememberSaveable { mutableStateOf(false) }

    val allProducts = (productsState as? UiState.Success)?.data ?: emptyList()
    val cartItems = (cartState as? UiState.Success)?.data ?: emptyList()
    val wishlistItems = (wishlistState as? UiState.Success)?.data ?: emptyList()
    val ordersList = (ordersState as? UiState.Success)?.data ?: emptyList()
    val allReviews = (reviewsState as? UiState.Success)?.data ?: emptyList()
    val userProfile = (profileState as? UiState.Success)?.data

    val selectedProduct: Product? = remember(selectedProductId, allProducts) {
        allProducts.find { it.id == selectedProductId }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (selectedProduct == null && !isCheckoutOpen && !isAdminDashboardOpen) {
                    TopBrandHeader(
                        onOpenAdmin = { isAdminDashboardOpen = true }
                    )
                }
            },
            bottomBar = {
                if (!isWideScreen && selectedProduct == null && !isCheckoutOpen && !isAdminDashboardOpen) {
                    NavigationBar {
                        MainDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            val badgeCount = when (dest) {
                                MainDestination.CART -> cartItems.sumOf { it.quantity }.toInt()
                                MainDestination.WISHLIST -> wishlistItems.size
                                else -> 0
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = dest },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (badgeCount > 0) {
                                                Badge { Text(badgeCount.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    }
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen && selectedProduct == null && !isCheckoutOpen && !isAdminDashboardOpen) {
                    NavigationRail {
                        MainDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            val badgeCount = when (dest) {
                                MainDestination.CART -> cartItems.sumOf { it.quantity }.toInt()
                                MainDestination.WISHLIST -> wishlistItems.size
                                else -> 0
                            }
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = dest },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (badgeCount > 0) {
                                                Badge { Text(badgeCount.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    }
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isAdminDashboardOpen -> {
                            AdminDashboardScreen(
                                products = allProducts,
                                orders = ordersList,
                                onBack = { isAdminDashboardOpen = false },
                                onSaveProduct = { existing, title, desc, cat, price, origPrice, stock, img, featured, brand ->
                                    viewModel.adminSaveProduct(existing, title, desc, cat, price, origPrice, stock, img, featured, brand)
                                },
                                onQuickAdjustStockPrice = { prod, newStock, newPrice ->
                                    viewModel.adminQuickAdjustStockAndPrice(prod, newStock, newPrice)
                                },
                                onDeleteProduct = { id ->
                                    viewModel.adminDeleteProduct(id)
                                },
                                onUpdateOrderStatus = { ord, status, payStatus ->
                                    viewModel.adminUpdateOrderStatus(ord, status, payStatus)
                                },
                                onRestoreSampleCatalog = {
                                    viewModel.adminRestoreDefaultCatalog()
                                }
                            )
                        }

                        isCheckoutOpen -> {
                            CheckoutScreen(
                                cartItems = cartItems,
                                userProfile = userProfile,
                                discountRate = discountRate,
                                isBusy = isBusy,
                                onBack = { isCheckoutOpen = false },
                                onConfirmOrder = { name, phone, addr, city, paymentMethod, paymentRef ->
                                    viewModel.placeOrder(
                                        customerName = name,
                                        customerPhone = phone,
                                        shippingAddress = addr,
                                        city = city,
                                        paymentMethod = paymentMethod,
                                        paymentAccountRef = paymentRef,
                                        onOrderSuccess = {
                                            isCheckoutOpen = false
                                            currentTab = MainDestination.ORDERS
                                        }
                                    )
                                }
                            )
                        }

                        selectedProduct != null -> {
                            val prodReviews = allReviews.filter { it.productId == selectedProduct.id }
                            val isWishlisted = wishlistItems.any { it.productId == selectedProduct.id }
                            ProductDetailScreen(
                                product = selectedProduct,
                                isWishlisted = isWishlisted,
                                productReviews = prodReviews,
                                onBack = { selectedProductId = null },
                                onToggleWishlist = { viewModel.toggleWishlist(selectedProduct) },
                                onAddToCart = { qty, size, color ->
                                    viewModel.addToCart(selectedProduct, qty, size, color)
                                },
                                onBuyNow = { qty, size, color ->
                                    viewModel.addToCart(selectedProduct, qty, size, color)
                                    selectedProductId = null
                                    currentTab = MainDestination.CART
                                },
                                onSubmitReview = { rating, comment ->
                                    viewModel.submitReview(selectedProduct, rating, comment)
                                }
                            )
                        }

                        else -> {
                            when (currentTab) {
                                MainDestination.HOME -> {
                                    HomeCatalogScreen(
                                        productsState = productsState,
                                        filteredProducts = filteredProducts,
                                        wishlistItems = wishlistItems,
                                        searchQuery = searchQuery,
                                        selectedCategory = selectedCategory,
                                        selectedSort = selectedSort,
                                        onlyInStock = onlyInStock,
                                        maxPriceFilter = maxPriceFilter,
                                        onSearchQueryChange = viewModel::updateSearchQuery,
                                        onSelectCategory = viewModel::selectCategory,
                                        onSelectSort = viewModel::selectSort,
                                        onToggleOnlyInStock = viewModel::toggleOnlyInStock,
                                        onMaxPriceChange = viewModel::updateMaxPriceFilter,
                                        onProductClick = { selectedProductId = it.id },
                                        onToggleWishlist = viewModel::toggleWishlist,
                                        onAddToCart = { viewModel.addToCart(it) },
                                        onApplyPromoBanner = {
                                            viewModel.updatePromoCode("PK20")
                                            viewModel.applyPromoCode()
                                        }
                                    )
                                }

                                MainDestination.WISHLIST -> {
                                    WishlistScreen(
                                        wishlistState = wishlistState,
                                        allProducts = allProducts,
                                        onProductClick = { selectedProductId = it.id },
                                        onRemoveWishlist = viewModel::toggleWishlist,
                                        onMoveToCart = { prod ->
                                            viewModel.addToCart(prod)
                                        },
                                        onExploreCatalog = { currentTab = MainDestination.HOME }
                                    )
                                }

                                MainDestination.CART -> {
                                    CartScreen(
                                        cartState = cartState,
                                        promoCode = promoCode,
                                        discountRate = discountRate,
                                        onPromoCodeChange = viewModel::updatePromoCode,
                                        onApplyPromo = viewModel::applyPromoCode,
                                        onUpdateQuantity = viewModel::updateCartItemQuantity,
                                        onRemoveItem = viewModel::removeCartItem,
                                        onProceedToCheckout = { isCheckoutOpen = true },
                                        onExploreProducts = { currentTab = MainDestination.HOME }
                                    )
                                }

                                MainDestination.ORDERS -> {
                                    OrdersHistoryAndTrackingScreen(
                                        ordersState = ordersState,
                                        onExploreCatalog = { currentTab = MainDestination.HOME }
                                    )
                                }

                                MainDestination.PROFILE -> {
                                    ProfileAndSettingsScreen(
                                        profileState = profileState,
                                        fallbackName = currentUserName,
                                        fallbackEmail = currentUserEmail,
                                        darkThemeEnabled = darkThemeEnabled,
                                        onToggleDarkTheme = onToggleDarkTheme,
                                        onSaveProfile = viewModel::saveProfile,
                                        onOpenAdminDashboard = { isAdminDashboardOpen = true },
                                        onSignOut = {
                                            signOutUser(
                                                context = context,
                                                onSignOutComplete = {},
                                                scope = coroutineScope
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBrandHeader(
    onOpenAdmin: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE03E1A), Color(0xFFF59E0B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BazaarPK",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Express Delivery • JazzCash & EasyPaisa",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalIconButton(
                onClick = onOpenAdmin,
                modifier = Modifier.testTag("admin_dashboard_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Open Admin Console"
                )
            }
        }
    }
}
