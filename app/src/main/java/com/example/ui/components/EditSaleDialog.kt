package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.PaymentMode
import com.example.data.Product
import com.example.data.SaleItem
import com.example.data.SaleRecord
import com.example.ui.theme.MonoCodeStyle
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

private data class EditableSaleLine(
    val id: Int,
    val name: String,
    val hsnCode: String,
    val unitPrice: Double,
    val priceInput: String,
    val qty: Int,
    val qtyInput: String
) {
    val lineTotal: Double get() = unitPrice * qty
}

private fun sanitizeEditDecimal(raw: String): String {
    val sb = StringBuilder()
    var hasDot = false
    for (ch in raw) {
        if (ch.isDigit()) {
            sb.append(ch)
        } else if (ch == '.' && !hasDot) {
            hasDot = true
            sb.append(ch)
        }
    }
    return sb.toString().take(10)
}

/**
 * Modal for editing an existing sale from the Ledger tab.
 * Strictly hides product stock in the catalog search dropdown.
 */
@Composable
fun EditSaleDialog(
    sale: SaleRecord,
    products: List<Product>,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (SaleRecord) -> Unit
) {
    var dateInput by remember(sale.billId) { mutableStateOf(sale.date) }
    var customerName by remember(sale.billId) {
        mutableStateOf(if (sale.customerName == "Walk-in Customer") "" else sale.customerName)
    }
    var customerMobile by remember(sale.billId) { mutableStateOf(sale.customerMobile) }
    var customerAddress by remember(sale.billId) { mutableStateOf(sale.customerAddress) }

    var nextLineId by remember(sale.billId) { mutableIntStateOf(sale.items.size + 1) }
    var lines by remember(sale.billId) {
        mutableStateOf(
            sale.items.mapIndexed { idx, item ->
                EditableSaleLine(
                    id = idx + 1,
                    name = item.name,
                    hsnCode = item.hsnCode,
                    unitPrice = item.price,
                    priceInput = Formatters.formatNumericPlain(item.price),
                    qty = item.qty,
                    qtyInput = item.qty.toString()
                )
            }
        )
    }

    var searchQuery by remember(sale.billId) { mutableStateOf("") }
    var paymentMode by remember(sale.billId) { mutableStateOf(sale.paymentMode) }
    var splitCashInput by remember(sale.billId) {
        mutableStateOf(
            if (sale.paymentMode == PaymentMode.SPLIT && sale.cashAmount > 0.0) {
                Formatters.formatNumericPlain(sale.cashAmount)
            } else ""
        )
    }
    var splitOnlineInput by remember(sale.billId) {
        mutableStateOf(
            if (sale.paymentMode == PaymentMode.SPLIT && sale.onlineAmount > 0.0) {
                Formatters.formatNumericPlain(sale.onlineAmount)
            } else ""
        )
    }
    var splitCardInput by remember(sale.billId) {
        mutableStateOf(
            if (sale.paymentMode == PaymentMode.SPLIT && sale.cardAmount > 0.0) {
                Formatters.formatNumericPlain(sale.cardAmount)
            } else ""
        )
    }

    val grandTotal = lines.sumOf { it.lineTotal }
    val cashAmt = splitCashInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val onlineAmt = splitOnlineInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val cardAmt = splitCardInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val splitRemaining = grandTotal - (cashAmt + onlineAmt + cardAmt)
    val isSplitBalanced = abs(splitRemaining) < 0.01

    val canSave = !isBusy &&
        lines.isNotEmpty() &&
        lines.all { it.qty > 0 && it.unitPrice >= 0.0 } &&
        (paymentMode != PaymentMode.SPLIT || isSplitBalanced)

    val trimmedSearch = searchQuery.trim()
    val matchingProducts = remember(products, trimmedSearch) {
        if (trimmedSearch.isEmpty()) {
            emptyList()
        } else {
            val q = trimmedSearch.lowercase()
            products
                .filter { it.name.lowercase().contains(q) }
                .sortedWith(
                    compareByDescending<Product> { it.name.lowercase().startsWith(q) }
                        .thenBy { it.name.lowercase() }
                )
                .take(8)
        }
    }

    Dialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 620.dp)
                .heightIn(max = 760.dp)
                .testTag("edit_sale_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Edit Sale",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = sale.billId,
                                style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { if (!isBusy) onDismiss() },
                        enabled = !isBusy,
                        modifier = Modifier.testTag("edit_sale_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close edit modal"
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Scrollable body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date & Customer Details
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        enabled = !isBusy,
                        label = { Text("Date") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sale_date_input")
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        enabled = !isBusy,
                        label = { Text(stringResource(R.string.pos_customer_label)) },
                        placeholder = { Text(stringResource(R.string.pos_customer_default)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.PersonOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sale_customer_name_input")
                    )

                    OutlinedTextField(
                        value = customerMobile,
                        onValueChange = { raw -> customerMobile = raw.filter { it.isDigit() }.take(10) },
                        enabled = !isBusy,
                        label = { Text(stringResource(R.string.pos_customer_mobile_label)) },
                        placeholder = { Text(stringResource(R.string.pos_customer_mobile_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = null,
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
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sale_customer_mobile_input")
                    )

                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        enabled = !isBusy,
                        label = { Text(stringResource(R.string.pos_customer_address_label)) },
                        placeholder = { Text(stringResource(R.string.pos_customer_address_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sale_customer_address_input")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Items Sold section + Add Product Search (NO stock shown!)
                    Text(
                        text = "Items Sold (${lines.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        enabled = !isBusy,
                        placeholder = { Text("Search catalog to add another product…") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search product to add",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_sale_product_search")
                    )

                    AnimatedVisibility(
                        visible = trimmedSearch.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            if (matchingProducts.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No matching products",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Column {
                                    matchingProducts.forEachIndexed { idx, prod ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(enabled = !isBusy) {
                                                    val existingIdx = lines.indexOfFirst {
                                                        it.name.equals(prod.name, ignoreCase = true)
                                                    }
                                                    lines = if (existingIdx >= 0) {
                                                        lines.mapIndexed { i, ln ->
                                                            if (i == existingIdx) {
                                                                val q = ln.qty + 1
                                                                ln.copy(qty = q, qtyInput = q.toString())
                                                            } else ln
                                                        }
                                                    } else {
                                                        val assignedId = nextLineId++
                                                        lines + EditableSaleLine(
                                                            id = assignedId,
                                                            name = prod.name,
                                                            hsnCode = prod.hsnCode,
                                                            unitPrice = prod.price,
                                                            priceInput = Formatters.formatNumericPlain(prod.price),
                                                            qty = 1,
                                                            qtyInput = "1"
                                                        )
                                                    }
                                                    searchQuery = ""
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = prod.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = Formatters.formatInr(prod.price),
                                                    style = MonoPriceMedium.copy(fontSize = 13.sp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Filled.AddShoppingCart,
                                                            contentDescription = "Add ${prod.name}",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
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

                    // Existing & newly added lines
                    if (lines.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = StockRedBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Add at least one item to save this bill.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StockRedFg,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lines.forEach { line ->
                                EditSaleLineCard(
                                    line = line,
                                    enabled = !isBusy,
                                    onIncrement = {
                                        lines = lines.map {
                                            if (it.id == line.id) {
                                                val nq = it.qty + 1
                                                it.copy(qty = nq, qtyInput = nq.toString())
                                            } else it
                                        }
                                    },
                                    onDecrement = {
                                        lines = lines.mapNotNull {
                                            if (it.id == line.id) {
                                                val nq = it.qty - 1
                                                if (nq <= 0) null else it.copy(qty = nq, qtyInput = nq.toString())
                                            } else it
                                        }
                                    },
                                    onQtyInputChange = { raw ->
                                        val cleaned = raw.filter { it.isDigit() }.take(5)
                                        val parsed = cleaned.toIntOrNull() ?: 0
                                        lines = lines.map {
                                            if (it.id == line.id) it.copy(qtyInput = cleaned, qty = parsed) else it
                                        }
                                    },
                                    onPriceInputChange = { raw ->
                                        val cleaned = sanitizeEditDecimal(raw)
                                        val parsed = cleaned.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
                                        lines = lines.map {
                                            if (it.id == line.id) it.copy(priceInput = cleaned, unitPrice = parsed) else it
                                        }
                                    },
                                    onRemove = {
                                        lines = lines.filterNot { it.id == line.id }
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Payment Mode selector
                    Text(
                        text = stringResource(R.string.pos_payment_mode_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EditPaymentPill(
                            mode = PaymentMode.CASH,
                            icon = Icons.Filled.Payments,
                            selected = paymentMode == PaymentMode.CASH,
                            enabled = !isBusy,
                            onClick = { paymentMode = PaymentMode.CASH },
                            modifier = Modifier.weight(1f)
                        )
                        EditPaymentPill(
                            mode = PaymentMode.ONLINE,
                            icon = Icons.Filled.QrCode2,
                            selected = paymentMode == PaymentMode.ONLINE,
                            enabled = !isBusy,
                            onClick = { paymentMode = PaymentMode.ONLINE },
                            modifier = Modifier.weight(1f)
                        )
                        EditPaymentPill(
                            mode = PaymentMode.CARD,
                            icon = Icons.Filled.CreditCard,
                            selected = paymentMode == PaymentMode.CARD,
                            enabled = !isBusy,
                            onClick = { paymentMode = PaymentMode.CARD },
                            modifier = Modifier.weight(1f)
                        )
                        EditPaymentPill(
                            mode = PaymentMode.SPLIT,
                            icon = Icons.Filled.CallSplit,
                            selected = paymentMode == PaymentMode.SPLIT,
                            enabled = !isBusy,
                            onClick = { paymentMode = PaymentMode.SPLIT },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(
                        visible = paymentMode == PaymentMode.SPLIT,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val (statusBg, statusFg, statusText) = when {
                            isSplitBalanced && grandTotal > 0.0 -> Triple(
                                StockGreenBg,
                                StockGreenFg,
                                stringResource(R.string.pos_split_balanced)
                            )
                            splitRemaining > 0.0 -> Triple(
                                StockAmberBg,
                                StockAmberFg,
                                stringResource(R.string.pos_split_remaining, Formatters.formatInr(splitRemaining))
                            )
                            else -> Triple(
                                StockRedBg,
                                StockRedFg,
                                stringResource(R.string.pos_split_overpaid, Formatters.formatInr(abs(splitRemaining)))
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Split Breakdown",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Surface(shape = RoundedCornerShape(16.dp), color = statusBg) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = statusFg,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            EditSplitFieldRow(
                                label = "Cash (₹)",
                                value = splitCashInput,
                                enabled = !isBusy,
                                onValueChange = { splitCashInput = sanitizeEditDecimal(it) },
                                onFillRemaining = {
                                    val rem = (grandTotal - onlineAmt - cardAmt).coerceAtLeast(0.0)
                                    splitCashInput = Formatters.formatNumericPlain(rem)
                                }
                            )
                            EditSplitFieldRow(
                                label = "Online (₹)",
                                value = splitOnlineInput,
                                enabled = !isBusy,
                                onValueChange = { splitOnlineInput = sanitizeEditDecimal(it) },
                                onFillRemaining = {
                                    val rem = (grandTotal - cashAmt - cardAmt).coerceAtLeast(0.0)
                                    splitOnlineInput = Formatters.formatNumericPlain(rem)
                                }
                            )
                            EditSplitFieldRow(
                                label = "Card (₹)",
                                value = splitCardInput,
                                enabled = !isBusy,
                                onValueChange = { splitCardInput = sanitizeEditDecimal(it) },
                                onFillRemaining = {
                                    val rem = (grandTotal - cashAmt - onlineAmt).coerceAtLeast(0.0)
                                    splitCardInput = Formatters.formatNumericPlain(rem)
                                }
                            )
                        }
                    }
                }

                // Footer with Grand Total + Cancel / Save Changes
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Grand Total",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatInr(grandTotal),
                            style = MonoPriceLarge.copy(fontSize = 22.sp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("edit_sale_grand_total")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            enabled = !isBusy,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("edit_sale_cancel_button")
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val (finalCash, finalOnline, finalCard) = when (paymentMode) {
                                    PaymentMode.CASH -> Triple(grandTotal, 0.0, 0.0)
                                    PaymentMode.ONLINE -> Triple(0.0, grandTotal, 0.0)
                                    PaymentMode.CARD -> Triple(0.0, 0.0, grandTotal)
                                    PaymentMode.SPLIT -> Triple(cashAmt, onlineAmt, cardAmt)
                                }
                                val updatedRecord = sale.copy(
                                    date = dateInput.trim().ifEmpty { sale.date },
                                    customerName = customerName.trim().ifEmpty { "Walk-in Customer" },
                                    customerMobile = customerMobile.filter { it.isDigit() },
                                    customerAddress = customerAddress.trim(),
                                    items = lines.map { ln ->
                                        SaleItem(
                                            name = ln.name,
                                            hsnCode = ln.hsnCode,
                                            qty = ln.qty,
                                            price = ln.unitPrice,
                                            total = ln.lineTotal
                                        )
                                    },
                                    totalAmount = grandTotal,
                                    paymentMode = paymentMode,
                                    cashAmount = finalCash,
                                    onlineAmount = finalOnline,
                                    cardAmount = finalCard
                                )
                                onSave(updatedRecord)
                            },
                            enabled = canSave,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("edit_sale_save_button")
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving…")
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.stock_save_changes),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditSaleLineCard(
    line: EditableSaleLine,
    enabled: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onQtyInputChange: (String) -> Unit,
    onPriceInputChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = line.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                )
                IconButton(
                    onClick = onRemove,
                    enabled = enabled,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Remove ${line.name}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Unit Price
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "₹",
                        style = MonoPriceMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        BasicTextField(
                            value = line.priceInput,
                            onValueChange = onPriceInputChange,
                            enabled = enabled,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MonoPriceMedium.copy(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .width(64.dp)
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        )
                    }
                }

                // Quantity Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        enabled = enabled,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease quantity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    BasicTextField(
                        value = line.qtyInput,
                        onValueChange = onQtyInputChange,
                        enabled = enabled,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MonoPriceMedium.copy(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.width(34.dp)
                    )
                    IconButton(
                        onClick = onIncrement,
                        enabled = enabled,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase quantity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Line Total
                Text(
                    text = Formatters.formatInr(line.lineTotal),
                    style = MonoPriceMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EditPaymentPill(
    mode: PaymentMode,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .heightIn(min = 50.dp)
            .testTag("edit_sale_payment_${mode.label.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(imageVector = icon, contentDescription = mode.label, modifier = Modifier.size(16.dp))
            Text(
                text = mode.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EditSplitFieldRow(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onFillRemaining: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            label = { Text(label) },
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            modifier = Modifier.weight(1f)
        )
        Surface(
            onClick = onFillRemaining,
            enabled = enabled,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.pos_fill_remaining),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
