package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogSeedData
import com.example.data.model.Product
import com.example.data.model.WishlistItem
import com.example.ui.components.ProductGridCard
import com.example.ui.components.ProductVisualImage
import com.example.ui.components.categoryIcon
import com.example.ui.components.formatPkr
import com.example.ui.viewmodel.SortOption
import com.example.ui.viewmodel.UiState

@Composable
fun HomeCatalogScreen(
    productsState: UiState<List<Product>>,
    filteredProducts: List<Product>,
    wishlistItems: List<WishlistItem>,
    searchQuery: String,
    selectedCategory: String,
    selectedSort: SortOption,
    onlyInStock: Boolean,
    maxPriceFilter: Float,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onToggleOnlyInStock: (Boolean) -> Unit,
    onMaxPriceChange: (Float) -> Unit,
    onProductClick: (Product) -> Unit,
    onToggleWishlist: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onApplyPromoBanner: () -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }
    val wishlistedIds = remember(wishlistItems) { wishlistItems.map { it.productId }.toSet() }
    val allProducts = (productsState as? UiState.Success)?.data ?: emptyList()
    val featuredProducts = remember(allProducts) { allProducts.filter { it.featured } }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 164.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_catalog_grid")
    ) {
        // 1. Search & Filter Bar
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search electronics, shoes, brands...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search products"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_input")
                    )

                    Surface(
                        onClick = { showFilters = !showFilters },
                        shape = RoundedCornerShape(16.dp),
                        color = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(56.dp)
                            .testTag("filter_toggle_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Toggle filters",
                                tint = if (showFilters) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Expandable Filter & Sort Panel
                AnimatedVisibility(visible = showFilters) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Sort & Filter Products",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "In Stock Only",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = onlyInStock,
                                        onCheckedChange = onToggleOnlyInStock
                                    )
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(SortOption.entries) { option ->
                                    FilterChip(
                                        selected = selectedSort == option,
                                        onClick = { onSelectSort(option) },
                                        label = { Text(option.label) }
                                    )
                                }
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Max Price Cap",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = formatPkr(maxPriceFilter.toDouble()),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Slider(
                                    value = maxPriceFilter,
                                    onValueChange = onMaxPriceChange,
                                    valueRange = 2000f..50000f,
                                    steps = 15
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Hero Promotional Banner
        item(span = { GridItemSpan(maxLineSpan) }) {
            HeroPromoBanner(onApplyPromo = onApplyPromoBanner)
        }

        // 3. Category Pills Row
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Shop by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("category_row")
                ) {
                    items(CatalogSeedData.CATEGORIES) { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCategory(category) },
                            label = {
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(category),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("category_chip_${category.lowercase()}")
                        )
                    }
                }
            }
        }

        // 4. Featured Products Horizontal Spotlight (when viewing All and no search query)
        if (selectedCategory == "All" && searchQuery.isBlank() && featuredProducts.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Featured Flash Deals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${featuredProducts.size} Curated Picks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featuredProducts, key = { "feat_${it.id}" }) { product ->
                            FeaturedSpotlightCard(
                                product = product,
                                onClick = { onProductClick(product) }
                            )
                        }
                    }
                }
            }
        }

        // 5. Catalog Section Header
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory == "All") "All Products" else "$selectedCategory Collection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredProducts.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 6. Loading / Error / Empty / Product Grid States
        when (productsState) {
            is UiState.Loading -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = productsState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No matching products found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Try clearing your search query or adjusting the price cap.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductGridCard(
                            product = product,
                            isWishlisted = wishlistedIds.contains(product.id),
                            onProductClick = { onProductClick(product) },
                            onToggleWishlist = { onToggleWishlist(product) },
                            onAddToCart = { onAddToCart(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPromoBanner(onApplyPromo: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF7C2D12),
                        Color(0xFFE03E1A)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.16f),
                radius = size.minDimension * 0.55f,
                center = Offset(size.width * 0.9f, size.height * 0.2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Surface(
                color = Color(0xFFFBBF24),
                shape = CircleShape
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = Color(0xFF451A03),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GRAND SEASON SALE • CODE: PK20",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF451A03),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Up to 35% OFF Flagship Tech & Luxury Fashion",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pay seamlessly with JazzCash, EasyPaisa, Bank Transfer, or Cash on Delivery. Free shipping over Rs. 15,000!",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onApplyPromo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("claim_promo_banner_btn")
            ) {
                Text(
                    text = "Claim 20% Promo (PK20)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeaturedSpotlightCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductVisualImage(
                imageUrl = product.imageUrl,
                category = product.category,
                contentDescription = product.title,
                iconSize = 28.dp,
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatPkr(product.price),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WishlistScreen(
    wishlistState: UiState<List<WishlistItem>>,
    allProducts: List<Product>,
    onProductClick: (Product) -> Unit,
    onRemoveWishlist: (Product) -> Unit,
    onMoveToCart: (Product) -> Unit,
    onExploreCatalog: () -> Unit
) {
    val wishlistItems = (wishlistState as? UiState.Success)?.data ?: emptyList()

    if (wishlistItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Text(
                    text = "Your Wishlist is Empty",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap the heart icon on any item to save your favorite electronics, fashion, or shoes for later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onExploreCatalog,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Explore Products")
                }
            }
        }
    } else {
        val matchedProducts = wishlistItems.map { wish ->
            allProducts.find { it.id == wish.productId } ?: Product(
                id = wish.productId,
                title = wish.productTitle,
                description = "Saved in your wishlist",
                category = wish.category,
                price = wish.price,
                originalPrice = wish.originalPrice,
                stock = 10L,
                imageUrl = wish.productImage,
                brand = wish.brand
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 164.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Saved Wishlist (${matchedProducts.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(matchedProducts, key = { it.id }) { prod ->
                ProductGridCard(
                    product = prod,
                    isWishlisted = true,
                    onProductClick = { onProductClick(prod) },
                    onToggleWishlist = { onRemoveWishlist(prod) },
                    onAddToCart = { onMoveToCart(prod) }
                )
            }
        }
    }
}
