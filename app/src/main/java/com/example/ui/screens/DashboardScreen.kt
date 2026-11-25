package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.PosUiState
import com.example.ui.theme.CardAccent
import com.example.ui.theme.CardBg
import com.example.ui.theme.CashAccent
import com.example.ui.theme.CashBg
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.MonoCodeStyle
import com.example.ui.theme.MonoPriceLarge
import com.example.ui.theme.MonoPriceMedium
import com.example.ui.theme.OnlineAccent
import com.example.ui.theme.OnlineBg
import com.example.util.Formatters

@Composable
fun DashboardScreen(
    uiState: PosUiState,
    onSelectMonth: (String) -> Unit,
    onHsnQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    val monthlySales = remember(uiState.sales, uiState.dashboardMonthKey) {
        uiState.sales.filter { Formatters.extractMonthKey(it.date) == uiState.dashboardMonthKey }
    }

    val totalMonthlyAmount = remember(monthlySales) { monthlySales.sumOf { it.totalAmount } }
    val billCount = monthlySales.size
    val cashCollected = remember(monthlySales) { monthlySales.sumOf { it.cashAmount } }
    val onlineCollected = remember(monthlySales) { monthlySales.sumOf { it.onlineAmount } }
    val cardCollected = remember(monthlySales) { monthlySales.sumOf { it.cardAmount } }

    val hsnQuery = uiState.dashboardHsnQuery.trim().lowercase()
    val lookupMatches = remember(uiState.products, hsnQuery) {
        if (hsnQuery.isEmpty()) {
            uiState.products.take(8)
        } else {
            uiState.products.filter {
                it.name.lowercase().contains(hsnQuery) || it.hsnCode.lowercase().contains(hsnQuery)
            }.take(20)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Selector Row
            item(key = "dashboard_month_picker") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_month_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Monthly Summary",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box {
                        Surface(
                            onClick = { monthDropdownExpanded = true },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("dashboard_month_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "Select month",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = Formatters.monthKeyToLabel(uiState.dashboardMonthKey),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            uiState.availableMonthKeys.forEach { monthKey ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = Formatters.monthKeyToLabel(monthKey),
                                            fontWeight = if (monthKey == uiState.dashboardMonthKey) {
                                                FontWeight.Bold
                                            } else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        monthDropdownExpanded = false
                                        onSelectMonth(monthKey)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Hero Monthly Sales Card with organic botanical banner art
            item(key = "dashboard_hero_kpi") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 152.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .testTag("dashboard_total_sales_card")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_organic_banner_1795603660990),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        ForestGreenDark.copy(alpha = 0.86f),
                                        ForestGreenDark.copy(alpha = 0.55f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = Formatters.monthKeyToLabel(uiState.dashboardMonthKey),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFE9C46A).copy(alpha = 0.24f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color(0xFFFDF0D5),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.dashboard_total_bills, billCount),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFFDF0D5)
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.dashboard_total_sales),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = Formatters.formatInr(totalMonthlyAmount),
                            style = MonoPriceLarge.copy(fontSize = 32.sp, lineHeight = 38.sp),
                            color = Color.White,
                            modifier = Modifier.testTag("dashboard_total_sales_amount")
                        )
                        if (billCount > 0) {
                            val avg = totalMonthlyAmount / billCount
                            Text(
                                text = "Avg. bill value ${Formatters.formatInr(avg)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            }

            // Payment Mode Breakdown KPI Cards (Cash / Online / Card — including Split bills)
            item(key = "dashboard_payment_kpis") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Collection by Payment Mode (incl. Split)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PaymentKpiCard(
                            title = stringResource(R.string.dashboard_cash_collected),
                            amount = cashCollected,
                            icon = Icons.Filled.Payments,
                            accentColor = CashAccent,
                            iconBgColor = CashBg,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_cash_kpi")
                        )
                        PaymentKpiCard(
                            title = stringResource(R.string.dashboard_online_collected),
                            amount = onlineCollected,
                            icon = Icons.Filled.QrCode2,
                            accentColor = OnlineAccent,
                            iconBgColor = OnlineBg,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_online_kpi")
                        )
                    }
                    PaymentKpiCard(
                        title = stringResource(R.string.dashboard_card_collected),
                        amount = cardCollected,
                        icon = Icons.Filled.CreditCard,
                        accentColor = CardAccent,
                        iconBgColor = CardBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_card_kpi")
                    )
                }
            }

            // Quick HSN & Price Lookup (NO stock displayed!)
            item(key = "dashboard_hsn_lookup_header") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.dashboard_hsn_lookup_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    OutlinedTextField(
                        value = uiState.dashboardHsnQuery,
                        onValueChange = onHsnQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_hsn_search_input"),
                        placeholder = { Text(stringResource(R.string.dashboard_hsn_lookup_hint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Lookup HSN and price",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (uiState.dashboardHsnQuery.isNotEmpty()) {
                                IconButton(onClick = { onHsnQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear lookup"
                                    )
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
            }

            item(key = "dashboard_hsn_table_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Product",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1.4f)
                            )
                            Text(
                                text = "HSN Code",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Price",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.widthIn(min = 68.dp)
                            )
                        }

                        if (lookupMatches.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching product or HSN found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            lookupMatches.forEachIndexed { idx, prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .padding(end = 8.dp)
                                    )
                                    Text(
                                        text = prod.hsnCode.ifBlank { "—" },
                                        style = MonoCodeStyle,
                                        color = if (prod.hsnCode.isBlank()) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    )
                                    Text(
                                        text = Formatters.formatInr(prod.price),
                                        style = MonoPriceMedium.copy(fontSize = 14.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.widthIn(min = 68.dp)
                                    )
                                }
                                if (idx < lookupMatches.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun PaymentKpiCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    accentColor: Color,
    iconBgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBgColor,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Formatters.formatInr(amount),
                    style = MonoPriceMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}
