package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.SyncStatus
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.NewSaleScreen
import com.example.ui.screens.StockScreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.HarvestOchre
import com.example.ui.theme.HoneyContainer
import com.example.ui.theme.OnHoneyContainer
import com.example.ui.theme.StockAmberBg
import com.example.ui.theme.StockAmberFg
import com.example.ui.theme.StockGreenBg
import com.example.ui.theme.StockGreenFg
import com.example.ui.theme.StockRedBg
import com.example.ui.theme.StockRedFg
import kotlinx.coroutines.delay

private data class NavDestination(
    val tab: AppTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val APP_DESTINATIONS = listOf(
    NavDestination(AppTab.NEW_SALE, R.string.nav_new_sale, Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale),
    NavDestination(AppTab.DASHBOARD, R.string.nav_dashboard, Icons.Filled.Analytics, Icons.Outlined.Analytics),
    NavDestination(AppTab.LEDGER, R.string.nav_ledger, Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    NavDestination(AppTab.STOCK, R.string.nav_stock, Icons.Filled.Inventory2, Icons.Outlined.Inventory2)
)

@Composable
fun MainApp(
    viewModel: PosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLaunchSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (showLaunchSplash) {
            delay(1100)
            showLaunchSplash = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Back gesture on secondary tabs returns to New Sale (POS)
    BackHandler(enabled = uiState.activeTab != AppTab.NEW_SALE) {
        viewModel.selectTab(AppTab.NEW_SALE)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 840.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AadiTopHeader(
                    syncStatus = uiState.syncStatus,
                    onRefreshClick = { viewModel.refreshFromSheet(showSuccessToast = true) },
                    onSettingsClick = { viewModel.openSettings() }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        tonalElevation = 2.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        APP_DESTINATIONS.forEach { dest ->
                            val selected = uiState.activeTab == dest.tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_tab_${dest.tab.route}")
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
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        Spacer(modifier = Modifier.weight(0.2f))
                        APP_DESTINATIONS.forEach { dest ->
                            val selected = uiState.activeTab == dest.tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_rail_${dest.tab.route}")
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Smart Blank Sheet Detection Banner
                    AnimatedVisibility(visible = uiState.syncStatus is SyncStatus.BlankSheetDetected) {
                        BlankSheetBanner(
                            productCount = uiState.products.size,
                            isBusy = uiState.isBusyAction,
                            onPushNow = { viewModel.pushInitialStockToSheet(forceOverwrite = false) }
                        )
                    }

                    // Helpful subtle banner when Google Apps Script URL is not yet connected
                    AnimatedVisibility(
                        visible = uiState.syncStatus is SyncStatus.LocalOnly && !uiState.dismissedNoUrlBanner
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.banner_no_url),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = { viewModel.openSettings() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Connect", style = MaterialTheme.typography.labelMedium)
                                    }
                                    IconButton(
                                        onClick = { viewModel.dismissNoUrlBanner() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Dismiss banner",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }

                    // Active Tab Screen
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (uiState.activeTab) {
                            AppTab.NEW_SALE -> NewSaleScreen(
                                uiState = uiState,
                                onSearchQueryChange = viewModel::onPosSearchQueryChange,
                                onAddProduct = viewModel::addProductToCart,
                                onIncrementQty = viewModel::incrementCartItemQty,
                                onDecrementQty = viewModel::decrementCartItemQty,
                                onQtyInputChange = viewModel::updateCartItemQtyInput,
                                onPriceInputChange = viewModel::updateCartItemPriceInput,
                                onRemoveCartItem = viewModel::removeCartItem,
                                onClearCart = viewModel::clearCart,
                                onCustomerNameChange = viewModel::onCustomerNameChange,
                                onCustomerMobileChange = viewModel::onCustomerMobileChange,
                                onCustomerAddressChange = viewModel::onCustomerAddressChange,
                                onSelectPaymentMode = viewModel::selectPaymentMode,
                                onSplitCashChange = viewModel::updateSplitCash,
                                onSplitOnlineChange = viewModel::updateSplitOnline,
                                onSplitCardChange = viewModel::updateSplitCard,
                                onFillRemaining = viewModel::fillSplitRemaining,
                                onSaveSale = viewModel::saveSale
                            )

                            AppTab.DASHBOARD -> DashboardScreen(
                                uiState = uiState,
                                onSelectMonth = viewModel::selectDashboardMonth,
                                onHsnQueryChange = viewModel::onDashboardHsnQueryChange
                            )

                            AppTab.LEDGER -> LedgerScreen(
                                uiState = uiState,
                                onSelectMonth = viewModel::selectLedgerMonth,
                                onSelectPaymentFilter = viewModel::selectLedgerPaymentFilter,
                                onSearchQueryChange = viewModel::onLedgerSearchQueryChange,
                                onEditSaleClick = viewModel::openEditSale,
                                onDismissEditSale = viewModel::closeEditSale,
                                onSaveEditedSale = viewModel::saveEditedSale
                            )

                            AppTab.STOCK -> StockScreen(
                                uiState = uiState,
                                onSearchQueryChange = viewModel::onStockSearchQueryChange,
                                onSelectFilter = viewModel::selectStockFilter,
                                onAddNewProductClick = viewModel::openAddProduct,
                                onProductClick = viewModel::openEditProduct,
                                onDismissEditModal = viewModel::closeEditProduct,
                                onSaveProductUpdate = viewModel::saveProductUpdate,
                                onDismissAddModal = viewModel::closeAddProduct,
                                onCreateProduct = viewModel::createNewProduct
                            )
                        }
                    }
                }
            }
        }
    }

    // Settings Modal
    if (uiState.showSettingsModal) {
        SettingsDialog(
            currentUrl = uiState.webAppUrl,
            productCount = uiState.products.size,
            isBusy = uiState.isBusyAction,
            onDismiss = viewModel::closeSettings,
            onSaveAndSync = viewModel::saveWebAppUrlAndSync,
            onPushInitialStock = viewModel::pushInitialStockToSheet,
            onImportJson = viewModel::importProductsJson
        )
    }

    // Branded Launch Splash Overlay when app opens
    AnimatedVisibility(
        visible = showLaunchSplash,
        exit = fadeOut(animationSpec = tween(durationMillis = 320))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .testTag("launch_splash_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_aadi_aarambh_logo),
                    contentDescription = "Aadi Aarambh Official Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(210.dp)
                )
                Text(
                    text = stringResource(R.string.company_full_name),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ForestGreen
                )
                Text(
                    text = stringResource(R.string.company_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AadiTopHeader(
    syncStatus: SyncStatus,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        color = ForestGreen,
        contentColor = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_aadi_aarambh_logo),
                        contentDescription = "Aadi Aarambh logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Private Limited • Organic POS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD8F3DC),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SyncStatusChip(syncStatus = syncStatus)

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier.testTag("header_refresh_button")
                ) {
                    if (syncStatus is SyncStatus.Syncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.action_refresh),
                            tint = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.action_settings),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncStatusChip(syncStatus: SyncStatus) {
    val (bg, dotColor, text) = when (syncStatus) {
        is SyncStatus.LiveSynced -> Triple(StockGreenBg, StockGreenFg, stringResource(R.string.status_live_synced))
        is SyncStatus.BlankSheetDetected -> Triple(StockAmberBg, StockAmberFg, stringResource(R.string.status_blank_sheet))
        is SyncStatus.Syncing -> Triple(Color.White.copy(alpha = 0.18f), Color.White, stringResource(R.string.status_syncing))
        is SyncStatus.Error -> Triple(StockRedBg, StockRedFg, stringResource(R.string.status_sync_error))
        is SyncStatus.LocalOnly -> Triple(Color.White.copy(alpha = 0.16f), Color(0xFFE9C46A), stringResource(R.string.status_local_mode))
    }
    val textColor = when (syncStatus) {
        is SyncStatus.LiveSynced -> StockGreenFg
        is SyncStatus.BlankSheetDetected -> StockAmberFg
        is SyncStatus.Error -> StockRedFg
        else -> Color.White
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = Modifier.testTag("sync_status_chip")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BlankSheetBanner(
    productCount: Int,
    isBusy: Boolean,
    onPushNow: () -> Unit
) {
    Surface(
        color = HoneyContainer,
        border = BorderStroke(1.dp, HarvestOchre.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("blank_sheet_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.banner_blank_sheet_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = OnHoneyContainer
                )
                Text(
                    text = "Showing pre-loaded catalog locally until pushed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnHoneyContainer.copy(alpha = 0.85f)
                )
            }
            Button(
                onClick = onPushNow,
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("banner_push_initial_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.banner_blank_sheet_action, productCount),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
