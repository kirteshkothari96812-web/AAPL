package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CartItem
import com.example.data.PaymentMode
import com.example.data.Product
import com.example.ui.PosUiState
import com.example.ui.theme.MonoPriceLarge
import com.example.ui.theme.MonoPriceMedium
import com.example.ui.theme.StockAmberBg
import com.example.ui.theme.StockAmberFg
import com.example.ui.theme.StockGreenBg
import com.example.ui.theme.StockGreenFg
import com.example.ui.theme.StockRedBg
import com.example.ui.theme.StockRedFg
import com.example.util.Formatters
import kotlin.math.abs

/**
 * TAB 1: NEW SALE (POS BILLING SCREEN)
 *
 * PRIVACY GUARANTEE: Stock quantity is NEVER shown anywhere on this screen,
 * neither in search results nor in quick suggestions nor in cart rows.
 */
@Composable
fun NewSaleScreen(
    uiState: PosUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddProduct: (Product) -> Unit,
    onIncrementQty: (Int) -> Unit,
    onDecrementQty: (Int) -> Unit,
    onQtyInputChange: (Int, String) -> Unit,
    onPriceInputChange: (Int, String) -> Unit,
    onRemoveCartItem: (Int) -> Unit,
    onClearCart: () -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onCustomerMobileChange: (String) -> Unit,
    onCustomerAddressChange: (String) -> Unit,
    onSelectPaymentMode: (PaymentMode) -> Unit,
    onSplitCashChange: (String) -> Unit,
    onSplitOnlineChange: (String) -> Unit,
    onSplitCardChange: (String) -> Unit,
    onFillRemaining: (PaymentMode) -> Unit,
    onSaveSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery = uiState.posSearchQuery.trim()
    val matchingProducts = remember(uiState.products, searchQuery) {
        if (searchQuery.isEmpty()) {
            emptyList()
        } else {
            val q = searchQuery.lowercase()
            uiState.products
                .filter { it.name.lowercase().contains(q) }
                .sortedWith(
                    compareByDescending<Product> { it.name.lowercase().startsWith(q) }
                        .thenBy { it.name.lowercase() }
                )
                .take(12)
        }
    }

    val quickPickProducts = remember(uiState.products) {
        uiState.products.filter { it.price > 0.0 }.take(10)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Smart Instant Product Search
                item(key = "pos_search_block") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = uiState.posSearchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pos_search_input"),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.pos_search_placeholder),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search products",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (uiState.posSearchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier.testTag("pos_search_clear")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        // Instant Search Dropdown (ONLY Item Name & Sale Price — NO stock!)
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pos_search_dropdown"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                if (matchingProducts.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No matching products for \"$searchQuery\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Column {
                                        matchingProducts.forEachIndexed { idx, product ->
                                            SearchDropdownRow(
                                                product = product,
                                                onClick = { onAddProduct(product) }
                                            )
                                            if (idx < matchingProducts.lastIndex) {
                                                HorizontalDivider(
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick-add horizontal chips when search is idle
                if (searchQuery.isEmpty() && quickPickProducts.isNotEmpty()) {
                    item(key = "pos_quick_picks") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Quick Add",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(quickPickProducts, key = { it.slNo }) { product ->
                                    Surface(
                                        onClick = { onAddProduct(product) },
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("quick_add_${product.slNo}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = product.name,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.widthIn(max = 170.dp)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = Formatters.formatInr(product.price),
                                                    style = MonoPriceMedium.copy(fontSize = 12.sp),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Cart Header
                item(key = "pos_cart_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalUnits = uiState.cart.sumOf { it.qty }
                        Text(
                            text = if (uiState.cart.isEmpty()) {
                                "Bill Items"
                            } else {
                                "Bill Items (${uiState.cart.size} items • $totalUnits qty)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (uiState.cart.isNotEmpty()) {
                            TextButton(
                                onClick = onClearCart,
                                modifier = Modifier.testTag("pos_clear_cart_button")
                            ) {
                                Text(
                                    text = stringResource(R.string.pos_clear_cart),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Empty Cart or Cart Rows
                if (uiState.cart.isEmpty()) {
                    item(key = "pos_empty_cart") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp, horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.ShoppingBag,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(R.string.pos_cart_empty_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = stringResource(R.string.pos_cart_empty_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.cart, key = { it.productSlNo }) { cartItem ->
                        CartItemCard(
                            item = cartItem,
                            onIncrement = { onIncrementQty(cartItem.productSlNo) },
                            onDecrement = { onDecrementQty(cartItem.productSlNo) },
                            onQtyInputChange = { onQtyInputChange(cartItem.productSlNo, it) },
                            onPriceInputChange = { onPriceInputChange(cartItem.productSlNo, it) },
                            onRemove = { onRemoveCartItem(cartItem.productSlNo) }
                        )
                    }
                }

                // Customer Name + Payment Mode Card
                item(key = "pos_checkout_details") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.customerName,
                                onValueChange = onCustomerNameChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pos_customer_input"),
                                label = { Text(stringResource(R.string.pos_customer_label)) },
                                placeholder = { Text(stringResource(R.string.pos_customer_default)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.PersonOutline,
                                        contentDescription = "Customer name",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            OutlinedTextField(
                                value = uiState.customerMobile,
                                onValueChange = onCustomerMobileChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pos_customer_mobile_input"),
                                label = { Text(stringResource(R.string.pos_customer_mobile_label)) },
                                placeholder = { Text(stringResource(R.string.pos_customer_mobile_placeholder)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = "Customer mobile number",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                prefix = {
                                    Text(
                                        text = "+91 ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            OutlinedTextField(
                                value = uiState.customerAddress,
                                onValueChange = onCustomerAddressChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pos_customer_address_input"),
                                label = { Text(stringResource(R.string.pos_customer_address_label)) },
                                placeholder = { Text(stringResource(R.string.pos_customer_address_placeholder)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = "Customer address",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = stringResource(R.string.pos_payment_mode_label),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PaymentModePill(
                                        mode = PaymentMode.CASH,
                                        icon = Icons.Filled.Payments,
                                        selected = uiState.paymentMode == PaymentMode.CASH,
                                        onClick = { onSelectPaymentMode(PaymentMode.CASH) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    PaymentModePill(
                                        mode = PaymentMode.ONLINE,
                                        icon = Icons.Filled.QrCode2,
                                        selected = uiState.paymentMode == PaymentMode.ONLINE,
                                        onClick = { onSelectPaymentMode(PaymentMode.ONLINE) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    PaymentModePill(
                                        mode = PaymentMode.CARD,
                                        icon = Icons.Filled.CreditCard,
                                        selected = uiState.paymentMode == PaymentMode.CARD,
                                        onClick = { onSelectPaymentMode(PaymentMode.CARD) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    PaymentModePill(
                                        mode = PaymentMode.SPLIT,
                                        icon = Icons.Filled.CallSplit,
                                        selected = uiState.paymentMode == PaymentMode.SPLIT,
                                        onClick = { onSelectPaymentMode(PaymentMode.SPLIT) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = uiState.paymentMode == PaymentMode.SPLIT,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                SplitPaymentSection(
                                    uiState = uiState,
                                    onSplitCashChange = onSplitCashChange,
                                    onSplitOnlineChange = onSplitOnlineChange,
                                    onSplitCardChange = onSplitCardChange,
                                    onFillRemaining = onFillRemaining
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Sticky Grand Total & Save Sale Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.pos_grand_total),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatInr(uiState.grandTotal),
                                style = MonoPriceLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("pos_grand_total_value")
                            )
                        }

                        Button(
                            onClick = onSaveSale,
                            enabled = uiState.canSaveSale,
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .heightIn(min = 52.dp)
                                .testTag("save_sale_button")
                        ) {
                            if (uiState.isBusyAction) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = stringResource(R.string.pos_save_sale),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchDropdownRow(
    product: Product,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("pos_search_item_${product.slNo}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = Formatters.formatInr(product.price),
                style = MonoPriceMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AddShoppingCart,
                        contentDescription = "Add ${product.name}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onQtyInputChange: (String) -> Unit,
    onPriceInputChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.productSlNo}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("cart_remove_${item.productSlNo}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Remove ${item.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Editable Unit Price
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "₹",
                        style = MonoPriceMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        BasicTextField(
                            value = item.priceInput,
                            onValueChange = onPriceInputChange,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MonoPriceMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .width(72.dp)
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .testTag("cart_price_${item.productSlNo}")
                        )
                    }
                }

                // Quantity stepper (- / input / +)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("cart_dec_${item.productSlNo}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease quantity",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    BasicTextField(
                        value = item.qtyInput,
                        onValueChange = onQtyInputChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MonoPriceMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .width(40.dp)
                            .testTag("cart_qty_${item.productSlNo}")
                    )
                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("cart_inc_${item.productSlNo}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase quantity",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Line Total
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatInr(item.lineTotal),
                        style = MonoPriceMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("cart_line_total_${item.productSlNo}")
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentModePill(
    mode: PaymentMode,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        contentColor = contentColor,
        border = border,
        modifier = modifier
            .heightIn(min = 56.dp)
            .testTag("payment_mode_${mode.label.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = mode.label,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = mode.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SplitPaymentSection(
    uiState: PosUiState,
    onSplitCashChange: (String) -> Unit,
    onSplitOnlineChange: (String) -> Unit,
    onSplitCardChange: (String) -> Unit,
    onFillRemaining: (PaymentMode) -> Unit
) {
    val remaining = uiState.splitRemaining
    val (statusBg, statusFg, statusText) = when {
        abs(remaining) < 0.01 && uiState.grandTotal > 0.0 -> Triple(
            StockGreenBg,
            StockGreenFg,
            stringResource(R.string.pos_split_balanced)
        )
        remaining > 0.0 -> Triple(
            StockAmberBg,
            StockAmberFg,
            stringResource(R.string.pos_split_remaining, Formatters.formatInr(remaining))
        )
        else -> Triple(
            StockRedBg,
            StockRedFg,
            stringResource(R.string.pos_split_overpaid, Formatters.formatInr(abs(remaining)))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Split Breakdown",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusBg
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = statusFg,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("split_remaining_badge")
                )
            }
        }

        SplitAmountRow(
            label = "Cash (₹)",
            value = uiState.splitState.cashInput,
            onValueChange = onSplitCashChange,
            onFillRemaining = { onFillRemaining(PaymentMode.CASH) },
            fieldTag = "split_cash_input",
            fillTag = "split_fill_cash"
        )
        SplitAmountRow(
            label = "Online (₹)",
            value = uiState.splitState.onlineInput,
            onValueChange = onSplitOnlineChange,
            onFillRemaining = { onFillRemaining(PaymentMode.ONLINE) },
            fieldTag = "split_online_input",
            fillTag = "split_fill_online"
        )
        SplitAmountRow(
            label = "Card (₹)",
            value = uiState.splitState.cardInput,
            onValueChange = onSplitCardChange,
            onFillRemaining = { onFillRemaining(PaymentMode.CARD) },
            fieldTag = "split_card_input",
            fillTag = "split_fill_card"
        )
    }
}

@Composable
private fun SplitAmountRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onFillRemaining: () -> Unit,
    fieldTag: String,
    fillTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(fieldTag)
        )
        Surface(
            onClick = onFillRemaining,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag(fillTag)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.pos_fill_remaining),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
