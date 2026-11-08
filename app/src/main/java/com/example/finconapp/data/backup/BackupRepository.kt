
package com.example.finconapp.data.backup

import androidx.room.withTransaction
import com.example.finconapp.data.local.database.AppDatabase
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.SavingsGoal
import com.example.finconapp.data.local.entity.Transaction

class BackupRepository(
    private val database: AppDatabase
) {

    companion object {
        const val CURRENT_BACKUP_VERSION = 1
    }

    // Crear objeto de respaldo
    suspend fun createBackup(): BackupData {

        return database.withTransaction {

            val categories =
                database.categoryDao()
                    .getAllCategoriesForBackup()

            val transactions =
                database.transactionDao()
                    .getAllTransactionsForBackup()

            val savingsGoals =
                database.savingsGoalDao()
                    .getAllSavingsGoalsForBackup()

            BackupData(
                backupVersion = CURRENT_BACKUP_VERSION,
                exportedAt = System.currentTimeMillis(),

                categories = categories.map {
                    BackupCategory(
                        id = it.id,
                        name = it.name
                    )
                },

                transactions = transactions.map {
                    BackupTransaction(
                        id = it.id,
                        title = it.title,
                        amount = it.amount,
                        description = it.description,
                        type = it.type,
                        categoryId = it.categoryId,
                        subcategory = it.subcategory,
                        date = it.date
                    )
                },

                savingsGoals = savingsGoals.map {
                    BackupSavingsGoal(
                        id = it.id,
                        name = it.name,
                        categoryId = it.categoryId,
                        targetAmount = it.targetAmount,
                        initialAmount = it.initialAmount,
                        deadline = it.deadline
                    )
                }
            )
        }
    }

    // Restaurar respaldo
    suspend fun restoreBackup(backup: BackupData) {

        validateBackup(backup)

        database.withTransaction {

            database.transactionDao().deleteAll()
            database.savingsGoalDao().deleteAll()
            database.categoryDao().deleteAll()

            // Restaurar categorías
            database.categoryDao().insertAll(
                backup.categories.map {
                    Category(
                        id = it.id,
                        name = it.name
                    )
                }
            )

            // Restaurar metas
            database.savingsGoalDao().insertAll(
                backup.savingsGoals.map {
                    SavingsGoal(
                        id = it.id,
                        name = it.name,
                        categoryId = it.categoryId,
                        targetAmount = it.targetAmount,
                        initialAmount = it.initialAmount,
                        deadline = it.deadline
                    )
                }
            )

            // Restaurar transacciones
            database.transactionDao().insertAll(
                backup.transactions.map {
                    Transaction(
                        id = it.id,
                        title = it.title,
                        amount = it.amount,
                        description = it.description,
                        type = it.type,
                        categoryId = it.categoryId,
                        subcategory = it.subcategory,
                        date = it.date
                    )
                }
            )
        }
    }

    // Validar respaldo
    fun validateBackup(backup: BackupData) {

        require(
            backup.backupVersion == CURRENT_BACKUP_VERSION
        ) {
            "Versión de respaldo no compatible."
        }

        val categoryIds =
            backup.categories.map { it.id }

        require(categoryIds.size == categoryIds.toSet().size) {
            "El respaldo contiene categorías duplicadas."
        }

        val transactionIds =
            backup.transactions.map { it.id }

        require(transactionIds.size == transactionIds.toSet().size) {
            "El respaldo contiene transacciones duplicadas."
        }

        val savingsGoalIds =
            backup.savingsGoals.map { it.id }

        require(savingsGoalIds.size == savingsGoalIds.toSet().size) {
            "El respaldo contiene metas duplicadas."
        }

        val validCategoryIds = categoryIds.toSet()

        require(
            backup.transactions.all {
                it.categoryId in validCategoryIds
            }
        ) {
            "Hay transacciones con categorías inexistentes."
        }

        require(
            backup.savingsGoals.all {
                it.categoryId in validCategoryIds
            }
        ) {
            "Hay metas de ahorro con categorías inexistentes."
        }
    }
}
