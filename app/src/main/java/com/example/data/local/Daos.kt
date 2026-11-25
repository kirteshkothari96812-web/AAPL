package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY slNo ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY slNo ASC")
    suspend fun getAllOnce(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Query("SELECT MAX(slNo) FROM products")
    suspend fun maxSlNo(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoreAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(products: List<ProductEntity>) {
        clearAll()
        upsertAll(products)
    }
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAtMillis DESC, billId DESC")
    fun observeAll(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY createdAtMillis DESC, billId DESC")
    suspend fun getAllOnce(): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(sale: SaleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sales: List<SaleEntity>)

    @Query("DELETE FROM sales")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(sales: List<SaleEntity>) {
        clearAll()
        upsertAll(sales)
    }
}
