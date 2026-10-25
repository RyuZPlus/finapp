package com.example.finconapp.data.repository

import com.example.finconapp.data.local.dao.CategoryDao
import com.example.finconapp.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {

    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }

    suspend fun insertCategory(category: Category): Int {
        return categoryDao.insert(category).toInt()
    }

    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    suspend fun categoryExists(name: String): Boolean {
        return categoryDao.countByName(name) > 0
    }

    suspend fun categoryExistsExcept(
        name: String,
        categoryId: Int
    ): Boolean {
        return categoryDao.countByNameExcludingId(
            name,
            categoryId
        ) > 0
    }

    suspend fun deleteCategory(
        categoryId: Int
    ): CategoryDeleteResult {

        val transactionCount =
            categoryDao.countTransactionsByCategory(categoryId)

        if (transactionCount > 0) {
            return CategoryDeleteResult.HAS_TRANSACTIONS
        }

        val savingsGoalCount =
            categoryDao.countSavingsGoalsByCategory(categoryId)

        if (savingsGoalCount > 0) {
            return CategoryDeleteResult.HAS_SAVINGS_GOALS
        }

        val deletedRows = categoryDao.delete(categoryId)

        return if (deletedRows > 0) {
            CategoryDeleteResult.DELETED
        } else {
            CategoryDeleteResult.NOT_FOUND
        }
    }
}