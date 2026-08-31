package com.example.finconapp.data.local.dao

import androidx.room.*
import com.example.finconapp.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

//Operaciones Crud
@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    //Se genera la lista
    fun getAllTransactions(): Flow<List<Transaction>>

    @Delete
    suspend fun delete(transaction: Transaction)
}