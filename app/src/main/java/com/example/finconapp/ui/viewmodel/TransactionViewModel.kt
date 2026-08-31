package com.example.finconapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finconapp.data.local.database.DatabaseProvider
import com.example.finconapp.data.local.entity.Transaction
import com.example.finconapp.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    val allTransactions: StateFlow<List<Transaction>>

    init {
        val db = DatabaseProvider.provide(application)
        val dao = db.transactionDao()
        repository = TransactionRepository(dao)
        allTransactions = repository.getAllTransactions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }
}