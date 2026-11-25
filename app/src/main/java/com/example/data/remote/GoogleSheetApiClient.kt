package com.example.data.remote

import com.example.data.PaymentMode
import com.example.data.Product
import com.example.data.SaleItem
import com.example.data.SaleRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class SheetFetchResult(
    val isSheetEmpty: Boolean,
    val products: List<Product>,
    val sales: List<SaleRecord>
)

data class SheetPostResult(
    val success: Boolean,
    val message: String,
    val billId: String? = null
)

class GoogleSheetApiClient {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val plainTextJsonMediaType = "text/plain;charset=utf-8".toMediaType()

    fun isValidWebAppUrl(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.startsWith("https://") &&
            trimmed.length > 16 &&
            !trimmed.contains("[PASTE_YOUR_WEB_APP_URL_HERE]")
    }

    suspend fun fetchSheetData(url: String): Result<SheetFetchResult> = withContext(Dispatchers.IO) {
        runCatching {
            require(isValidWebAppUrl(url)) { "Please enter a valid HTTPS Google Apps Script Web App URL." }
            val request = Request.Builder()
                .url(url.trim())
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IOException("Server returned HTTP ${response.code}")
                }
                parseFetchResponse(body)
            }
        }
    }

    suspend fun pushInitialStock(
        url: String,
        products: List<Product>,
        forceOverwrite: Boolean = false
    ): Result<SheetPostResult> {
        val productsArr = JSONArray()
        products.forEach { p ->
            productsArr.put(
                JSONObject().apply {
                    put("slNo", p.slNo)
                    put("name", p.name)
                    put("hsnCode", p.hsnCode)
                    put("stock", p.stock)
                    put("price", p.price)
                }
            )
        }
        val payload = JSONObject().apply {
            put("action", "PUSH_INITIAL_STOCK")
            put("products", productsArr)
            put("forceOverwrite", forceOverwrite)
        }
        return postAction(url, payload)
    }

    suspend fun addSale(url: String, sale: SaleRecord): Result<SheetPostResult> {
        val itemsArr = JSONArray()
        sale.items.forEach { item ->
            itemsArr.put(
                JSONObject().apply {
                    put("name", item.name)
                    put("hsnCode", item.hsnCode)
                    put("qty", item.qty)
                    put("price", item.price)
                    put("total", item.total)
                }
            )
        }
        val payload = JSONObject().apply {
            put("action", "ADD_SALE")
            put("date", sale.date)
            put("customerName", sale.customerName)
            put("customerMobile", sale.customerMobile)
            put("customerAddress", sale.customerAddress)
            put("items", itemsArr)
            put("totalAmount", sale.totalAmount)
            put("paymentMode", sale.paymentMode.label)
            put("cashAmount", sale.cashAmount)
            put("onlineAmount", sale.onlineAmount)
            put("cardAmount", sale.cardAmount)
        }
        return postAction(url, payload)
    }

    suspend fun updateSale(url: String, sale: SaleRecord): Result<SheetPostResult> {
        val itemsArr = JSONArray()
        sale.items.forEach { item ->
            itemsArr.put(
                JSONObject().apply {
                    put("name", item.name)
                    put("hsnCode", item.hsnCode)
                    put("qty", item.qty)
                    put("price", item.price)
                    put("total", item.total)
                }
            )
        }
        val payload = JSONObject().apply {
            put("action", "UPDATE_SALE")
            put("billId", sale.billId)
            put("date", sale.date)
            put("customerName", sale.customerName)
            put("customerMobile", sale.customerMobile)
            put("customerAddress", sale.customerAddress)
            put("items", itemsArr)
            put("totalAmount", sale.totalAmount)
            put("paymentMode", sale.paymentMode.label)
            put("cashAmount", sale.cashAmount)
            put("onlineAmount", sale.onlineAmount)
            put("cardAmount", sale.cardAmount)
        }
        return postAction(url, payload)
    }

    suspend fun updateProduct(url: String, product: Product): Result<SheetPostResult> {
        val payload = JSONObject().apply {
            put("action", "UPDATE_PRODUCT")
            put("slNo", product.slNo)
            put("name", product.name)
            put("hsnCode", product.hsnCode)
            put("stock", product.stock)
            put("price", product.price)
        }
        return postAction(url, payload)
    }

    suspend fun addProduct(url: String, product: Product): Result<SheetPostResult> {
        val payload = JSONObject().apply {
            put("action", "ADD_PRODUCT")
            put("slNo", product.slNo)
            put("name", product.name)
            put("hsnCode", product.hsnCode)
            put("stock", product.stock)
            put("price", product.price)
        }
        return postAction(url, payload)
    }

    private suspend fun postAction(url: String, payload: JSONObject): Result<SheetPostResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(isValidWebAppUrl(url)) { "Google Apps Script URL is not configured." }
                val requestBody = payload.toString().toRequestBody(plainTextJsonMediaType)
                val request = Request.Builder()
                    .url(url.trim())
                    .header("Content-Type", "text/plain;charset=utf-8")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IOException("Sync failed (HTTP ${response.code})")
                    }
                    parsePostResponse(body)
                }
            }
        }

    private fun parseFetchResponse(rawBody: String): SheetFetchResult {
        val trimmed = rawBody.trim()
        if (!trimmed.startsWith("{")) {
            throw IOException("Unexpected response from Google Apps Script. Make sure the Web App is deployed with 'Anyone' access.")
        }
        val root = JSONObject(trimmed)
        val status = root.optString("status", "success")
        if (status.equals("error", ignoreCase = true)) {
            throw IOException(root.optString("message", "Google Sheet returned an error"))
        }

        val productsArr = root.optJSONArray("products") ?: JSONArray()
        val products = mutableListOf<Product>()
        for (i in 0 until productsArr.length()) {
            val obj = productsArr.optJSONObject(i) ?: continue
            val name = obj.optString("name", obj.optString("itemName", "")).trim()
            if (name.isEmpty()) continue
            val slNo = obj.optInt("slNo", i + 1)
            val hsnCode = obj.optString("hsnCode", obj.optString("hsn", "")).let {
                if (it == "null") "" else it.trim()
            }
            val stock = parseFlexibleInt(obj.opt("stock"))
            val price = parseFlexibleDouble(obj.opt("price"))
            products.add(
                Product(
                    slNo = slNo,
                    name = name,
                    hsnCode = hsnCode,
                    stock = stock,
                    price = price
                )
            )
        }

        val salesArr = root.optJSONArray("sales") ?: JSONArray()
        val sales = mutableListOf<SaleRecord>()
        for (i in 0 until salesArr.length()) {
            val obj = salesArr.optJSONObject(i) ?: continue
            val billId = obj.optString("billId", obj.optString("id", "BILL-${i + 1}")).trim()
            val date = obj.optString("date", "").trim()
            val customer = obj.optString("customerName", "Walk-in Customer").ifBlank { "Walk-in Customer" }
            val customerMobile = obj.optString("customerMobile", obj.optString("mobile", "")).let {
                if (it == "null") "" else it.trim()
            }
            val customerAddress = obj.optString("customerAddress", obj.optString("address", "")).let {
                if (it == "null") "" else it.trim()
            }
            val items = parseSaleItemsField(obj.opt("items"))
            val totalAmount = parseFlexibleDouble(obj.opt("totalAmount")).let {
                if (it > 0.0) it else items.sumOf { item -> item.total }
            }
            val mode = PaymentMode.fromLabel(obj.optString("paymentMode", "Cash"))
            var cashAmount = parseFlexibleDouble(obj.opt("cashAmount"))
            var onlineAmount = parseFlexibleDouble(obj.opt("onlineAmount"))
            var cardAmount = parseFlexibleDouble(obj.opt("cardAmount"))
            if (cashAmount == 0.0 && onlineAmount == 0.0 && cardAmount == 0.0 && totalAmount > 0.0) {
                when (mode) {
                    PaymentMode.CASH -> cashAmount = totalAmount
                    PaymentMode.ONLINE -> onlineAmount = totalAmount
                    PaymentMode.CARD -> cardAmount = totalAmount
                    PaymentMode.SPLIT -> Unit
                }
            }
            sales.add(
                SaleRecord(
                    billId = billId,
                    date = date,
                    customerName = customer,
                    customerMobile = customerMobile,
                    customerAddress = customerAddress,
                    items = items,
                    totalAmount = totalAmount,
                    paymentMode = mode,
                    cashAmount = cashAmount,
                    onlineAmount = onlineAmount,
                    cardAmount = cardAmount
                )
            )
        }

        val isSheetEmpty = root.optBoolean("isSheetEmpty", false) || products.isEmpty()
        return SheetFetchResult(
            isSheetEmpty = isSheetEmpty,
            products = products,
            sales = sales
        )
    }

    private fun parsePostResponse(rawBody: String): SheetPostResult {
        val trimmed = rawBody.trim()
        if (!trimmed.startsWith("{")) {
            return SheetPostResult(success = true, message = "Synced")
        }
        val root = JSONObject(trimmed)
        val status = root.optString("status", "success")
        val isSuccess = !status.equals("error", ignoreCase = true)
        val message = root.optString("message", if (isSuccess) "Synced successfully" else "Sync failed")
        val billId = root.optString("billId", "").takeIf { it.isNotBlank() && it != "null" }
        if (!isSuccess) {
            throw IOException(message)
        }
        return SheetPostResult(success = true, message = message, billId = billId)
    }

    private fun parseSaleItemsField(raw: Any?): List<SaleItem> {
        val arr = when (raw) {
            is JSONArray -> raw
            is String -> runCatching { JSONArray(raw) }.getOrNull()
            else -> null
        } ?: return emptyList()

        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val name = obj.optString("name", "").trim()
                if (name.isEmpty()) continue
                val hsnCode = obj.optString("hsnCode", "").let { if (it == "null") "" else it.trim() }
                val qty = parseFlexibleInt(obj.opt("qty")).coerceAtLeast(1)
                val price = parseFlexibleDouble(obj.opt("price"))
                val total = parseFlexibleDouble(obj.opt("total")).let { if (it > 0.0) it else qty * price }
                add(SaleItem(name = name, hsnCode = hsnCode, qty = qty, price = price, total = total))
            }
        }
    }

    private fun parseFlexibleInt(raw: Any?): Int = when (raw) {
        is Number -> raw.toInt()
        is String -> raw.trim().toDoubleOrNull()?.toInt() ?: 0
        else -> 0
    }

    private fun parseFlexibleDouble(raw: Any?): Double = when (raw) {
        is Number -> raw.toDouble()
        is String -> raw.trim().replace(",", "").replace("₹", "").toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}
