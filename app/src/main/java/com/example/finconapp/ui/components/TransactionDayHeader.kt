package com.example.finconapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionDayHeader(
    date: String,
    transactions: List<Transaction>
) {

    val income =
        transactions
            .filter { it.type == "Ingreso" }
            .sumOf { it.amount }

    val expenses =
        transactions
            .filter { it.type == "Gasto" }
            .sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 4.dp
            )
    ) {

        Text(
            text = date,
            style =
                MaterialTheme.typography.titleMedium
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            if (income > 0) {

                Text(
                    text =
                        "+$%,.2f".format(income),
                    color =
                        androidx.compose.ui.graphics.Color(
                            0xFF2E7D32
                        ),
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            if (expenses > 0) {

                Text(
                    text =
                        "-$%,.2f".format(expenses),
                    color =
                        androidx.compose.ui.graphics.Color(
                            0xFFC62828
                        ),
                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

fun formatTransactionDay(
    timestamp: Long
): String {

    val formatter =
        SimpleDateFormat(
            "EEEE, dd 'de' MMMM",
            Locale("es", "MX")
        )

    return formatter.format(
        Date(timestamp)
    ).replaceFirstChar {
        it.uppercase()
    }
}