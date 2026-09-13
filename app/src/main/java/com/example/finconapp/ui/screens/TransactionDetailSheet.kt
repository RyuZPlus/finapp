package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

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
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text =
                    if (transaction.type == "Ingreso") {
                        "+$%,.2f".format(transaction.amount)
                    } else {
                        "-$%,.2f".format(transaction.amount)
                    },
                style = MaterialTheme.typography.headlineMedium,
                color =
                    if (transaction.type == "Ingreso") {
                        Color(0xFF2E7D32)
                    } else {
                        Color(0xFFC62828)
                    }
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

            /*
             * BOTÓN EDITAR
             */
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar movimiento")
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            /*
             * BOTÓN ELIMINAR
             */
            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar movimiento")
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }

    /*
     * CONFIRMACIÓN DE ELIMINACIÓN
     */
    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text("Eliminar movimiento")
            },

            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar " +
                            "\"${transaction.title}\"? " +
                            "Esta acción no se puede deshacer."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
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