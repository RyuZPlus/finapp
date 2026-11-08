package com.example.finconapp.data.repository

import com.example.finconapp.data.local.dao.SavingsGoalDao
import com.example.finconapp.data.local.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

class SavingsGoalRepository(
    private val dao: SavingsGoalDao
) {
    suspend fun getAllSavingsGoalsForBackup(): List<SavingsGoal> {
        return dao.getAllSavingsGoalsForBackup()
    }

    suspend fun insertAllSavingsGoals(goals: List<SavingsGoal>) {
        dao.insertAll(goals)
    }
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>> {
        return dao.getAllSavingsGoals()
    }

    suspend fun insert(goal: SavingsGoal) {
        dao.insert(goal)
    }

    suspend fun update(goal: SavingsGoal) {
        dao.update(goal)
    }

    suspend fun delete(goal: SavingsGoal) {
        dao.delete(goal)
    }
}