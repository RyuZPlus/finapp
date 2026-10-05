package com.example.finconapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.finconapp.data.local.dao.CategoryDao
import com.example.finconapp.data.local.dao.SavingsGoalDao
import com.example.finconapp.data.local.dao.TransactionDao
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.Transaction
import com.example.finconapp.data.local.entity.SavingsGoal

@Database(
    entities = [
        Transaction::class,
        Category::class,
        SavingsGoal::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun savingsGoalDao(): SavingsGoalDao
}