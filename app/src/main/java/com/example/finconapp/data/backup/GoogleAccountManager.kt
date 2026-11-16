
package com.example.finconapp.data.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.common.api.Scope

class GoogleAccountManager(context: Context) {

    private val authorizationClient =
        Identity.getAuthorizationClient(context)

    companion object {
        private const val DRIVE_APPDATA_SCOPE =
            "https://www.googleapis.com/auth/drive.appdata"
    }

    // Solicitar autorización para acceder a Google Drive
    fun authorize(
        activity: Activity,
        onResolutionRequired: (IntentSender) -> Unit,
        onAuthorized: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val requestedScopes = listOf(
            Scope(DRIVE_APPDATA_SCOPE)
        )

        val request = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .build()

        authorizationClient
            .authorize(request)
            .addOnSuccessListener { result ->

                if (result.hasResolution()) {

                    val pendingIntent = result.pendingIntent

                    if (pendingIntent != null) {
                        onResolutionRequired(
                            pendingIntent.intentSender
                        )
                    } else {
                        onError(
                            IllegalStateException(
                                "No se recibió la autorización pendiente."
                            )
                        )
                    }

                } else {

                    val accessToken = result.accessToken

                    if (!accessToken.isNullOrBlank()) {
                        onAuthorized(accessToken)
                    } else {
                        onError(
                            IllegalStateException(
                                "Google no proporcionó un token de acceso."
                            )
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    // Obtener el resultado después de aceptar los permisos
    fun getAuthorizationResult(
        data: Intent?
    ): AuthorizationResult {

        return authorizationClient
            .getAuthorizationResultFromIntent(data)
    }

}
