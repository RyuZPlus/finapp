
package com.example.finconapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finconapp.data.backup.BackupFileManager
import com.example.finconapp.data.backup.BackupRepository
import com.example.finconapp.data.local.database.DatabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BackupViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        DatabaseProvider.provide(application)

    private val backupRepository =
        BackupRepository(database)

    private val _uiState =
        MutableStateFlow(BackupUiState())

    val uiState = _uiState.asStateFlow()


    // EXPORTAR RESPALDO
    fun exportBackup(uri: Uri) {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isProcessing = true,
                    message = null,
                    error = null
                )
            }

            try {

                // Recopilar información de Room
                val backup =
                    backupRepository.createBackup()

                // Escribir archivo JSON
                BackupFileManager.writeBackup(
                    getApplication(),
                    uri,
                    backup
                )

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = "Respaldo exportado correctamente."
                    )
                }

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message
                            ?: "No se pudo exportar el respaldo."
                    )
                }
            }
        }
    }


    // PREPARAR IMPORTACIÓN
    fun prepareImport(uri: Uri) {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isProcessing = true,
                    message = null,
                    error = null,
                    pendingRestore = null
                )
            }

            try {

                // Leer archivo JSON
                val backup =
                    BackupFileManager.readBackup(
                        getApplication(),
                        uri
                    )

                // Validar información
                backupRepository.validateBackup(backup)

                // Dejar el respaldo pendiente
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        pendingRestore = backup,
                        message = "El respaldo es válido. Confirma la restauración."
                    )
                }

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message
                            ?: "No se pudo leer el respaldo."
                    )
                }
            }
        }
    }


    // CONFIRMAR RESTAURACIÓN
    fun confirmRestore() {

        val backup =
            _uiState.value.pendingRestore ?: return

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isProcessing = true,
                    message = null,
                    error = null
                )
            }

            try {

                backupRepository.restoreBackup(backup)

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        pendingRestore = null,
                        message = "Respaldo restaurado correctamente."
                    )
                }

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message
                            ?: "No se pudo restaurar el respaldo."
                    )
                }
            }
        }
    }


    // CANCELAR RESTAURACIÓN
    fun cancelRestore() {

        _uiState.update {
            it.copy(
                pendingRestore = null,
                message = null,
                error = null
            )
        }
    }
}
