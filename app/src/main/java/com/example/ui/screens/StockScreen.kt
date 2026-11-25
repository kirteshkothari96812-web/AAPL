package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Product
import com.example.ui.PosUiState
import com.example.ui.StockFilter
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoPriceMedium
import com.example.ui.theme.StockAmberBg
import com.example.ui.theme.StockAmberFg
import com.example.ui.theme.StockGreenBg
import com.example.ui.theme.StockGreenFg
import com.example.ui.theme.StockRedBg
import com.example.ui.theme.StockRedFg
import com.example.util.Formatters

/**
 * TAB 4: STOCK & PRODUCTS (INTERNAL INVENTORY)
 *
 * The ONLY screen in the app where Stock Quantity is displayed.
 */
@Composable
fun StockScreen(
    uiState: PosUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectFilter: (StockFilter) -> Unit,
    onAddNewProductClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    onDismissEditModal: () -> Unit,
    onSaveProductUpdate: (Product) -> Unit,
    onDismissAddModal: () -> Unit,
    onCreateProduct: (name: String, hsnCode: String, stock: Int, price: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val outCount = remember(uiState.products) { uiState.products.count { it.stock <= 0 } }
    val lowCount = remember(uiState.products) { uiState.products.count { it.stock in 1..5 } }
    val healthyCount = remember(uiState.products) { uiState.products.count { it.stock > 5 } }

    val filteredProducts = remember(uiState.products, uiState.stockSearchQuery, uiState.stockFilter) {
        val q = uiState.stockSearchQuery.trim().lowercase()
        uiState.products.filter { prod ->
            val queryMatch = q.isEmpty() ||
                prod.name.lowercase().contains(q) ||
                prod.hsnCode.lowercase().contains(q)
            val filterMatch = when (uiState.stockFilter) {
                StockFilter.ALL -> true
                StockFilter.OUT_OF_STOCK -> prod.stock <= 0
                StockFilter.LOW_STOCK -> prod.stock in 1..5
                StockFilter.HEALTHY -> prod.stock > 5
            }
            queryMatch && filterMatch
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header + "+ Add New Product" button
            item(key = "stock_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Stock & Products",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.products.size} products • Tap any row to restock or edit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onAddNewProductClick,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("add_new_product_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Product",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Search bar (Item Name OR HSN Code)
            item(key = "stock_search") {
                OutlinedTextField(
                    value = uiState.stockSearchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stock_search_input"),
                    placeholder = { Text(stringResource(R.string.stock_search_hint)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search by name or HSN",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.stockSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                )
            }

            // Stock Filter Chips
            item(key = "stock_filter_chips") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf(
                        StockFilter.ALL to "All (${uiState.products.size})",
                        StockFilter.OUT_OF_STOCK to "Out ($outCount)",
                        StockFilter.LOW_STOCK to "Low 1–5 ($lowCount)",
                        StockFilter.HEALTHY to "In Stock ($healthyCount)"
                    )
                    items(chips) { (filter, label) ->
                        FilterChip(
                            selected = uiState.stockFilter == filter,
                            onClick = { onSelectFilter(filter) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("stock_filter_${filter.name.lowercase()}")
                        )
                    }
                }
            }

            // Dedicated 4-Column Inventory Table Header
            item(key = "stock_table_header") {
                Surface(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.stock_col_item),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1.45f)
                        )
                        Text(
                            text = stringResource(R.string.stock_col_hsn),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(0.95f)
                        )
                        Text(
                            text = stringResource(R.string.stock_col_price),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(0.72f)
                        )
                        Text(
                            text = stringResource(R.string.stock_col_stock),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(0.62f)
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item(key = "stock_empty") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "No matching products",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(filteredProducts, key = { _, p -> p.slNo }) { index, product ->
                    StockProductRow(
                        product = product,
                        isLast = index == filteredProducts.lastIndex,
                        onClick = { onProductClick(product) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    // Edit / Restock Modal
    uiState.editingProduct?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = onDismissEditModal,
            onSave = onSaveProductUpdate
        )
    }

    // Add New Product Modal
    if (uiState.showAddProductModal) {
        AddProductDialog(
            onDismiss = onDismissAddModal,
            onCreate = onCreateProduct
        )
    }
}

@Composable
private fun StockProductRow(
    product: Product,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val (stockBg, stockFg) = when {
        product.stock <= 0 -> StockRedBg to StockRedFg
        product.stock in 1..5 -> StockAmberBg to StockAmberFg
        else -> StockGreenBg to StockGreenFg
    }

    val shape = if (isLast) {
        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(0.dp)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_row_${product.slNo}"),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Item Name
                Column(
                    modifier = Modifier
                        .weight(1.45f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "#${product.slNo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Column 2: Dedicated HSN Code column (shows "—" if empty)
                Text(
                    text = product.hsnCode.ifBlank { "—" },
                    style = MonoCodeStyle.copy(fontSize = 11.sp),
                    color = if (product.hsnCode.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(0.95f)
                        .padding(end = 4.dp)
                )

                // Column 3: Sale Price (₹)
                Text(
                    text = Formatters.formatInr(product.price),
                    style = MonoPriceMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(0.72f)
                        .padding(end = 6.dp)
                )

                // Column 4: Color-coded Current Stock pill
                Box(
                    modifier = Modifier.weight(0.62f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = stockBg
                    ) {
                        Text(
                            text = product.stock.toString(),
                            style = MonoPriceMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = stockFg,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("stock_badge_${product.slNo}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var stockInput by remember(product.slNo) { mutableStateOf(product.stock.toString()) }
    var restockDeltaInput by remember(product.slNo) { mutableStateOf("") }
    var priceInput by remember(product.slNo) { mutableStateOf(Formatters.formatNumericPlain(product.price)) }
    var hsnInput by remember(product.slNo) { mutableStateOf(product.hsnCode) }

    fun applyRestock(delta: Int) {
        val current = stockInput.toIntOrNull() ?: product.stock
        stockInput = (current + delta).coerceAtLeast(0).toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column {
                Text(
                    text = stringResource(R.string.stock_edit_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Quick +Restock buttons
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.stock_quick_restock),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 20, 50).forEach { addQty ->
                            OutlinedButton(
                                onClick = { applyRestock(addQty) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .testTag("restock_plus_$addQty")
                            ) {
                                Text("+$addQty", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = restockDeltaInput,
                            onValueChange = { restockDeltaInput = it.filter { c -> c.isDigit() }.take(5) },
                            label = { Text("Custom +Restock") },
                            placeholder = { Text("e.g. 12") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val add = restockDeltaInput.toIntOrNull() ?: 0
                                if (add > 0) {
                                    applyRestock(add)
                                    restockDeltaInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("+Add")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Exact Stock Quantity
                OutlinedTextField(
                    value = stockInput,
                    onValueChange = { raw ->
                        stockInput = raw.filterIndexed { idx, c -> c.isDigit() || (idx == 0 && c == '-') }.take(6)
                    },
                    label = { Text("Exact Stock Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_stock_input")
                )

                // Sale Price (₹)
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { raw ->
                        val sb = StringBuilder()
                        var dot = false
                        for (c in raw) {
                            if (c.isDigit()) sb.append(c)
                            else if (c == '.' && !dot) {
                                dot = true
                                sb.append(c)
                            }
                        }
                        priceInput = sb.toString().take(10)
                    },
                    label = { Text("Sale Price (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_price_input")
                )

                // HSN Code
                OutlinedTextField(
                    value = hsnInput,
                    onValueChange = { hsnInput = it.take(20) },
                    label = { Text("HSN Code") },
                    placeholder = { Text("Optional") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_hsn_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newStock = stockInput.toIntOrNull() ?: product.stock
                    val newPrice = priceInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: product.price
                    onSave(
                        product.copy(
                            stock = newStock,
                            price = newPrice,
                            hsnCode = hsnInput.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("edit_product_save_button")
            ) {
                Text(stringResource(R.string.stock_save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, hsnCode: String, stock: Int, price: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hsnCode by remember { mutableStateOf("") }
    var stockInput by remember { mutableStateOf("0") }
    var priceInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.stock_add_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    placeholder = { Text("e.g. Organic Mustard Oil 1L") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_product_name_input")
                )
                OutlinedTextField(
                    value = hsnCode,
                    onValueChange = { hsnCode = it.take(20) },
                    label = { Text("HSN Code") },
                    placeholder = { Text("Optional") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_product_hsn_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { raw ->
                            val sb = StringBuilder()
                            var dot = false
                            for (c in raw) {
                                if (c.isDigit()) sb.append(c)
                                else if (c == '.' && !dot) {
                                    dot = true
                                    sb.append(c)
                                }
                            }
                            priceInput = sb.toString().take(10)
                        },
                        label = { Text("Price (₹) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_product_price_input")
                    )
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it.filter { c -> c.isDigit() }.take(6) },
                        label = { Text("Stock") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_product_stock_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = priceInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                    val parsedStock = stockInput.toIntOrNull() ?: 0
                    onCreate(name, hsnCode, parsedStock, parsedPrice)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("add_product_confirm_button")
            ) {
                Text(stringResource(R.string.stock_create_product))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
