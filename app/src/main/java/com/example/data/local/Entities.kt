package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.PaymentMode
import com.example.data.Product
import com.example.data.SaleItem
import com.example.data.SaleRecord
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val slNo: Int,
    val name: String,
    val hsnCode: String,
    val stock: Int,
    val price: Double
) {
    fun toDomain(): Product = Product(
        slNo = slNo,
        name = name,
        hsnCode = hsnCode,
        stock = stock,
        price = price
    )

    companion object {
        fun fromDomain(product: Product): ProductEntity = ProductEntity(
            slNo = product.slNo,
            name = product.name,
            hsnCode = product.hsnCode,
            stock = product.stock,
            price = product.price
        )
    }
}

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val billId: String,
    val date: String,
    val customerName: String,
    val customerMobile: String = "",
    val customerAddress: String = "",
    val itemsJson: String,
    val totalAmount: Double,
    val paymentMode: String,
    val cashAmount: Double,
    val onlineAmount: Double,
    val cardAmount: Double,
    val createdAtMillis: Long
) {
    fun toDomain(): SaleRecord = SaleRecord(
        billId = billId,
        date = date,
        customerName = customerName,
        customerMobile = customerMobile,
        customerAddress = customerAddress,
        items = deserializeSaleItems(itemsJson),
        totalAmount = totalAmount,
        paymentMode = PaymentMode.fromLabel(paymentMode),
        cashAmount = cashAmount,
        onlineAmount = onlineAmount,
        cardAmount = cardAmount
    )

    companion object {
        fun fromDomain(sale: SaleRecord, createdAtMillis: Long = System.currentTimeMillis()): SaleEntity =
            SaleEntity(
                billId = sale.billId,
                date = sale.date,
                customerName = sale.customerName,
                customerMobile = sale.customerMobile,
                customerAddress = sale.customerAddress,
                itemsJson = serializeSaleItems(sale.items),
                totalAmount = sale.totalAmount,
                paymentMode = sale.paymentMode.label,
                cashAmount = sale.cashAmount,
                onlineAmount = sale.onlineAmount,
                cardAmount = sale.cardAmount,
                createdAtMillis = createdAtMillis
            )

        fun serializeSaleItems(items: List<SaleItem>): String {
            val arr = JSONArray()
            items.forEach { item ->
                arr.put(
                    JSONObject().apply {
                        put("name", item.name)
                        put("hsnCode", item.hsnCode)
                        put("qty", item.qty)
                        put("price", item.price)
                        put("total", item.total)
                    }
                )
            }
            return arr.toString()
        }

        fun deserializeSaleItems(raw: String): List<SaleItem> {
            return runCatching {
                val arr = JSONArray(raw)
                buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        add(
                            SaleItem(
                                name = obj.optString("name", ""),
                                hsnCode = obj.optString("hsnCode", ""),
                                qty = obj.optInt("qty", 1),
                                price = obj.optDouble("price", 0.0).takeIf { !it.isNaN() } ?: 0.0,
                                total = obj.optDouble("total", 0.0).takeIf { !it.isNaN() } ?: 0.0
                            )
                        )
                    }
                }
            }.getOrDefault(emptyList())
        }
    }
}
