package com.example.finconapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finconapp.data.local.database.DatabaseProvider
import com.example.finconapp.data.local.entity.SavingsGoal
import com.example.finconapp.data.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavingsGoalViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        SavingsGoalRepository(
            DatabaseProvider.provide(application)
                .savingsGoalDao()
        )

    val savingsGoals: StateFlow<List<SavingsGoal>> =
        repository
            .getAllSavingsGoals()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun insert(goal: SavingsGoal) {

        viewModelScope.launch {
            repository.insert(goal)
        }
    }

    fun update(goal: SavingsGoal) {

        viewModelScope.launch {
            repository.update(goal)
        }
    }

    fun delete(goal: SavingsGoal) {

        viewModelScope.launch {
            repository.delete(goal)
        }
    }
}