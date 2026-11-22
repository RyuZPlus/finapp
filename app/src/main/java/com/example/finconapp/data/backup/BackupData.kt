
package com.example.finconapp.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val backupVersion: Int,
    val exportedAt: Long,
    val transactions: List<BackupTransaction>,
    val categories: List<BackupCategory>,
    val pendingPayments: List<BackupPendingPayment>
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
data class BackupPendingPayment(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val amount: Double,
    val paidAmount: Double,
    val dueDate: Long,
    val reminderDate: Long?,
    val status: String,
    val description: String?
)
