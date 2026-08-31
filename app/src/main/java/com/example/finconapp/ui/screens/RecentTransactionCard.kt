package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction

@Composable
fun RecentTransactionsCard(
    transactions: List<Transaction>
) {

    val recentTransactions = transactions
        .sortedByDescending { it.date }
        .take(5)

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Últimos movimientos",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            if (recentTransactions.isEmpty()) {

                Text(
                    text = "No hay movimientos registrados.",
                    style = MaterialTheme.typography.bodyMedium
                )

            } else {

                recentTransactions.forEach { transaction ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            // Descripción
                            Text(
                                text = transaction.title
                                    ?: "Sin título",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            // Categoría
                            Text(
                                text = transaction.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Monto
                        Text(
                            text = if (transaction.type == "Ingreso") {
                                "+$%,.2f".format(transaction.amount)
                            } else {
                                "-$%,.2f".format(transaction.amount)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (transaction.type == "Ingreso") {
                                Color(0xFF2E7D32)
                            } else {
                                Color(0xFFC62828)
                            }
                        )
                    }
                }
            }
        }
    }
}