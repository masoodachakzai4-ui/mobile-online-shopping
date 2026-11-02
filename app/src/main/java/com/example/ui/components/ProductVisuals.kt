package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.Product
import com.example.ui.theme.StarGold
import java.text.NumberFormat
import java.util.Locale

fun formatPkr(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }
    return "Rs. ${formatter.format(amount)}"
}

fun categoryIcon(category: String): ImageVector = when (category.lowercase()) {
    "electronics" -> Icons.Default.Headphones
    "fashion" -> Icons.Default.Checkroom
    "shoes" -> Icons.AutoMirrored.Filled.DirectionsRun
    "beauty" -> Icons.Default.Spa
    "accessories" -> Icons.Default.Watch
    else -> Icons.Default.AutoAwesome
}

fun categoryColors(category: String): List<Color> = when (category.lowercase()) {
    "electronics" -> listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
    "fashion" -> listOf(Color(0xFF7C2D12), Color(0xFFC2410C))
    "shoes" -> listOf(Color(0xFF991B1B), Color(0xFFE03E1A))
    "beauty" -> listOf(Color(0xFF701A75), Color(0xFFBE185D))
    "accessories" -> listOf(Color(0xFF064E3B), Color(0xFF0F766E))
    else -> listOf(Color(0xFF1E293B), Color(0xFFE03E1A))
}

@Composable
fun ProductVisualImage(
    imageUrl: String,
    category: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 44.dp
) {
    val gradientColors = categoryColors(category)
    val icon = categoryIcon(category)

    Box(
        modifier = modifier
            .background(Brush.linearGradient(gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.8f, size.height * 0.2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = size.minDimension * 0.30f,
                center = Offset(size.width * 0.2f, size.height * 0.8f)
            )
        }

        if (imageUrl.isNotBlank()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(iconSize)
                    )
                },
                error = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(iconSize)
                    )
                }
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    isWishlisted: Boolean,
    onProductClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
            ) {
                ProductVisualImage(
                    imageUrl = product.imageUrl,
                    category = product.category,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Discount pill
                if (product.discountPercentage > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(bottomEnd = 14.dp, topStart = 16.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-${product.discountPercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                // Wishlist heart button
                IconButton(
                    onClick = onToggleWishlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.38f))
                        .testTag("wishlist_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isWishlisted) "Remove from wishlist" else "Save to wishlist",
                        tint = if (isWishlisted) Color(0xFFFF4D6D) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Category chip at bottom-left of image
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = product.brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", product.ratingAvg),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${product.reviewCount})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatPkr(product.price),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (product.originalPrice > product.price) {
                            Text(
                                text = formatPkr(product.originalPrice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    FilledIconButton(
                        onClick = onAddToCart,
                        enabled = product.stock > 0L,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("add_to_cart_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Add ${product.title} to cart",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
