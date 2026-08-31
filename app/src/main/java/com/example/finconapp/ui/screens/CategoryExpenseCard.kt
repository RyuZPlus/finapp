package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction

@Composable
fun CategoryExpenseCard(
    transactions: List<Transaction>
) {

    val expenses = transactions
        .filter { it.type == "Gasto" }

    val totalExpenses = expenses
        .sumOf { it.amount }

    val expensesByCategory = expenses
        .groupBy { it.category }
        .mapValues { (_, transactions) ->
            transactions.sumOf { it.amount }
        }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Por categoría",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            if (expensesByCategory.isEmpty()) {

                Text(
                    text = "No hay gastos registrados.",
                    style = MaterialTheme.typography.bodyMedium
                )

            } else {

                expensesByCategory.forEach { (category, amount) ->

                    // Porcentaje que representa la categoría
                    // respecto al total de gastos
                    val percentage = if (totalExpenses > 0) {
                        (amount / totalExpenses) * 100
                    } else {
                        0.0
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "%.1f%%".format(percentage),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        LinearProgressIndicator(
                            progress = {
                                (percentage / 100).toFloat()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = "$%,.2f".format(amount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}