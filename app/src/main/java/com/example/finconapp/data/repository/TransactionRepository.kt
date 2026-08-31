package com.example.finconapp.data.repository
import com.example.finconapp.data.local.dao.TransactionDao
import com.example.finconapp.data.local.entity.Transaction

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    fun getAllTransactions() = transactionDao.getAllTransactions()
}