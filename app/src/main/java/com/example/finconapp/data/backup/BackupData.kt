
package com.example.finconapp.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val backupVersion: Int,
    val exportedAt: Long,
    val transactions: List<BackupTransaction>,
    val categories: List<BackupCategory>,
    val savingsGoals: List<BackupSavingsGoal>
)

@Serializable
data class BackupCategory(
    val id: Int,
    val name: String
)

@Serializable
data class BackupTransaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val description: String?,
    val type: String,
    val categoryId: Int,
    val subcategory: String?,
    val date: Long
)

@Serializable
data class BackupSavingsGoal(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val targetAmount: Double,
    val initialAmount: Double,
    val deadline: Long?
)
