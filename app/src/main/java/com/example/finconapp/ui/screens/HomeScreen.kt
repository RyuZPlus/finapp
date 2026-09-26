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
import androidx.compose.ui.Alignment
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.ui.components.DateRangeSelector
import com.example.finconapp.ui.viewmodel.TransactionViewModel

@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    viewModel: TransactionViewModel,
    categoriesList: List<Category>
) {

    val transactions by viewModel.filteredTransactions.collectAsState()

    val customStartDate by
    viewModel.customStartDate.collectAsState()

    val customEndDate by
    viewModel.customEndDate.collectAsState()

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Resumen",
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
                transactions = transactions,
                categories = categoriesList
            )
        }

        // Últimos movimientos
        item {

            RecentTransactionsCard(
                transactions = transactions,
                categories = categoriesList
            )
        }
    }
}