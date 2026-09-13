package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction
import com.example.finconapp.ui.components.DateRangeSelector
import com.example.finconapp.ui.components.TransactionDayHeader
import com.example.finconapp.ui.components.TransactionFilters
import com.example.finconapp.ui.components.formatTransactionDay
import com.example.finconapp.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    paddingValues: PaddingValues,
    viewModel: TransactionViewModel
) {

    val transactions by viewModel.filteredTransactions.collectAsState()

    val customStartDate by
    viewModel.customStartDate.collectAsState()

    val customEndDate by
    viewModel.customEndDate.collectAsState()

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedType by remember {
        mutableStateOf("Todos")
    }

    var selectedCategory by remember {
        mutableStateOf<String?>(null)
    }

    var selectedTransaction by remember {
        mutableStateOf<Transaction?>(null)
    }

    /*
     * Aplicamos los filtros de la pantalla
     * sobre las transacciones que ya vienen
     * filtradas por fecha.
     */
    val displayedTransactions = remember(
        transactions,
        searchText,
        selectedType,
        selectedCategory
    ) {

        transactions.filter { transaction ->

            val matchesSearch =
                searchText.isBlank() ||
                        transaction.title.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        transaction.description?.contains(
                            searchText,
                            ignoreCase = true
                        ) == true ||
                        transaction.category.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        transaction.subcategory?.contains(
                            searchText,
                            ignoreCase = true
                        ) == true

            val matchesType =
                selectedType == "Todos" ||
                        transaction.type == selectedType

            val matchesCategory =
                selectedCategory == null ||
                        transaction.category == selectedCategory

            matchesSearch &&
                    matchesType &&
                    matchesCategory
        }
    }

    val totalTransactions =
        displayedTransactions.size

    val totalIncome =
        displayedTransactions
            .filter { it.type == "Ingreso" }
            .sumOf { it.amount }

    val totalExpenses =
        displayedTransactions
            .filter { it.type == "Gasto" }
            .sumOf { it.amount }

    val categories =
        transactions
            .map { it.category }
            .distinct()
            .sorted()

    /*
     * Agrupamos por día.
     */
    val groupedTransactions =
        displayedTransactions
            .sortedByDescending { it.date }
            .groupBy { transaction ->
                formatTransactionDay(transaction.date)
            }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * HEADER
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "Movimientos",
                style = MaterialTheme.typography.headlineMedium
            )

            DateRangeSelector(
                selectedRange =
                    viewModel.dateRangeType.collectAsState().value,

                customStartDate =
                    customStartDate,

                customEndDate =
                    customEndDate,

                onRangeSelected = { type ->
                    viewModel.setDateRange(type)
                },

                onCustomRangeSelected = {
                        startDate,
                        endDate ->

                    viewModel.setCustomDateRange(
                        startDate,
                        endDate
                    )
                }
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * BUSCADOR
         */
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Buscar movimientos")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            },
            singleLine = true
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * FILTROS
         */
        TransactionFilters(
            selectedType = selectedType,
            onTypeSelected = {
                selectedType = it
            },

            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = {
                selectedCategory = it
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * INDICADORES
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = "$totalTransactions registros",
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text = "Ingresos",
                    style =
                        MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "$%,.2f".format(totalIncome),
                    color = Color(0xFF2E7D32),
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text = "Gastos",
                    style =
                        MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "$%,.2f".format(totalExpenses),
                    color = Color(0xFFC62828),
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /*
         * LISTA
         */
        if (displayedTransactions.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "No hay movimientos registrados.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                groupedTransactions.forEach {
                        (day, dayTransactions) ->

                    item {

                        TransactionDayHeader(
                            date = day,
                            transactions =
                                dayTransactions
                        )
                    }

                    items(
                        items = dayTransactions,
                        key = {
                                transaction ->
                            transaction.id
                        }
                    ) { transaction ->

                        TransactionItem(
                            transaction = transaction,
                            onClick = {
                                selectedTransaction =
                                    transaction
                            }
                        )
                    }
                }
            }
        }
    }

    /*
     * DETALLE DE TRANSACCIÓN
     */
    selectedTransaction?.let { transaction ->

        TransactionDetailSheet(
            transaction = transaction,
            onDismiss = {
                selectedTransaction = null
            }
        )
    }
}