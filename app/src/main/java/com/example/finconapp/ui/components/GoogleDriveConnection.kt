package com.example.finconapp.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.backup.GoogleAccountManager
import com.example.finconapp.data.backup.GoogleDriveManager
import kotlinx.coroutines.launch

@Composable
fun GoogleDriveConnection(
    onAccessToken: (String) -> Unit
) {

    val context = LocalContext.current

    val activity = remember(context) {
        context.findActivity()
    }

    val accountManager = remember(context) {
        GoogleAccountManager(context)
    }

    var isAuthorized by remember {
        mutableStateOf(false)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var driveStatus by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    // Comprobar acceso y comunicar el token.
    fun verifyDriveAccess(token: String) {

        scope.launch {

            isLoading = true

            driveStatus =
                "Comprobando conexión con Google Drive..."

            try {

                val files =
                    GoogleDriveManager.listAppDataFiles(token)

                isAuthorized = true

                driveStatus =
                    "Conexión verificada. Archivos encontrados: ${files.size}"

                // Comunicar el token al componente padre.
                onAccessToken(token)

            } catch (e: Exception) {

                isAuthorized = false

                driveStatus =
                    "Error al consultar Google Drive: ${e.message}"

            } finally {

                isLoading = false
            }
        }
    }

    // Procesar la respuesta de autorización.
    val authorizationLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            isLoading = false

            if (result.resultCode == Activity.RESULT_OK) {

                try {

                    val authorizationResult =
                        accountManager.getAuthorizationResult(
                            result.data
                        )

                    val token = authorizationResult.accessToken

                    if (!token.isNullOrBlank()) {

                        verifyDriveAccess(token)

                    } else {

                        Toast.makeText(
                            context,
                            "No se recibió el token de acceso.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        context,
                        "Error al procesar la autorización: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {

                Toast.makeText(
                    context,
                    "Autorización cancelada.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = if (isAuthorized) {
                "Google Drive está autorizado."
            } else {
                "Vincula tu cuenta de Google para guardar tus respaldos."
            }
        )

        Button(
            onClick = {

                isLoading = true

                accountManager.authorize(
                    activity = activity,

                    onResolutionRequired = { intentSender ->

                        authorizationLauncher.launch(
                            IntentSenderRequest.Builder(
                                intentSender
                            ).build()
                        )
                    },

                    onAuthorized = { token ->

                        verifyDriveAccess(token)
                    },

                    onError = { exception ->

                        isLoading = false

                        Toast.makeText(
                            context,
                            "Error: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            },

            enabled = !isLoading,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text(
                text = when {
                    isLoading -> "Conectando..."
                    isAuthorized -> "Comprobar autorización"
                    else -> "Vincular Google Drive"
                }
            )
        }

        driveStatus?.let { status ->

            Text(
                text = status,

                color = if (status.startsWith("Error")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

// Obtener Activity de forma segura desde Context.
private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> error("No se encontró una Activity.")
    }
