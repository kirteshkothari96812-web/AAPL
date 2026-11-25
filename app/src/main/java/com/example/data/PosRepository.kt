package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.ProductEntity
import com.example.data.local.SaleEntity
import com.example.data.local.SettingsDataStore
import com.example.data.remote.GoogleSheetApiClient
import com.example.util.Formatters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class PosRepository(
    private val database: AppDatabase,
    private val settingsDataStore: SettingsDataStore,
    private val apiClient: GoogleSheetApiClient = GoogleSheetApiClient()
) {
    private val productDao = database.productDao()
    private val saleDao = database.saleDao()

    val productsFlow: Flow<List<Product>> = productDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    val salesFlow: Flow<List<SaleRecord>> = saleDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    val webAppUrlFlow: Flow<String> = settingsDataStore.webAppUrlFlow

    suspend fun ensureSeeded() {
        val existingCount = productDao.count()
        if (existingCount == 0 || existingCount == 41) {
            productDao.insertIgnoreAll(InitialProducts.ITEMS.map { ProductEntity.fromDomain(it) })
        }
    }

    suspend fun saveWebAppUrl(url: String) {
        settingsDataStore.saveWebAppUrl(url)
    }

    suspend fun hasValidSheetUrl(): Boolean {
        return apiClient.isValidWebAppUrl(settingsDataStore.getWebAppUrl())
    }

    suspend fun syncFromSheet(): SyncStatus {
        ensureSeeded()
        val url = settingsDataStore.getWebAppUrl()
        if (!apiClient.isValidWebAppUrl(url)) {
            return SyncStatus.LocalOnly
        }
        val result = apiClient.fetchSheetData(url)
        return result.fold(
            onSuccess = { fetched ->
                if (fetched.isSheetEmpty || fetched.products.isEmpty()) {
                    // Keep showing pre-loaded / local products and alert user to push to blank sheet
                    if (fetched.sales.isNotEmpty()) {
                        saveSalesLocally(fetched.sales)
                    }
                    SyncStatus.BlankSheetDetected
                } else {
                    productDao.replaceAll(fetched.products.map { ProductEntity.fromDomain(it) })
                    saveSalesLocally(fetched.sales)
                    SyncStatus.LiveSynced
                }
            },
            onFailure = { error ->
                SyncStatus.Error(error.message ?: "Unable to connect to Google Sheet")
            }
        )
    }

    suspend fun pushInitialStockToSheet(forceOverwrite: Boolean = false): Result<Int> {
        val url = settingsDataStore.getWebAppUrl()
        if (!apiClient.isValidWebAppUrl(url)) {
            return Result.failure(IllegalStateException("Please save your Google Apps Script Web App URL in Settings first."))
        }
        val productsToPush = productDao.getAllOnce().map { it.toDomain() }.ifEmpty { InitialProducts.ITEMS }
        return apiClient.pushInitialStock(url, productsToPush, forceOverwrite).map {
            productsToPush.size
        }
    }

    suspend fun importCatalogFromJson(rawJson: String): Result<Int> {
        return InitialProducts.parseProductsJson(rawJson).map { parsed ->
            productDao.replaceAll(parsed.map { ProductEntity.fromDomain(it) })
            parsed.size
        }
    }

    suspend fun recordSale(
        customerName: String,
        customerMobile: String,
        customerAddress: String,
        cartItems: List<CartItem>,
        paymentMode: PaymentMode,
        cashAmount: Double,
        onlineAmount: Double,
        cardAmount: Double
    ): Result<Pair<SaleRecord, Boolean>> {
        val now = Date()
        val dateString = Formatters.formatDateTimeForSheet(now)
        val existingSales = saleDao.getAllOnce()
        val localBillId = Formatters.generateBillId(now, existingSales.size + 1)

        val saleItems = cartItems.map { item ->
            SaleItem(
                name = item.name,
                hsnCode = item.hsnCode,
                qty = item.qty,
                price = item.unitPrice,
                total = item.lineTotal
            )
        }
        val grandTotal = saleItems.sumOf { it.total }
        val normalizedCustomer = customerName.trim().ifEmpty { "Walk-in Customer" }
        val normalizedMobile = customerMobile.filter { it.isDigit() }
        val normalizedAddress = customerAddress.trim()

        val draftSale = SaleRecord(
            billId = localBillId,
            date = dateString,
            customerName = normalizedCustomer,
            customerMobile = normalizedMobile,
            customerAddress = normalizedAddress,
            items = saleItems,
            totalAmount = grandTotal,
            paymentMode = paymentMode,
            cashAmount = cashAmount,
            onlineAmount = onlineAmount,
            cardAmount = cardAmount
        )

        // Optimistically deduct inventory in local Room DB immediately
        val currentProducts = productDao.getAllOnce()
        val qtyBySlNo = cartItems.groupBy { it.productSlNo }.mapValues { (_, list) -> list.sumOf { it.qty } }
        val updatedProducts = currentProducts.map { prod ->
            val deducted = qtyBySlNo[prod.slNo] ?: 0
            if (deducted > 0) prod.copy(stock = prod.stock - deducted) else prod
        }
        productDao.upsertAll(updatedProducts)

        val url = settingsDataStore.getWebAppUrl()
        if (!apiClient.isValidWebAppUrl(url)) {
            saleDao.upsert(SaleEntity.fromDomain(draftSale, now.time))
            return Result.success(draftSale to false)
        }

        val remoteResult = apiClient.addSale(url, draftSale)
        return remoteResult.fold(
            onSuccess = { postResp ->
                val finalSale = draftSale.copy(billId = postResp.billId ?: localBillId)
                saleDao.upsert(SaleEntity.fromDomain(finalSale, now.time))
                Result.success(finalSale to true)
            },
            onFailure = {
                // Keep saved locally so no sale is ever lost at checkout
                saleDao.upsert(SaleEntity.fromDomain(draftSale, now.time))
                Result.success(draftSale to false)
            }
        )
    }

    suspend fun updateSale(
        original: SaleRecord,
        updated: SaleRecord
    ): Result<Boolean> {
        val normalizedUpdated = updated.copy(
            customerName = updated.customerName.trim().ifEmpty { "Walk-in Customer" },
            customerMobile = updated.customerMobile.filter { it.isDigit() },
            customerAddress = updated.customerAddress.trim(),
            totalAmount = updated.items.sumOf { it.total }
        )

        // Optimistically adjust local stock by delta (oldQty - newQty)
        val oldQtyByName = original.items.groupBy { it.name.trim().lowercase() }
            .mapValues { (_, list) -> list.sumOf { it.qty } }
        val newQtyByName = normalizedUpdated.items.groupBy { it.name.trim().lowercase() }
            .mapValues { (_, list) -> list.sumOf { it.qty } }
        val currentProducts = productDao.getAllOnce()
        val adjustedProducts = currentProducts.map { prod ->
            val key = prod.name.trim().lowercase()
            val oldQty = oldQtyByName[key] ?: 0
            val newQty = newQtyByName[key] ?: 0
            val stockDelta = oldQty - newQty
            if (stockDelta != 0) prod.copy(stock = prod.stock + stockDelta) else prod
        }
        productDao.upsertAll(adjustedProducts)

        val parsedMillis = Formatters.parseDateToMillis(normalizedUpdated.date) ?: System.currentTimeMillis()
        saleDao.upsert(SaleEntity.fromDomain(normalizedUpdated, parsedMillis))

        val url = settingsDataStore.getWebAppUrl()
        if (!apiClient.isValidWebAppUrl(url)) {
            return Result.success(false)
        }
        return apiClient.updateSale(url, normalizedUpdated).map { true }
    }

    suspend fun updateProduct(product: Product): Result<Boolean> {
        productDao.upsert(ProductEntity.fromDomain(product))
        val url = settingsDataStore.getWebAppUrl()
        if (!apiClient.isValidWebAppUrl(url)) {
            return Result.success(false)
        }
        return apiClient.updateProduct(url, product).map { true }
    }

    suspend fun addProduct(
        name: String,
        hsnCode: String,
        stock: Int,
        price: Double
    ): Result<Pair<Product, Boolean>> {
        val nextSlNo = (productDao.maxSlNo() ?: 0) + 1
        val product = Product(
            slNo = nextSlNo,
            name = name.trim(),
            hsnCode = hsnCode.trim(),
            stock = stock,
            price = price
        )
        productDao.upsert(ProductEntity.fromDomain(product))
        val url = settingsDataStore.getWebAppUrl()
        if (!apiClient.isValidWebAppUrl(url)) {
            return Result.success(product to false)
        }
        return apiClient.addProduct(url, product).map { product to true }
    }

    private suspend fun saveSalesLocally(sales: List<SaleRecord>) {
        val baseTime = System.currentTimeMillis()
        val entities = sales.mapIndexed { idx, sale ->
            val parsedMillis = Formatters.parseDateToMillis(sale.date) ?: (baseTime - idx * 1000L)
            SaleEntity.fromDomain(sale, parsedMillis)
        }
        saleDao.replaceAll(entities)
    }
}
