
package com.example.finconapp.data.local.dao

import androidx.room.*
import com.example.finconapp.data.local.entity.PendingPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPaymentDao {

    @Insert
    suspend fun insert(payment: PendingPayment)

    @Insert
    suspend fun insertAll(payments: List<PendingPayment>)

    @Update
    suspend fun update(payment: PendingPayment)

    @Delete
    suspend fun delete(payment: PendingPayment)

    @Query("SELECT * FROM pending_payments ORDER BY dueDate ASC")
    fun getAllPendingPayments(): Flow<List<PendingPayment>>

    @Query("SELECT * FROM pending_payments ORDER BY dueDate ASC")
    suspend fun getAllPendingPaymentsForBackup(): List<PendingPayment>

    @Query("DELETE FROM pending_payments")
    suspend fun deleteAll()

    @Query("""
        SELECT * FROM pending_payments
        WHERE status = 'PENDING'
        AND dueDate BETWEEN :startDate AND :endDate
        ORDER BY dueDate ASC
    """)
    fun getUpcomingPayments(
        startDate: Long,
        endDate: Long
    ): Flow<List<PendingPayment>>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM pending_payments
            WHERE categoryId = :categoryId
        )
    """)
    suspend fun categoryHasPendingPayments(
        categoryId: Int
    ): Boolean
}
