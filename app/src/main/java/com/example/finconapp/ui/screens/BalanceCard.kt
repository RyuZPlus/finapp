package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction

@Composable
fun BalanceCard(
    transactions: List<Transaction>
) {

    val income = transactions
        .filter { it.type.equals("Ingreso", ignoreCase = true) }
        .sumOf { it.amount }

    val expenses = transactions
        .filter { it.type.equals("Gasto", ignoreCase = true) }
        .sumOf { it.amount }

    val balance = income - expenses

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Balance",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "$%.2f".format(balance),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "Ingresos",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "$%.2f".format(income),
                        color = Color(0xFF2E7D32)
                    )
                }

                Column {

                    Text(
                        text = "Gastos",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "$%.2f".format(expenses),
                        color = Color(0xFFC62828)
                    )
                }
            }
        }
    }
}