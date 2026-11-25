package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PaymentMode
import com.example.data.SaleRecord
import com.example.ui.PosUiState
import com.example.ui.components.EditSaleDialog
import com.example.ui.theme.CardAccent
import com.example.ui.theme.CardBg
import com.example.ui.theme.CashAccent
import com.example.ui.theme.CashBg
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoPriceMedium
import com.example.ui.theme.OnlineAccent
import com.example.ui.theme.OnlineBg
import com.example.ui.theme.SplitAccent
import com.example.ui.theme.SplitBg
import com.example.util.Formatters

@Composable
fun LedgerScreen(
    uiState: PosUiState,
    onSelectMonth: (String) -> Unit,
    onSelectPaymentFilter: (PaymentMode?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditSaleClick: (SaleRecord) -> Unit,
    onDismissEditSale: () -> Unit,
    onSaveEditedSale: (original: SaleRecord, updated: SaleRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    val ledgerMonthOptions = remember(uiState.availableMonthKeys) {
        listOf(Formatters.ALL_MONTHS_KEY) + uiState.availableMonthKeys
    }

    val filteredSales = remember(
        uiState.sales,
        uiState.ledgerMonthKey,
        uiState.ledgerPaymentFilter,
        uiState.ledgerSearchQuery
    ) {
        val q = uiState.ledgerSearchQuery.trim().lowercase()
        uiState.sales.filter { sale ->
            val monthMatch = uiState.ledgerMonthKey == Formatters.ALL_MONTHS_KEY ||
                Formatters.extractMonthKey(sale.date) == uiState.ledgerMonthKey
            val modeMatch = uiState.ledgerPaymentFilter == null || sale.paymentMode == uiState.ledgerPaymentFilter
            val searchMatch = q.isEmpty() ||
                sale.billId.lowercase().contains(q) ||
                sale.customerName.lowercase().contains(q) ||
                sale.customerMobile.lowercase().contains(q) ||
                sale.items.any { it.name.lowercase().contains(q) }
            monthMatch && modeMatch && searchMatch
        }
    }

    val filteredTotal = remember(filteredSales) { filteredSales.sumOf { it.totalAmount } }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Filters: Month + Search
            item(key = "ledger_filters") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sales Ledger",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Box {
                            Surface(
                                onClick = { monthDropdownExpanded = true },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("ledger_month_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = "Filter by month",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = Formatters.monthKeyToLabel(uiState.ledgerMonthKey),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = monthDropdownExpanded,
                                onDismissRequest = { monthDropdownExpanded = false }
                            ) {
                                ledgerMonthOptions.forEach { key ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = Formatters.monthKeyToLabel(key),
                                                fontWeight = if (key == uiState.ledgerMonthKey) {
                                                    FontWeight.Bold
                                                } else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            monthDropdownExpanded = false
                                            onSelectMonth(key)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.ledgerSearchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ledger_search_input"),
                        placeholder = { Text(stringResource(R.string.ledger_search_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search ledger",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (uiState.ledgerSearchQuery.isNotEmpty()) {
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

                    // Payment Mode Filter Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.ledgerPaymentFilter == null,
                                onClick = { onSelectPaymentFilter(null) },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("ledger_filter_all")
                            )
                        }
                        items(PaymentMode.entries) { mode ->
                            FilterChip(
                                selected = uiState.ledgerPaymentFilter == mode,
                                onClick = { onSelectPaymentFilter(mode) },
                                label = { Text(mode.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("ledger_filter_${mode.label.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Filtered Summary Bar
            item(key = "ledger_summary_bar") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ledger_summary_bar"),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${filteredSales.size} Matching Transaction${if (filteredSales.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = Formatters.formatInr(filteredTotal),
                            style = MonoPriceMedium.copy(fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Ledger Cards or Empty State
            if (filteredSales.isEmpty()) {
                item(key = "ledger_empty") {
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
                                .padding(32.dp),
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
                                        imageVector = Icons.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.ledger_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.ledger_empty_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredSales, key = { it.billId }) { sale ->
                    LedgerSaleCard(
                        sale = sale,
                        onEditClick = { onEditSaleClick(sale) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    uiState.editingSale?.let { targetSale ->
        EditSaleDialog(
            sale = targetSale,
            products = uiState.products,
            isBusy = uiState.isBusyAction,
            onDismiss = onDismissEditSale,
            onSave = { updated -> onSaveEditedSale(targetSale, updated) }
        )
    }
}

@Composable
private fun LedgerSaleCard(
    sale: SaleRecord,
    onEditClick: () -> Unit
) {
    val (badgeBg, badgeFg) = when (sale.paymentMode) {
        PaymentMode.CASH -> CashBg to CashAccent
        PaymentMode.ONLINE -> OnlineBg to OnlineAccent
        PaymentMode.CARD -> CardBg to CardAccent
        PaymentMode.SPLIT -> SplitBg to SplitAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ledger_card_${sale.billId}"),
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row: Bill ID + Payment Badge + Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = sale.billId,
                            style = MonoCodeStyle.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = sale.paymentMode.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = badgeFg,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = sale.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (sale.customerMobile.isNotBlank()) {
                        Text(
                            text = "Mobile: ${sale.customerMobile}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (sale.customerAddress.isNotBlank()) {
                        Text(
                            text = "Address: ${sale.customerAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = Formatters.formatDisplayDateTime(sale.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = Formatters.formatInr(sale.totalAmount),
                        style = MonoPriceMedium.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .testTag("ledger_edit_${sale.billId}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit bill ${sale.billId}",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Split payment exact breakdown
            if (sale.paymentMode == PaymentMode.SPLIT) {
                val breakdown = Formatters.formatSplitBreakdown(sale)
                if (breakdown.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SplitBg
                    ) {
                        Text(
                            text = breakdown,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SplitAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            if (sale.items.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sale.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.qty} × ${item.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = Formatters.formatInr(item.total),
                                style = MonoCodeStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
