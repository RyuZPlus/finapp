package com.example.finconapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.finconapp.ui.viewmodel.BackupViewModel
import com.example.finconapp.ui.components.GoogleDriveConnection

@Composable
fun BackupScreen(
    viewModel: BackupViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val driveBackups by viewModel.driveBackups.collectAsState()

    var passwordInput by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    // Token de acceso de Google Drive.
    var driveAccessToken by remember {
        mutableStateOf<String?>(null)
    }

    // Selector para guardar el respaldo local.
    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(
                "application/json"
            )
        ) { uri ->

            uri?.let {

                val password = passwordInput.toCharArray()
                passwordInput = ""

                viewModel.exportBackup(
                    uri = uri,
                    password = password
                )
            }
        }

    // Selector para importar el respaldo local.
    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->

            uri?.let {

                val password = passwordInput.toCharArray()
                passwordInput = ""

                viewModel.prepareImport(
                    uri = uri,
                    password = password
                )
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp),

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

        // CONTRASEÑA DEL RESPALDO
        OutlinedTextField(
            value = passwordInput,
            onValueChange = {
                passwordInput = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Contraseña del respaldo")
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                androidx.compose.ui.text.input.VisualTransformation.None
            } else {
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            },
            trailingIcon = {

                TextButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Text(
                        if (passwordVisible) "Ocultar" else "Mostrar"
                    )
                }
            }
        )

        // EXPORTAR RESPALDO LOCAL
        Button(
            onClick = {
                exportLauncher.launch("Finconapp_respaldo.json")
            },
            enabled = !uiState.isProcessing &&
                    passwordInput.length >= 12,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exportar respaldo")
        }

        // IMPORTAR RESPALDO LOCAL
        OutlinedButton(
            onClick = {
                importLauncher.launch(
                    arrayOf("application/json")
                )
            },
            enabled = !uiState.isProcessing &&
                    passwordInput.length >= 8,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Importar respaldo")
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // GOOGLE DRIVE
        Text(
            text = "Sincronización en la nube",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Vincula tu cuenta de Google para guardar y recuperar tus respaldos.",
            style = MaterialTheme.typography.bodyMedium
        )

        // CONEXIÓN Y AUTORIZACIÓN
        GoogleDriveConnection(
            onAccessToken = { token ->

                driveAccessToken = token

                // Consultar respaldos al conectar Google Drive.
                viewModel.loadDriveBackups(token)
            }
        )

        // SUBIR RESPALDO A GOOGLE DRIVE
        Button(
            onClick = {

                val token = driveAccessToken

                if (token != null) {

                    val password = passwordInput.toCharArray()
                    passwordInput = ""

                    viewModel.uploadBackupToDrive(
                        accessToken = token,
                        password = password
                    )
                }
            },
            enabled = !uiState.isProcessing &&
                    driveAccessToken != null &&
                    passwordInput.length >= 12,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear respaldo en Google Drive")
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // LISTA DE RESPALDOS DE GOOGLE DRIVE
        Text(
            text = "Respaldos disponibles",
            style = MaterialTheme.typography.titleMedium
        )

        // CONSULTAR RESPALDOS
        OutlinedButton(
            onClick = {

                driveAccessToken?.let { token ->
                    viewModel.loadDriveBackups(token)
                }
            },
            enabled = !uiState.isProcessing &&
                    driveAccessToken != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Consultar respaldos")
        }

        // MOSTRAR RESPALDOS ENCONTRADOS
        if (driveAccessToken != null && !uiState.isProcessing) {

            if (driveBackups.isEmpty()) {

                Text(
                    text = "No hay respaldos disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                driveBackups.forEach { file ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleSmall
                            )

                            Text(
                                text = "Archivo cifrado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Última modificación: ${
                                    file.modifiedTime ?: "No disponible"
                                }",
                                style = MaterialTheme.typography.bodySmall
                            )

                            OutlinedButton(
                                onClick = {

                                    val token = driveAccessToken

                                    if (token != null) {

                                        val password = passwordInput.toCharArray()
                                        passwordInput = ""

                                        viewModel.prepareDriveRestore(
                                            accessToken = token,
                                            fileId = file.id,
                                            password = password
                                        )
                                    }
                                },
                                enabled = !uiState.isProcessing &&
                                        passwordInput.length >= 8,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Descargar y restaurar")
                            }
                        }
                    }
                }
            }
        }

        // ESTADO DE PROCESAMIENTO
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

                    Text("¿Deseas continuar?")
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
