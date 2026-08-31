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

    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }
}
