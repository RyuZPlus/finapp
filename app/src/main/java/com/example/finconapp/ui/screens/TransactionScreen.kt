package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.finconapp.ui.viewmodel.TransactionViewModel
import androidx.compose.ui.Alignment
import com.example.finconapp.ui.components.DateRangeSelector

@Composable
fun TransactionScreen(
    paddingValues: PaddingValues,
    viewModel: TransactionViewModel
) {

    //val transactions by viewModel.allTransactions.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    val customStartDate by
    viewModel.customStartDate.collectAsState()

    val customEndDate by
    viewModel.customEndDate.collectAsState()

    val totalTransactions = transactions.size

    val totalIncome = transactions
        .filter { it.type == "Ingreso" }
        .sumOf { it.amount }

    val totalExpenses = transactions
        .filter { it.type == "Gasto" }
        .sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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

                onCustomRangeSelected = { startDate, endDate ->
                    viewModel.setCustomDateRange(
                        startDate,
                        endDate
                    )
                }
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = "$totalTransactions registros",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column {
                Text(
                    text = "Ingresos",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "$%,.2f".format(totalIncome),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32)
                )
            }

            Column {
                Text(
                    text = "Gastos",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "$%,.2f".format(totalExpenses),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC62828)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        if (transactions.isEmpty()) {

            Text(
                text = "No hay movimientos registrados.",
                style = MaterialTheme.typography.bodyMedium
            )

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = transactions,
                    key = { transaction ->
                        transaction.id
                    }
                ) { transaction ->

                    TransactionItem(
                        transaction = transaction
                    )
                }
            }
        }
    }
}