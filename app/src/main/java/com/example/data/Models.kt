package com.example.data

enum class PaymentMode(val label: String) {
    CASH("Cash"),
    ONLINE("Online"),
    CARD("Card"),
    SPLIT("Split");

    companion object {
        fun fromLabel(raw: String?): PaymentMode {
            val normalized = raw?.trim()?.lowercase() ?: return CASH
            return entries.firstOrNull { it.label.lowercase() == normalized || it.name.lowercase() == normalized }
                ?: CASH
        }
    }
}

data class Product(
    val slNo: Int,
    val name: String,
    val hsnCode: String,
    val stock: Int,
    val price: Double
)

data class SaleItem(
    val name: String,
    val hsnCode: String,
    val qty: Int,
    val price: Double,
    val total: Double
)

data class SaleRecord(
    val billId: String,
    val date: String,
    val customerName: String,
    val customerMobile: String = "",
    val customerAddress: String = "",
    val items: List<SaleItem>,
    val totalAmount: Double,
    val paymentMode: PaymentMode,
    val cashAmount: Double,
    val onlineAmount: Double,
    val cardAmount: Double
)

/**
 * Cart line item used exclusively on the New Sale (POS) tab.
 * Intentionally omits any stock quantity property to enforce the billing privacy rule.
 */
data class CartItem(
    val productSlNo: Int,
    val name: String,
    val hsnCode: String,
    val unitPrice: Double,
    val priceInput: String,
    val qty: Int,
    val qtyInput: String
) {
    val lineTotal: Double
        get() = unitPrice * qty
}

sealed interface SyncStatus {
    data object LocalOnly : SyncStatus
    data object Syncing : SyncStatus
    data object LiveSynced : SyncStatus
    data object BlankSheetDetected : SyncStatus
    data class Error(val message: String) : SyncStatus
}
