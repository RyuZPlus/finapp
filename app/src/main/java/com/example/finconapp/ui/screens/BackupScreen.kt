
package com.example.finconapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finconapp.ui.viewmodel.BackupViewModel

@Composable
fun BackupScreen(
    viewModel: BackupViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    // Selector para guardar el respaldo
    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(
                "application/json"
            )
        ) { uri ->

            uri?.let {
                viewModel.exportBackup(it)
            }
        }

    // Selector para importar el respaldo
    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->

            uri?.let {
                viewModel.prepareImport(it)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Respaldo de datos",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Guarda tus datos o recupera un respaldo anterior.",
            style = MaterialTheme.typography.bodyMedium
        )

        // EXPORTAR
        Button(
            onClick = {
                exportLauncher.launch("Finconapp_respaldo.json")
            },
            enabled = !uiState.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Exportar respaldo")
        }

        // IMPORTAR
        OutlinedButton(
            onClick = {
                importLauncher.launch(
                    arrayOf("application/json")
                )
            },
            enabled = !uiState.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Importar respaldo")
        }

        // PROCESANDO
        if (uiState.isProcessing) {

            CircularProgressIndicator()

            Text("Procesando respaldo...")
        }

        // MENSAJE DE ÉXITO
        uiState.message?.let { message ->

            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // MENSAJE DE ERROR
        uiState.error?.let { error ->

            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    // CONFIRMACIÓN DE RESTAURACIÓN
    uiState.pendingRestore?.let { backup ->

        AlertDialog(

            onDismissRequest = {
                if (!uiState.isProcessing) {
                    viewModel.cancelRestore()
                }
            },

            title = {
                Text("Restaurar respaldo")
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "Esta operación reemplazará los datos actuales de Finconapp."
                    )

                    Text(
                        "Categorías: ${backup.categories.size}"
                    )

                    Text(
                        "Transacciones: ${backup.transactions.size}"
                    )

                    Text(
                        "Metas de ahorro: ${backup.savingsGoals.size}"
                    )

                    Text(
                        "¿Deseas continuar?"
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        viewModel.confirmRestore()
                    },
                    enabled = !uiState.isProcessing
                ) {
                    Text("Restaurar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        viewModel.cancelRestore()
                    },
                    enabled = !uiState.isProcessing
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
