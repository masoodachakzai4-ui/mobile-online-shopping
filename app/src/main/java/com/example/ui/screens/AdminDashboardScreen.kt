package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogSeedData
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.ui.components.ProductVisualImage
import com.example.ui.components.formatPkr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    products: List<Product>,
    orders: List<Order>,
    onBack: () -> Unit,
    onSaveProduct: (
        existing: Product?,
        title: String,
        description: String,
        category: String,
        price: Double,
        originalPrice: Double,
        stock: Long,
        imageUrl: String,
        featured: Boolean,
        brand: String
    ) -> Unit,
    onQuickAdjustStockPrice: (Product, Long, Double) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onUpdateOrderStatus: (Order, String, String) -> Unit,
    onRestoreSampleCatalog: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showProductDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Store"
                            )
                        }
                        Column {
                            Text(
                                text = "Admin Console",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Catalog, Inventory, Orders & Revenue Analytics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onRestoreSampleCatalog) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restore sample catalog"
                        )
                    }
                }

                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Analytics") },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        modifier = Modifier.testTag("admin_tab_analytics")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Products (${products.size})") },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        modifier = Modifier.testTag("admin_tab_products")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Orders (${orders.size})") },
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                        modifier = Modifier.testTag("admin_tab_orders")
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showProductDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("admin_add_product_fab")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Product")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Product", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> AdminRevenueAnalyticsTab(products = products, orders = orders)
                1 -> AdminProductsManagementTab(
                    products = products,
                    onEditProduct = { prod ->
                        editingProduct = prod
                        showProductDialog = true
                    },
                    onQuickAdjust = onQuickAdjustStockPrice,
                    onDeleteProduct = onDeleteProduct
                )
                2 -> AdminOrdersManagementTab(
                    orders = orders,
                    onUpdateOrderStatus = onUpdateOrderStatus
                )
            }
        }
    }

    if (showProductDialog) {
        ProductEditorDialog(
            initialProduct = editingProduct,
            onDismiss = { showProductDialog = false },
            onConfirm = { title, desc, cat, price, origPrice, stock, img, featured, brand ->
                onSaveProduct(editingProduct, title, desc, cat, price, origPrice, stock, img, featured, brand)
                showProductDialog = false
            }
        )
    }
}

