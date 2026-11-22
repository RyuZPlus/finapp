package com.example.finconapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.finconapp.data.local.dao.CategoryDao
import com.example.finconapp.data.local.dao.PendingPaymentDao
import com.example.finconapp.data.local.dao.TransactionDao
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.Transaction
import com.example.finconapp.data.local.entity.PendingPayment

@Database(
    entities = [
        Transaction::class,
        Category::class,
        PendingPayment::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun pendingPaymentDao(): PendingPaymentDao
}