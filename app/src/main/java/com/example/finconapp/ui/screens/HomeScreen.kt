package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.ui.viewmodel.TransactionViewModel

@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    viewModel: TransactionViewModel
) {

    val transactions by viewModel.allTransactions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),

        verticalArrangement = Arrangement.spacedBy(20.dp),

        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 24.dp
        )
    ) {

        // Título
        item {

            Text(
                text = "Resumen",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Balance
        item {

            BalanceCard(
                transactions = transactions
            )
        }

        // Gráfico
        item {

            CategoryExpenseCard(
                transactions = transactions
            )
        }

        // Últimos movimientos
        item {

            RecentTransactionsCard(
                transactions = transactions
            )
        }
    }
}