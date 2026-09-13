package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: Transaction,
    onDismiss: () -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {

            Text(
                text = transaction.title,
                style =
                    MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text =
                    if (transaction.type == "Ingreso")
                        "+$%,.2f".format(transaction.amount)
                    else
                        "-$%,.2f".format(transaction.amount),
                style =
                    MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            DetailRow(
                label = "Tipo",
                value = transaction.type
            )

            DetailRow(
                label = "Categoría",
                value = transaction.category
            )

            transaction.subcategory?.let {

                DetailRow(
                    label = "Subcategoría",
                    value = it
                )
            }

            DetailRow(
                label = "Fecha",
                value = formatDetailDate(
                    transaction.date
                )
            )

            transaction.description?.let {

                if (it.isNotBlank()) {

                    DetailRow(
                        label = "Descripción",
                        value = it
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Text(
            text = label,
            style =
                MaterialTheme.typography.labelMedium
        )

        Text(
            text = value,
            style =
                MaterialTheme.typography.bodyLarge
        )
    }
}

private fun formatDetailDate(
    timestamp: Long
): String {

    val formatter =
        SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale("es", "MX")
        )

    return formatter.format(
        Date(timestamp)
    )
}