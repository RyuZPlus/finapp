package com.example.finconapp.data.local.dao

import androidx.room.*
import com.example.finconapp.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

//Operaciones Crud
@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("""
        SELECT * FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>>

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM transactions
            WHERE categoryId = :categoryId
        )
    """)
    suspend fun categoryHasTransactions(categoryId: Int): Boolean

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}