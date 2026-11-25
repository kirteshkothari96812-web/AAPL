package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartItem
import com.example.data.PaymentMode
import com.example.data.PosRepository
import com.example.data.Product
import com.example.data.SaleRecord
import com.example.data.SyncStatus
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
import com.example.util.Formatters
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class AppTab(val route: String) {
    NEW_SALE("new_sale"),
    DASHBOARD("dashboard"),
    LEDGER("ledger"),
    STOCK("stock")
}

enum class StockFilter(val label: String) {
    ALL("All"),
    OUT_OF_STOCK("Out of Stock"),
    LOW_STOCK("Low (1–5)"),
    HEALTHY("In Stock (>5)")
}

data class SplitPaymentState(
    val cashInput: String = "",
    val onlineInput: String = "",
    val cardInput: String = ""
) {
    val cashAmount: Double get() = cashInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val onlineAmount: Double get() = onlineInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val cardAmount: Double get() = cardInput.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    val allocatedTotal: Double get() = cashAmount + onlineAmount + cardAmount
}

private data class WorkingUiState(
    val activeTab: AppTab = AppTab.NEW_SALE,
    val syncStatus: SyncStatus = SyncStatus.LocalOnly,
    val isBusyAction: Boolean = false,
    // Tab 1: POS
    val posSearchQuery: String = "",
    val cart: List<CartItem> = emptyList(),
    val customerName: String = "",
    val customerMobile: String = "",
    val customerAddress: String = "",
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val splitState: SplitPaymentState = SplitPaymentState(),
    // Tab 2: Dashboard
    val dashboardMonthKey: String = Formatters.currentMonthKey(),
    val dashboardHsnQuery: String = "",
    // Tab 3: Ledger
    val ledgerMonthKey: String = Formatters.currentMonthKey(),
    val ledgerPaymentFilter: PaymentMode? = null,
    val ledgerSearchQuery: String = "",
    val editingSale: SaleRecord? = null,
    // Tab 4: Stock
    val stockSearchQuery: String = "",
    val stockFilter: StockFilter = StockFilter.ALL,
    val editingProduct: Product? = null,
    val showAddProductModal: Boolean = false,
    // Modals & banners
    val showSettingsModal: Boolean = false,
    val dismissedNoUrlBanner: Boolean = false
)

