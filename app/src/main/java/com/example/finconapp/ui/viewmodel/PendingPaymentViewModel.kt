
package com.example.finconapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finconapp.data.local.database.DatabaseProvider
import com.example.finconapp.data.local.entity.PendingPayment
import com.example.finconapp.data.repository.PendingPaymentRepository
import com.example.finconapp.utils.PendingPaymentReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingPaymentViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = PendingPaymentRepository(
        DatabaseProvider.provide(application).pendingPaymentDao()
    )

    private val reminderScheduler =
        PendingPaymentReminderScheduler(application)

    val pendingPayments: StateFlow<List<PendingPayment>> =
        repository.getAllPendingPayments().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun insert(payment: PendingPayment) {
        viewModelScope.launch {
            val newId = repository.insert(payment)

            reminderScheduler.schedule(
                payment.copy(id = newId.toInt())
            )
        }
    }

    fun update(payment: PendingPayment) {
        viewModelScope.launch {
            repository.update(payment)
            reminderScheduler.schedule(payment)
        }
    }

    fun delete(payment: PendingPayment) {
        viewModelScope.launch {
            reminderScheduler.cancel(payment.id)
            repository.delete(payment)
        }
    }

    fun registerPayment(
        payment: PendingPayment,
        amount: Double
    ) {
        viewModelScope.launch {
            val updatedPayment = repository.registerPayment(
                payment,
                amount
            )

            reminderScheduler.schedule(updatedPayment)
        }
    }
}
