package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.CatalogSeedData
import com.example.data.model.UserProfile
import com.example.ui.components.ProductVisualImage
import com.example.ui.components.formatPkr
import com.example.ui.theme.BankTransferBlue
import com.example.ui.theme.CodAmber
import com.example.ui.theme.EasyPaisaGreen
import com.example.ui.theme.JazzCashRed
import com.example.ui.viewmodel.UiState

@Composable
fun CartScreen(
    cartState: UiState<List<CartItem>>,
    promoCode: String,
    discountRate: Double,
    onPromoCodeChange: (String) -> Unit,
    onApplyPromo: () -> Unit,
    onUpdateQuantity: (String, Long) -> Unit,
    onRemoveItem: (String) -> Unit,
    onProceedToCheckout: () -> Unit,
    onExploreProducts: () -> Unit
) {
    val items = (cartState as? UiState.Success)?.data ?: emptyList()

    if (items.isEmpty()) {
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
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCartCheckout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Text(
                    text = "Your Shopping Cart is Empty",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Add products from Electronics, Fashion, Shoes, Beauty, or Accessories to start checking out.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onExploreProducts,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("empty_cart_shop_btn")
                ) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse Catalog")
                }
            }
        }
        return
    }

    val subtotal = items.sumOf { it.lineTotal }
    val deliveryFee = if (subtotal >= 15000.0) 0.0 else 250.0
    val discountAmount = subtotal * discountRate
    val total = (subtotal + deliveryFee - discountAmount).coerceAtLeast(1.0)

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Shopping Cart (${items.sumOf { it.quantity }} items)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(items, key = { it.id }) { item ->
                CartItemRowCard(
                    item = item,
                    onIncrement = { onUpdateQuantity(item.id, item.quantity + 1L) },
                    onDecrement = { onUpdateQuantity(item.id, item.quantity - 1L) },
                    onRemove = { onRemoveItem(item.id) }
                )
            }

            // Promo Code Box
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Have a Promo Code? (Try PK20 or BAZAAR10)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = promoCode,
                                onValueChange = onPromoCodeChange,
                                placeholder = { Text("Enter promo code") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("promo_code_input")
                            )
                            OutlinedButton(
                                onClick = onApplyPromo,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("apply_promo_btn")
                            ) {
                                Text("Apply", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Order Summary & Checkout Bottom Sheet
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPriceLine("Subtotal", formatPkr(subtotal))
                SummaryPriceLine(
                    "Shipping Fee",
                    if (deliveryFee == 0.0) "FREE" else formatPkr(deliveryFee),
                    highlight = deliveryFee == 0.0
                )
                if (discountAmount > 0.0) {
                    SummaryPriceLine(
                        "Promo Discount (${(discountRate * 100).toInt()}%)",
                        "-${formatPkr(discountAmount)}",
                        highlight = true
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Payable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatPkr(total),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onProceedToCheckout,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("proceed_to_checkout_btn")
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCartCheckout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Proceed to Checkout (${formatPkr(total)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemRowCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductVisualImage(
                imageUrl = item.productImage,
                category = item.category,
                contentDescription = item.productTitle,
                iconSize = 28.dp,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.selectedSize} • ${item.selectedColor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatPkr(item.lineTotal),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDecrement,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease quantity",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = onIncrement,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase quantity",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPriceLine(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    userProfile: UserProfile?,
    discountRate: Double,
    isBusy: Boolean,
    onBack: () -> Unit,
    onConfirmOrder: (
        customerName: String,
        customerPhone: String,
        shippingAddress: String,
        city: String,
        paymentMethod: String,
        paymentAccountRef: String
    ) -> Unit
) {
    BackHandler(onBack = onBack)

    var customerName by remember(userProfile) {
        mutableStateOf(userProfile?.displayName?.ifBlank { "Shopper" } ?: "Shopper")
    }
    var customerPhone by remember(userProfile) {
        mutableStateOf(userProfile?.phone?.ifBlank { "0300-1234567" } ?: "0300-1234567")
    }
    var shippingAddress by remember(userProfile) {
        mutableStateOf(userProfile?.shippingAddress?.ifBlank { "House 24, Main Boulevard, Gulberg III" } ?: "House 24, Main Boulevard, Gulberg III")
    }
    var city by remember(userProfile) {
        mutableStateOf(userProfile?.city?.ifBlank { "Lahore" } ?: "Lahore")
    }
    var selectedPayment by remember(userProfile) {
        mutableStateOf(userProfile?.preferredPayment?.ifBlank { "JazzCash" } ?: "JazzCash")
    }
    var paymentAccountRef by remember {
        mutableStateOf("03001234567")
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    val subtotal = cartItems.sumOf { it.lineTotal }
    val deliveryFee = if (subtotal >= 15000.0) 0.0 else 250.0
    val discountAmount = subtotal * discountRate
    val totalAmount = (subtotal + deliveryFee - discountAmount).coerceAtLeast(1.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(tonalElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to cart"
                    )
                }
                Text(
                    text = "Secure Checkout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Shipping Details Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Delivery Address & Contact",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Recipient Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("checkout_phone_input")
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("checkout_city_input")
                        )
                    }
                    OutlinedTextField(
                        value = shippingAddress,
                        onValueChange = { shippingAddress = it },
                        label = { Text("Complete Street Address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_address_input")
                    )
                }
            }

            // 2. Payment Methods Card (COD, JazzCash, EasyPaisa, Bank Transfer, Online Card)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. Select Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    CatalogSeedData.PAYMENT_METHODS.forEach { method ->
                        val isSelected = selectedPayment == method
                        val (icon, badgeColor, subtitle) = paymentMethodMeta(method)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) badgeColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) badgeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPayment = method }
                                .testTag("payment_option_${method.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(badgeColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = method,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPayment = method }
                                )
                            }
                        }
                    }

                    // Dynamic Payment Reference Input for Wallet / Bank / Card
                    if (selectedPayment != "Cash on Delivery") {
                        val refLabel = when (selectedPayment) {
                            "JazzCash" -> "JazzCash Mobile Account # (e.g. 03001234567)"
                            "EasyPaisa" -> "EasyPaisa Mobile Account # (e.g. 03451234567)"
                            "Bank Transfer" -> "Sender IBAN / Transaction Ref (Sent to PK36MEZN000123456789)"
                            else -> "Card Holder & Last 4 Digits (Visa / Mastercard)"
                        }
                        OutlinedTextField(
                            value = paymentAccountRef,
                            onValueChange = { paymentAccountRef = it },
                            label = { Text(refLabel) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_ref_input")
                        )
                    }
                }
            }

            if (validationError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Bottom Place Order Bar
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pay via $selectedPayment",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatPkr(totalAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (customerName.trim().length < 2 || customerPhone.trim().length < 5 || shippingAddress.trim().length < 5 || city.trim().length < 2) {
                                validationError = "Please enter valid recipient name, phone, city, and street address."
                                return@Button
                            }
                            validationError = null
                            onConfirmOrder(
                                customerName,
                                customerPhone,
                                shippingAddress,
                                city,
                                selectedPayment,
                                if (selectedPayment == "Cash on Delivery") "COD" else paymentAccountRef.ifBlank { customerPhone }
                            )
                        },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("confirm_order_btn")
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Place Order Now",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun paymentMethodMeta(method: String): Triple<ImageVector, Color, String> = when (method) {
    "JazzCash" -> Triple(
        Icons.Default.PhoneAndroid,
        JazzCashRed,
        "Instant mobile wallet payment with MPIN confirmation"
    )
    "EasyPaisa" -> Triple(
        Icons.Default.PhoneAndroid,
        EasyPaisaGreen,
        "Fast 1-tap EasyPaisa mobile wallet checkout"
    )
    "Bank Transfer" -> Triple(
        Icons.Default.AccountBalance,
        BankTransferBlue,
        "Direct RAAST / IBAN transfer to BazaarPK Meezan/HBL account"
    )
    "Online Card" -> Triple(
        Icons.Default.CreditCard,
        Color(0xFF7C3AED),
        "3D-Secure Visa, Mastercard & UnionPay debit/credit cards"
    )
    else -> Triple(
        Icons.Default.LocalAtm,
        CodAmber,
        "Pay in cash at your doorstep upon package delivery"
    )
}