data class PosUiState(
    val activeTab: AppTab = AppTab.NEW_SALE,
    val products: List<Product> = emptyList(),
    val sales: List<SaleRecord> = emptyList(),
    val webAppUrl: String = "",
    val syncStatus: SyncStatus = SyncStatus.LocalOnly,
    val isBusyAction: Boolean = false,
    // Tab 1
    val posSearchQuery: String = "",
    val cart: List<CartItem> = emptyList(),
    val customerName: String = "",
    val customerMobile: String = "",
    val customerAddress: String = "",
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val splitState: SplitPaymentState = SplitPaymentState(),
    // Tab 2
    val dashboardMonthKey: String = Formatters.currentMonthKey(),
    val dashboardHsnQuery: String = "",
    // Tab 3
    val ledgerMonthKey: String = Formatters.currentMonthKey(),
    val ledgerPaymentFilter: PaymentMode? = null,
    val ledgerSearchQuery: String = "",
    val editingSale: SaleRecord? = null,
    // Tab 4
    val stockSearchQuery: String = "",
    val stockFilter: StockFilter = StockFilter.ALL,
    val editingProduct: Product? = null,
    val showAddProductModal: Boolean = false,
    // Settings & Banners
    val showSettingsModal: Boolean = false,
    val dismissedNoUrlBanner: Boolean = false
) {
    val grandTotal: Double
        get() = cart.sumOf { it.lineTotal }

    val splitRemaining: Double
        get() = grandTotal - splitState.allocatedTotal

    val isSplitBalanced: Boolean
        get() = abs(splitRemaining) < 0.01

    val canSaveSale: Boolean
        get() = !isBusyAction &&
            cart.isNotEmpty() &&
            cart.all { it.qty > 0 && it.unitPrice >= 0.0 } &&
            (paymentMode != PaymentMode.SPLIT || isSplitBalanced)

    val availableMonthKeys: List<String>
        get() {
            val keys = linkedSetOf(Formatters.currentMonthKey())
            sales.forEach { sale ->
                keys.add(Formatters.extractMonthKey(sale.date))
            }
            return keys.sortedDescending()
        }
}

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PosRepository(
        database = AppDatabase.getInstance(application),
        settingsDataStore = SettingsDataStore(application)
    )

    private val workingState = MutableStateFlow(WorkingUiState())

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    val uiState: StateFlow<PosUiState> = combine(
        repository.productsFlow,
        repository.salesFlow,
        repository.webAppUrlFlow,
        workingState
    ) { products, sales, webAppUrl, working ->
        PosUiState(
            activeTab = working.activeTab,
            products = products,
            sales = sales,
            webAppUrl = webAppUrl,
            syncStatus = working.syncStatus,
            isBusyAction = working.isBusyAction,
            posSearchQuery = working.posSearchQuery,
            cart = working.cart,
            customerName = working.customerName,
            customerMobile = working.customerMobile,
            customerAddress = working.customerAddress,
            paymentMode = working.paymentMode,
            splitState = working.splitState,
            dashboardMonthKey = working.dashboardMonthKey,
            dashboardHsnQuery = working.dashboardHsnQuery,
            ledgerMonthKey = working.ledgerMonthKey,
            ledgerPaymentFilter = working.ledgerPaymentFilter,
            ledgerSearchQuery = working.ledgerSearchQuery,
            editingSale = working.editingSale,
            stockSearchQuery = working.stockSearchQuery,
            stockFilter = working.stockFilter,
            editingProduct = working.editingProduct,
            showAddProductModal = working.showAddProductModal,
            showSettingsModal = working.showSettingsModal,
            dismissedNoUrlBanner = working.dismissedNoUrlBanner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PosUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            refreshFromSheet(showSuccessToast = false)
        }
    }

    fun selectTab(tab: AppTab) {
        workingState.update { it.copy(activeTab = tab) }
    }

    fun openSettings() {
        workingState.update { it.copy(showSettingsModal = true) }
    }

    fun closeSettings() {
        workingState.update { it.copy(showSettingsModal = false) }
    }

    fun dismissNoUrlBanner() {
        workingState.update { it.copy(dismissedNoUrlBanner = true) }
    }

    fun saveWebAppUrlAndSync(newUrl: String) {
        viewModelScope.launch {
            repository.saveWebAppUrl(newUrl)
            workingState.update { it.copy(showSettingsModal = false, dismissedNoUrlBanner = false) }
            refreshFromSheet(showSuccessToast = true)
        }
    }

    fun refreshFromSheet(showSuccessToast: Boolean = true) {
        viewModelScope.launch {
            val hasUrl = repository.hasValidSheetUrl()
            if (!hasUrl) {
                workingState.update { it.copy(syncStatus = SyncStatus.LocalOnly) }
                if (showSuccessToast) {
                    _toastEvents.tryEmit("Running in Local Mode — add your Google Apps Script URL in Settings to sync.")
                }
                return@launch
            }
            workingState.update { it.copy(syncStatus = SyncStatus.Syncing) }
            val resultStatus = repository.syncFromSheet()
            workingState.update { it.copy(syncStatus = resultStatus) }
            when (resultStatus) {
                is SyncStatus.LiveSynced -> {
                    if (showSuccessToast) _toastEvents.tryEmit("Synced with Google Sheet")
                }
                is SyncStatus.BlankSheetDetected -> {
                    if (showSuccessToast) {
                        _toastEvents.tryEmit("Blank Google Sheet detected — tap the top banner to push products")
                    }
                }
                is SyncStatus.Error -> {
                    _toastEvents.tryEmit("Sync error: ${resultStatus.message}")
                }
                else -> Unit
            }
        }
    }

    fun pushInitialStockToSheet(forceOverwrite: Boolean = false) {
        viewModelScope.launch {
            workingState.update { it.copy(isBusyAction = true) }
            val result = repository.pushInitialStockToSheet(forceOverwrite = forceOverwrite)
            result.fold(
                onSuccess = { count ->
                    _toastEvents.tryEmit("$count products synced to Google Sheet!")
                    workingState.update { it.copy(isBusyAction = false, showSettingsModal = false) }
                    refreshFromSheet(showSuccessToast = false)
                },
                onFailure = { error ->
                    workingState.update { it.copy(isBusyAction = false) }
                    _toastEvents.tryEmit(error.message ?: "Failed to push stock to Google Sheet")
                }
            )
        }
    }

    fun importProductsJson(rawJson: String) {
        viewModelScope.launch {
            workingState.update { it.copy(isBusyAction = true) }
            val result = repository.importCatalogFromJson(rawJson)
            result.fold(
                onSuccess = { count ->
                    workingState.update { it.copy(isBusyAction = false) }
                    _toastEvents.tryEmit("Imported $count products into catalog!")
                },
                onFailure = { err ->
                    workingState.update { it.copy(isBusyAction = false) }
                    _toastEvents.tryEmit("Invalid JSON: ${err.message ?: "Check format"}")
                }
            )
        }
    }

    // ---- TAB 1: NEW SALE (POS) ----

    fun onPosSearchQueryChange(query: String) {
        workingState.update { it.copy(posSearchQuery = query) }
    }

    fun addProductToCart(product: Product) {
        workingState.update { state ->
            val existingIdx = state.cart.indexOfFirst { it.productSlNo == product.slNo }
            val updatedCart = if (existingIdx >= 0) {
                state.cart.mapIndexed { idx, item ->
                    if (idx == existingIdx) {
                        val nextQty = item.qty + 1
                        item.copy(qty = nextQty, qtyInput = nextQty.toString())
                    } else item
                }
            } else {
                state.cart + CartItem(
                    productSlNo = product.slNo,
                    name = product.name,
                    hsnCode = product.hsnCode,
                    unitPrice = product.price,
                    priceInput = Formatters.formatNumericPlain(product.price),
                    qty = 1,
                    qtyInput = "1"
                )
            }
            state.copy(cart = updatedCart, posSearchQuery = "")
        }
    }

    fun incrementCartItemQty(productSlNo: Int) {
        workingState.update { state ->
            state.copy(
                cart = state.cart.map { item ->
                    if (item.productSlNo == productSlNo) {
                        val next = item.qty + 1
                        item.copy(qty = next, qtyInput = next.toString())
                    } else item
                }
            )
        }
    }

    fun decrementCartItemQty(productSlNo: Int) {
        workingState.update { state ->
            val updated = state.cart.mapNotNull { item ->
                if (item.productSlNo == productSlNo) {
                    val next = item.qty - 1
                    if (next <= 0) null else item.copy(qty = next, qtyInput = next.toString())
                } else item
            }
            state.copy(cart = updated)
        }
    }

    fun updateCartItemQtyInput(productSlNo: Int, input: String) {
        val cleaned = input.filter { it.isDigit() }.take(5)
        val parsed = cleaned.toIntOrNull() ?: 0
        workingState.update { state ->
            state.copy(
                cart = state.cart.map { item ->
                    if (item.productSlNo == productSlNo) {
                        item.copy(qtyInput = cleaned, qty = parsed)
                    } else item
                }
            )
        }
    }

    fun updateCartItemPriceInput(productSlNo: Int, input: String) {
        val cleaned = sanitizeDecimalInput(input)
        val parsed = cleaned.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
        workingState.update { state ->
            state.copy(
                cart = state.cart.map { item ->
                    if (item.productSlNo == productSlNo) {
                        item.copy(priceInput = cleaned, unitPrice = parsed)
                    } else item
                }
            )
        }
    }

    fun removeCartItem(productSlNo: Int) {
        workingState.update { state ->
            state.copy(cart = state.cart.filterNot { it.productSlNo == productSlNo })
        }
    }

    fun clearCart() {
        workingState.update {
            it.copy(
                cart = emptyList(),
                customerName = "",
                customerMobile = "",
                customerAddress = "",
                paymentMode = PaymentMode.CASH,
                splitState = SplitPaymentState()
            )
        }
    }

    fun onCustomerNameChange(name: String) {
        workingState.update { it.copy(customerName = name) }
    }

    fun onCustomerMobileChange(mobile: String) {
        val cleaned = mobile.filter { it.isDigit() }.take(10)
        workingState.update { it.copy(customerMobile = cleaned) }
    }

    fun onCustomerAddressChange(address: String) {
        workingState.update { it.copy(customerAddress = address) }
    }

    fun selectPaymentMode(mode: PaymentMode) {
        workingState.update { state ->
            state.copy(
                paymentMode = mode,
                splitState = if (mode == PaymentMode.SPLIT) state.splitState else SplitPaymentState()
            )
        }
    }

    fun updateSplitCash(input: String) {
        val cleaned = sanitizeDecimalInput(input)
        workingState.update { it.copy(splitState = it.splitState.copy(cashInput = cleaned)) }
    }

    fun updateSplitOnline(input: String) {
        val cleaned = sanitizeDecimalInput(input)
        workingState.update { it.copy(splitState = it.splitState.copy(onlineInput = cleaned)) }
    }

    fun updateSplitCard(input: String) {
        val cleaned = sanitizeDecimalInput(input)
        workingState.update { it.copy(splitState = it.splitState.copy(cardInput = cleaned)) }
    }

    fun fillSplitRemaining(target: PaymentMode) {
        workingState.update { state ->
            val grandTotal = state.cart.sumOf { it.lineTotal }
            val current = state.splitState
            val updated = when (target) {
                PaymentMode.CASH -> {
                    val rem = (grandTotal - current.onlineAmount - current.cardAmount).coerceAtLeast(0.0)
                    current.copy(cashInput = Formatters.formatNumericPlain(rem))
                }
                PaymentMode.ONLINE -> {
                    val rem = (grandTotal - current.cashAmount - current.cardAmount).coerceAtLeast(0.0)
                    current.copy(onlineInput = Formatters.formatNumericPlain(rem))
                }
                PaymentMode.CARD -> {
                    val rem = (grandTotal - current.cashAmount - current.onlineAmount).coerceAtLeast(0.0)
                    current.copy(cardInput = Formatters.formatNumericPlain(rem))
                }
                PaymentMode.SPLIT -> current
            }
            state.copy(splitState = updated)
        }
    }

    fun saveSale() {
        val snapshot = uiState.value
        if (!snapshot.canSaveSale) return

        val total = snapshot.grandTotal
        val (cashAmt, onlineAmt, cardAmt) = when (snapshot.paymentMode) {
            PaymentMode.CASH -> Triple(total, 0.0, 0.0)
            PaymentMode.ONLINE -> Triple(0.0, total, 0.0)
            PaymentMode.CARD -> Triple(0.0, 0.0, total)
            PaymentMode.SPLIT -> Triple(
                snapshot.splitState.cashAmount,
                snapshot.splitState.onlineAmount,
                snapshot.splitState.cardAmount
            )
        }

        viewModelScope.launch {
            workingState.update { it.copy(isBusyAction = true) }
            val result = repository.recordSale(
                customerName = snapshot.customerName,
                customerMobile = snapshot.customerMobile,
                customerAddress = snapshot.customerAddress,
                cartItems = snapshot.cart,
                paymentMode = snapshot.paymentMode,
                cashAmount = cashAmt,
                onlineAmount = onlineAmt,
                cardAmount = cardAmt
            )
            result.fold(
                onSuccess = { (savedSale, syncedRemotely) ->
                    workingState.update {
                        it.copy(
                            isBusyAction = false,
                            cart = emptyList(),
                            customerName = "",
                            customerMobile = "",
                            customerAddress = "",
                            paymentMode = PaymentMode.CASH,
                            splitState = SplitPaymentState(),
                            posSearchQuery = ""
                        )
                    }
                    val message = if (syncedRemotely) {
                        "Sale saved! Bill ID: ${savedSale.billId}"
                    } else {
                        "Sale saved locally! Bill ID: ${savedSale.billId}"
                    }
                    _toastEvents.tryEmit(message)
                    if (syncedRemotely) {
                        refreshFromSheet(showSuccessToast = false)
                    }
                },
                onFailure = { err ->
                    workingState.update { it.copy(isBusyAction = false) }
                    _toastEvents.tryEmit("Could not save sale: ${err.message}")
                }
            )
        }
    }

    // ---- TAB 2: DASHBOARD ----

    fun selectDashboardMonth(monthKey: String) {
        workingState.update { it.copy(dashboardMonthKey = monthKey) }
    }

    fun onDashboardHsnQueryChange(query: String) {
        workingState.update { it.copy(dashboardHsnQuery = query) }
    }

    // ---- TAB 3: LEDGER ----

    fun selectLedgerMonth(monthKey: String) {
        workingState.update { it.copy(ledgerMonthKey = monthKey) }
    }

    fun selectLedgerPaymentFilter(mode: PaymentMode?) {
        workingState.update { it.copy(ledgerPaymentFilter = mode) }
    }

    fun onLedgerSearchQueryChange(query: String) {
        workingState.update { it.copy(ledgerSearchQuery = query) }
    }

    fun openEditSale(sale: SaleRecord) {
        workingState.update { it.copy(editingSale = sale) }
    }

    fun closeEditSale() {
        workingState.update { it.copy(editingSale = null) }
    }

    fun saveEditedSale(original: SaleRecord, updated: SaleRecord) {
        viewModelScope.launch {
            workingState.update { it.copy(isBusyAction = true) }
            val result = repository.updateSale(original, updated)
            result.fold(
                onSuccess = { syncedRemotely ->
                    if (syncedRemotely) {
                        val refreshedStatus = repository.syncFromSheet()
                        workingState.update {
                            it.copy(
                                isBusyAction = false,
                                editingSale = null,
                                syncStatus = refreshedStatus
                            )
                        }
                        _toastEvents.tryEmit("Bill updated!")
                    } else {
                        workingState.update { it.copy(isBusyAction = false, editingSale = null) }
                        _toastEvents.tryEmit("Bill updated!")
                    }
                },
                onFailure = { err ->
                    workingState.update { it.copy(isBusyAction = false, editingSale = null) }
                    _toastEvents.tryEmit("Updated locally (Sheet sync failed: ${err.message})")
                }
            )
        }
    }

    // ---- TAB 4: STOCK & PRODUCTS ----

    fun onStockSearchQueryChange(query: String) {
        workingState.update { it.copy(stockSearchQuery = query) }
    }

    fun selectStockFilter(filter: StockFilter) {
        workingState.update { it.copy(stockFilter = filter) }
    }

    fun openEditProduct(product: Product) {
        workingState.update { it.copy(editingProduct = product) }
    }

    fun closeEditProduct() {
        workingState.update { it.copy(editingProduct = null) }
    }

    fun openAddProduct() {
        workingState.update { it.copy(showAddProductModal = true) }
    }

    fun closeAddProduct() {
        workingState.update { it.copy(showAddProductModal = false) }
    }

    fun saveProductUpdate(updated: Product) {
        viewModelScope.launch {
            workingState.update { it.copy(isBusyAction = true) }
            val result = repository.updateProduct(updated)
            result.fold(
                onSuccess = { synced ->
                    workingState.update { it.copy(isBusyAction = false, editingProduct = null) }
                    val msg = if (synced) "${updated.name} updated in Google Sheet" else "${updated.name} updated locally"
                    _toastEvents.tryEmit(msg)
                    if (synced) refreshFromSheet(showSuccessToast = false)
                },
                onFailure = { err ->
                    workingState.update { it.copy(isBusyAction = false, editingProduct = null) }
                    _toastEvents.tryEmit("Updated locally (Sheet sync failed: ${err.message})")
                }
            )
        }
    }

    fun createNewProduct(name: String, hsnCode: String, stock: Int, price: Double) {
        if (name.isBlank()) {
            _toastEvents.tryEmit("Product name is required")
            return
        }
        viewModelScope.launch {
            workingState.update { it.copy(isBusyAction = true) }
            val result = repository.addProduct(name, hsnCode, stock, price)
            result.fold(
                onSuccess = { (created, synced) ->
                    workingState.update { it.copy(isBusyAction = false, showAddProductModal = false) }
                    val msg = if (synced) "Added ${created.name} to Google Sheet" else "Added ${created.name} locally"
                    _toastEvents.tryEmit(msg)
                    if (synced) refreshFromSheet(showSuccessToast = false)
                },
                onFailure = { err ->
                    workingState.update { it.copy(isBusyAction = false, showAddProductModal = false) }
                    _toastEvents.tryEmit("Added locally (Sheet sync failed: ${err.message})")
                }
            )
        }
    }

    private fun sanitizeDecimalInput(raw: String): String {
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
}
