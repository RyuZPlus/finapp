package com.example.finconapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    val categories =
        repository
            .getAllCategories()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun update(category: Category, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {

            val exists =
                repository.categoryExistsExcept(
                    category.name,
                    category.id
                )

            if (exists) {
                onResult(false)
                return@launch
            }

            repository.update(category)

            onResult(true)
        }
    }

    fun insert(
        category: Category,
        onResult: (Boolean, Int?) -> Unit
    ) {
        viewModelScope.launch {

            val exists =
                repository.categoryExists(category.name)

            if (exists) {
                onResult(false, null)
                return@launch
            }

            val categoryId =
                repository.insertCategory(category)

            onResult(true, categoryId)
        }
    }

    fun delete(
        categoryId: Int,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {

            val success =
                repository.deleteCategoryIfEmpty(
                    categoryId
                )

            onResult(success)
        }
    }
}