@Composable
private fun AdminRevenueAnalyticsTab(
    products: List<Product>,
    orders: List<Order>
) {
    val activeOrders = orders.filter { it.orderStatus != "Cancelled" }
    val totalRevenue = activeOrders.sumOf { it.totalAmount }
    val totalUnitsSold = activeOrders.sumOf { it.itemCount }
    val totalInventoryValue = products.sumOf { it.price * it.stock }
    val lowStockProducts = products.filter { it.stock <= 15L }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiMetricCard(
                    title = "Total Sales Revenue",
                    value = formatPkr(totalRevenue),
                    subtitle = "${activeOrders.size} active orders • $totalUnitsSold units",
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Inventory Value",
                    value = formatPkr(totalInventoryValue),
                    subtitle = "${products.sumOf { it.stock }} total units in stock",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category Distribution Breakdown
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Catalog & Stock by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val totalStock = products.sumOf { it.stock }.coerceAtLeast(1L)
                    CatalogSeedData.PRODUCT_CATEGORIES.forEach { cat ->
                        val catProducts = products.filter { it.category.equals(cat, ignoreCase = true) }
                        val catStock = catProducts.sumOf { it.stock }
                        val ratio = (catStock.toFloat() / totalStock.toFloat()).coerceIn(0f, 1f)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$cat (${catProducts.size} SKUs)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$catStock units",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }

        // Payment Methods Revenue Breakdown
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Revenue by Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CatalogSeedData.PAYMENT_METHODS.forEach { method ->
                        val methodOrders = activeOrders.filter { it.paymentMethod == method }
                        val methodSum = methodOrders.sumOf { it.totalAmount }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$method (${methodOrders.size} orders)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = formatPkr(methodSum),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Low Stock Alerts
        if (lowStockProducts.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Low Inventory Alerts (${lowStockProducts.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        lowStockProducts.forEach { prod ->
                            Text(
                                text = "• ${prod.title} — Only ${prod.stock} left in stock",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun AdminProductsManagementTab(
    products: List<Product>,
    onEditProduct: (Product) -> Unit,
    onQuickAdjust: (Product, Long, Double) -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(products, key = { it.id }) { prod ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProductVisualImage(
                            imageUrl = prod.imageUrl,
                            category = prod.category,
                            contentDescription = prod.title,
                            iconSize = 26.dp,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${prod.brand} • ${prod.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = prod.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${formatPkr(prod.price)} • Stock: ${prod.stock}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { onEditProduct(prod) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit product")
                        }
                        IconButton(onClick = { onDeleteProduct(prod.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete product",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    HorizontalDivider()

                    // Quick Stock & Price Adjuster Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Stock:", style = MaterialTheme.typography.labelMedium)
                            OutlinedButton(
                                onClick = { onQuickAdjust(prod, (prod.stock - 5L).coerceAtLeast(0L), prod.price) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("5")
                            }
                            OutlinedButton(
                                onClick = { onQuickAdjust(prod, prod.stock + 5L, prod.price) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("5")
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Price:", style = MaterialTheme.typography.labelMedium)
                            OutlinedButton(
                                onClick = { onQuickAdjust(prod, prod.stock, (prod.price - 500.0).coerceAtLeast(500.0)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("-500")
                            }
                            OutlinedButton(
                                onClick = { onQuickAdjust(prod, prod.stock, prod.price + 500.0) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+500")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminOrdersManagementTab(
    orders: List<Order>,
    onUpdateOrderStatus: (Order, String, String) -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No customer orders placed yet.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(orders, key = { it.id }) { order ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "#${order.trackingNumber} • ${order.customerName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatPkr(order.totalAmount),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${order.itemsSummary} | ${order.paymentMethod} (${order.paymentStatus})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Update Fulfillment Status:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (CatalogSeedData.ORDER_STATUS_STEPS + "Cancelled").forEach { status ->
                            FilterChip(
                                selected = order.orderStatus == status,
                                onClick = {
                                    val newPayStatus = if (status == "Delivered") "Paid" else order.paymentStatus
                                    onUpdateOrderStatus(order, status, newPayStatus)
                                },
                                label = { Text(status, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductEditorDialog(
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        category: String,
        price: Double,
        originalPrice: Double,
        stock: Long,
        imageUrl: String,
        featured: Boolean,
        brand: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialProduct?.title ?: "") }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "BazaarPK") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Electronics") }
    var priceText by remember { mutableStateOf(initialProduct?.price?.toLong()?.toString() ?: "12500") }
    var originalPriceText by remember { mutableStateOf(initialProduct?.originalPrice?.toLong()?.toString() ?: "15000") }
    var stockText by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "25") }
    var imageUrl by remember { mutableStateOf(initialProduct?.imageUrl ?: "") }
    var featured by remember { mutableStateOf(initialProduct?.featured ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Add New Product" else "Edit Product",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_product_title_input")
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CatalogSeedData.PRODUCT_CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (PKR)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = originalPriceText,
                        onValueChange = { originalPriceText = it },
                        label = { Text("MSRP (PKR)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("Stock Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Feature on Home Banner", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = featured, onCheckedChange = { featured = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = priceText.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1000.0
                    val parsedOrig = originalPriceText.toDoubleOrNull()?.coerceAtLeast(parsedPrice) ?: parsedPrice
                    val parsedStock = stockText.toLongOrNull()?.coerceAtLeast(0L) ?: 10L
                    if (title.trim().length >= 2 && description.trim().length >= 5) {
                        onConfirm(
                            title,
                            description,
                            category,
                            parsedPrice,
                            parsedOrig,
                            parsedStock,
                            imageUrl,
                            featured,
                            brand
                        )
                    }
                },
                modifier = Modifier.testTag("admin_save_product_btn")
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
