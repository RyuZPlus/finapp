
package com.example.finconapp.data.repository

import com.example.finconapp.data.local.dao.PendingPaymentDao
import com.example.finconapp.data.local.entity.PendingPayment
import kotlinx.coroutines.flow.Flow

class PendingPaymentRepository(
    private val dao: PendingPaymentDao
) {

    suspend fun registerPayment(
        payment: PendingPayment,
        amount: Double
    ): PendingPayment {
        require(amount > 0.0 && amount.isFinite()) {
            "El importe debe ser mayor que cero."
        }

        val remainingAmount = payment.amount - payment.paidAmount

        require(amount <= remainingAmount) {
            "El abono supera el saldo pendiente."
        }

        val newPaidAmount = payment.paidAmount + amount

        val updatedPayment = payment.copy(
            paidAmount = newPaidAmount,
            status = if (newPaidAmount >= payment.amount) {
                "PAID"
            } else {
                "PENDING"
            }
        )

        dao.update(updatedPayment)

        return updatedPayment
    }


    // Obtener todos los pagos para respaldos
    suspend fun getAllPendingPaymentsForBackup(): List<PendingPayment> {
        return dao.getAllPendingPaymentsForBackup()
    }

    // Restaurar pagos desde un respaldo
    suspend fun insertAllPendingPayments(
        payments: List<PendingPayment>
    ) {
        dao.insertAll(payments)
    }

    // Obtener todos los pagos pendientes
    fun getAllPendingPayments(): Flow<List<PendingPayment>> {
        return dao.getAllPendingPayments()
    }

    // Obtener pagos próximos a vencer
    fun getUpcomingPayments(
        startDate: Long,
        endDate: Long
    ): Flow<List<PendingPayment>> {
        return dao.getUpcomingPayments(startDate, endDate)
    }

    // Registrar un nuevo pago
    suspend fun insert(payment: PendingPayment): Long {
        return dao.insert(payment)
    }

    // Actualizar un pago
    suspend fun update(payment: PendingPayment) {
        dao.update(payment)
    }

    // Eliminar un pago
    suspend fun delete(payment: PendingPayment) {
        dao.delete(payment)
    }

    // Eliminar todos los pagos (restauración de respaldos)
    suspend fun deleteAll() {
        dao.deleteAll()
    }

    // Comprobar si una categoría tiene pagos asociados
    suspend fun categoryHasPendingPayments(
        categoryId: Int
    ): Boolean {
        return dao.categoryHasPendingPayments(categoryId)
    }

}
