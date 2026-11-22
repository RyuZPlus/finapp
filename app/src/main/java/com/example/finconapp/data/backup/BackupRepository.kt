
package com.example.finconapp.data.backup

import androidx.room.withTransaction
import com.example.finconapp.data.local.database.AppDatabase
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.PendingPayment
import com.example.finconapp.data.local.entity.Transaction

class BackupRepository(
    private val database: AppDatabase
) {

    companion object {
        const val CURRENT_BACKUP_VERSION = 2
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

            val pendingPayments =
                database.pendingPaymentDao()
                    .getAllPendingPaymentsForBackup()

            BackupData(
                backupVersion = CURRENT_BACKUP_VERSION,
                exportedAt = System.currentTimeMillis(),

                // Respaldar categorías
                categories = categories.map {
                    BackupCategory(
                        id = it.id,
                        name = it.name
                    )
                },

                // Respaldar transacciones
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

                // Respaldar pagos pendientes
                pendingPayments = pendingPayments.map {
                    BackupPendingPayment(
                        id = it.id,
                        name = it.name,
                        categoryId = it.categoryId,
                        amount = it.amount,
                        paidAmount = it.paidAmount,
                        dueDate = it.dueDate,
                        reminderDate = it.reminderDate,
                        status = it.status,
                        description = it.description
                    )
                }
            )
        }
    }

    // Restaurar respaldo
    suspend fun restoreBackup(backup: BackupData) {

        validateBackup(backup)

        database.withTransaction {

            // Eliminar registros anteriores
            database.transactionDao().deleteAll()
            database.pendingPaymentDao().deleteAll()
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

            // Restaurar pagos pendientes
            database.pendingPaymentDao().insertAll(
                backup.pendingPayments.map {
                    PendingPayment(
                        id = it.id,
                        name = it.name,
                        categoryId = it.categoryId,
                        amount = it.amount,
                        paidAmount = it.paidAmount,
                        dueDate = it.dueDate,
                        reminderDate = it.reminderDate,
                        status = it.status,
                        description = it.description
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

        // Validar categorías
        val categoryIds =
            backup.categories.map { it.id }

        require(categoryIds.size == categoryIds.toSet().size) {
            "El respaldo contiene categorías duplicadas."
        }

        // Validar transacciones
        val transactionIds =
            backup.transactions.map { it.id }

        require(transactionIds.size == transactionIds.toSet().size) {
            "El respaldo contiene transacciones duplicadas."
        }

        // Validar pagos pendientes
        val pendingPaymentIds =
            backup.pendingPayments.map { it.id }

        require(
            pendingPaymentIds.size == pendingPaymentIds.toSet().size
        ) {
            "El respaldo contiene pagos pendientes duplicados."
        }

        val validCategoryIds = categoryIds.toSet()

        // Comprobar categorías de las transacciones
        require(
            backup.transactions.all {
                it.categoryId in validCategoryIds
            }
        ) {
            "Hay transacciones con categorías inexistentes."
        }

        // Comprobar categorías de los pagos pendientes
        require(
            backup.pendingPayments.all {
                it.categoryId in validCategoryIds
            }
        ) {
            "Hay pagos pendientes con categorías inexistentes."
        }

        // Validar importes de pagos pendientes
        require(
            backup.pendingPayments.all {
                it.amount > 0.0 &&
                        it.paidAmount >= 0.0 &&
                        it.paidAmount <= it.amount
            }
        ) {
            "El respaldo contiene importes de pagos pendientes inválidos."
        }
    }
}
