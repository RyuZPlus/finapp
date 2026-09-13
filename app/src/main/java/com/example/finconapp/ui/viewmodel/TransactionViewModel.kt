package com.example.finconapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finconapp.data.local.database.DatabaseProvider
import com.example.finconapp.data.local.entity.Transaction
import com.example.finconapp.data.repository.TransactionRepository
import com.example.finconapp.ui.model.DateRange
import com.example.finconapp.ui.model.DateRangeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository

    // Periodo seleccionado actualmente
    private val _dateRangeType =
        MutableStateFlow(DateRangeType.MONTH)

    val dateRangeType: StateFlow<DateRangeType> =
        _dateRangeType

    private val _customStartDate = MutableStateFlow<Long?>(null)
    val customStartDate: StateFlow<Long?> = _customStartDate

    private val _customEndDate = MutableStateFlow<Long?>(null)
    val customEndDate: StateFlow<Long?> = _customEndDate

    // Fechas correspondientes al periodo seleccionado
    private val _dateRange =
        MutableStateFlow(
            calculateDateRange(DateRangeType.MONTH)
        )

    val dateRange: StateFlow<DateRange> =
        _dateRange

    // Transacciones filtradas por el periodo seleccionado
    val filteredTransactions: StateFlow<List<Transaction>>

    init {

        val db = DatabaseProvider.provide(application)
        val dao = db.transactionDao()

        repository = TransactionRepository(dao)

        filteredTransactions = _dateRange
            .flatMapLatest { range ->

                repository.getTransactionsByDateRange(
                    range.startDate,
                    range.endDate
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // Insertar movimiento
    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    // Cambiar periodo
    fun setDateRange(type: DateRangeType) {

        _dateRangeType.value = type

        if (type != DateRangeType.CUSTOM) {

            _dateRange.value =
                calculateDateRange(type)
        }
    }

    // Establecer rango personalizado
    fun setCustomDateRange(
        startDate: Long,
        endDate: Long
    ) {

        _customStartDate.value = startDate
        _customEndDate.value = endDate

        _dateRangeType.value =
            DateRangeType.CUSTOM

        _dateRange.value =
            DateRange(
                startDate = startDate,
                endDate = endDate
            )
    }

    // Calcular rango de fechas
    private fun calculateDateRange(
        type: DateRangeType
    ): DateRange {

        val calendar = Calendar.getInstance()

        // Fecha de hoy
        val endCalendar =
            Calendar.getInstance()

        when (type) {

            DateRangeType.DAY -> {

                // Inicio del día
                calendar.set(
                    Calendar.HOUR_OF_DAY,
                    0
                )
                calendar.set(
                    Calendar.MINUTE,
                    0
                )
                calendar.set(
                    Calendar.SECOND,
                    0
                )
                calendar.set(
                    Calendar.MILLISECOND,
                    0
                )

                // Final del día
                endCalendar.set(
                    Calendar.HOUR_OF_DAY,
                    23
                )
                endCalendar.set(
                    Calendar.MINUTE,
                    59
                )
                endCalendar.set(
                    Calendar.SECOND,
                    59
                )
                endCalendar.set(
                    Calendar.MILLISECOND,
                    999
                )
            }

            DateRangeType.WEEK -> {

                // Últimos 7 días incluyendo hoy
                calendar.add(
                    Calendar.DAY_OF_YEAR,
                    -6
                )

                calendar.set(
                    Calendar.HOUR_OF_DAY,
                    0
                )
                calendar.set(
                    Calendar.MINUTE,
                    0
                )
                calendar.set(
                    Calendar.SECOND,
                    0
                )
                calendar.set(
                    Calendar.MILLISECOND,
                    0
                )

                endCalendar.set(
                    Calendar.HOUR_OF_DAY,
                    23
                )
                endCalendar.set(
                    Calendar.MINUTE,
                    59
                )
                endCalendar.set(
                    Calendar.SECOND,
                    59
                )
                endCalendar.set(
                    Calendar.MILLISECOND,
                    999
                )
            }

            DateRangeType.MONTH -> {

                // Primer día del mes actual
                calendar.add(
                    Calendar.DAY_OF_YEAR,
                    -29
                )

                calendar.set(
                    Calendar.HOUR_OF_DAY,
                    0
                )
                calendar.set(
                    Calendar.MINUTE,
                    0
                )
                calendar.set(
                    Calendar.SECOND,
                    0
                )
                calendar.set(
                    Calendar.MILLISECOND,
                    0
                )

                endCalendar.set(
                    Calendar.HOUR_OF_DAY,
                    23
                )
                endCalendar.set(
                    Calendar.MINUTE,
                    59
                )
                endCalendar.set(
                    Calendar.SECOND,
                    59
                )
                endCalendar.set(
                    Calendar.MILLISECOND,
                    999
                )
            }

            DateRangeType.CUSTOM -> {
                // No se utiliza aquí.
            }
        }

        return DateRange(
            startDate = calendar.timeInMillis,
            endDate = endCalendar.timeInMillis
        )
    }
}