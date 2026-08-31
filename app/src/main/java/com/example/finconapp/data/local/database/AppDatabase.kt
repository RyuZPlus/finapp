package com.example.finconapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.finconapp.data.local.dao.CategoryDao
import com.example.finconapp.data.local.dao.TransactionDao
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.Transaction

@Database(
    entities = [
        Transaction::class,
        Category::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao
}