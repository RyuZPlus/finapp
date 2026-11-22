
package com.example.finconapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finconapp.data.local.database.DatabaseProvider
import com.example.finconapp.data.local.entity.PendingPayment
import com.example.finconapp.data.repository.PendingPaymentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingPaymentViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        PendingPaymentRepository(
            DatabaseProvider.provide(application)
                .pendingPaymentDao()
        )

    // Lista de pagos pendientes
    val pendingPayments: StateFlow<List<PendingPayment>> =
        repository
            .getAllPendingPayments()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // Registrar un pago
    fun insert(payment: PendingPayment) {
        viewModelScope.launch {
            repository.insert(payment)
        }
    }

    // Actualizar un pago
    fun update(payment: PendingPayment) {
        viewModelScope.launch {
            repository.update(payment)
        }
    }

    // Eliminar un pago
    fun delete(payment: PendingPayment) {
        viewModelScope.launch {
            repository.delete(payment)
        }
    }

    // Registrar un abono
    fun registerPayment(
        payment: PendingPayment,
        amount: Double
    ) {
        viewModelScope.launch {
            repository.registerPayment(
                payment,
                amount
            )
        }
    }
}
