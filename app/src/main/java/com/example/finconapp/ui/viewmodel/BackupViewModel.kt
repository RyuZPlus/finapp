
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
import com.example.finconapp.data.backup.GoogleDriveManager
import com.example.finconapp.data.backup.DriveBackupFile
import kotlinx.coroutines.CancellationException

class BackupViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        DatabaseProvider.provide(application)

    private val backupRepository =
        BackupRepository(database)

    private val _uiState =
        MutableStateFlow(BackupUiState())

    // RESPALDOS DISPONIBLES EN GOOGLE DRIVE
    private val _driveBackups =
        MutableStateFlow<List<DriveBackupFile>>(emptyList())

    val driveBackups = _driveBackups.asStateFlow()

    val uiState = _uiState.asStateFlow()


    // EXPORTAR RESPALDO CIFRADO
    fun exportBackup(
        uri: Uri,
        password: CharArray
    ) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isProcessing = true,
                    message = null,
                    error = null
                )
            }

            try {
                // Recopilar información de Room.
                val backup = backupRepository.createBackup()

                // Crear y guardar el archivo cifrado.
                BackupFileManager.writeBackup(
                    context = getApplication(),
                    uri = uri,
                    backup = backup,
                    password = password
                )

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = "Respaldo cifrado exportado correctamente."
                    )
                }

            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message
                            ?: "No se pudo exportar el respaldo."
                    )
                }

            } finally {
                password.fill('\u0000')
            }
        }
    }


    // PREPARAR IMPORTACIÓN DE RESPALDO CIFRADO
    fun prepareImport(
        uri: Uri,
        password: CharArray
    ) {
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
                // Leer y descifrar el archivo.
                val backup = BackupFileManager.readBackup(
                    context = getApplication(),
                    uri = uri,
                    password = password
                )

                // Validar la información recuperada.
                backupRepository.validateBackup(backup)

                // Dejar el respaldo pendiente de confirmación.
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        pendingRestore = backup,
                        message = "El respaldo es válido. Confirma la restauración."
                    )
                }

            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message
                            ?: "No se pudo importar el respaldo."
                    )
                }

            } finally {
                password.fill('\u0000')
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
    // CREAR Y SUBIR RESPALDO A GOOGLE DRIVE
    fun uploadBackupToDrive(
        accessToken: String,
        password: CharArray
    ) {
        viewModelScope.launch {

            var encryptedBytes: ByteArray? = null

            _uiState.update {
                it.copy(
                    isProcessing = true,
                    message = null,
                    error = null
                )
            }

            try {

                // 1. Obtener registros de Room.
                val backup = backupRepository.createBackup()

                // 2. Cifrar el respaldo en memoria.
                encryptedBytes = BackupFileManager.createEncryptedBackup(
                    backup = backup,
                    password = password
                )

                // 3. Subir o actualizar el archivo en Drive.
                val uploadedFile =
                    GoogleDriveManager.uploadEncryptedBackup(
                        accessToken = accessToken,
                        encryptedData = encryptedBytes
                    )

                // 4. Informar resultado.
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = "Respaldo guardado en Google Drive correctamente."
                    )
                }

            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message
                            ?: "No se pudo guardar el respaldo en Google Drive."
                    )
                }

            } finally {
                encryptedBytes?.fill(0)
                password.fill('\u0000')
            }
        }
    }
    // CONSULTAR RESPALDOS DISPONIBLES EN GOOGLE DRIVE
    fun loadDriveBackups(accessToken: String) {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isProcessing = true,
                    message = null,
                    error = null
                )
            }

            try {

                val files =
                    GoogleDriveManager.listAppDataFiles(accessToken)
                        .filter { it.name == "finconapp_backup.enc" }

                _driveBackups.value = files

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = if (files.isEmpty()) {
                            "No se encontraron respaldos en Google Drive."
                        } else {
                            "Se encontraron ${files.size} respaldo(s)."
                        }
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message
                            ?: "No se pudieron consultar los respaldos."
                    )
                }
            }
        }
    }

    // DESCARGAR Y PREPARAR RESTAURACIÓN DESDE GOOGLE DRIVE
    fun prepareDriveRestore(
        accessToken: String,
        fileId: String,
        password: CharArray
    ) {
        viewModelScope.launch {

            var encryptedBytes: ByteArray? = null

            _uiState.update {
                it.copy(
                    isProcessing = true,
                    message = null,
                    error = null,
                    pendingRestore = null
                )
            }

            try {

                // 1. Descargar el archivo cifrado.
                encryptedBytes =
                    GoogleDriveManager.downloadEncryptedBackup(
                        accessToken = accessToken,
                        fileId = fileId
                    )

                // 2. Descifrar y convertir el contenido a BackupData.
                val backup =
                    BackupFileManager.readEncryptedBackup(
                        encryptedBytes = encryptedBytes,
                        password = password
                    )

                // 3. Validar la estructura y los datos del respaldo.
                backupRepository.validateBackup(backup)

                // 4. Esperar confirmación del usuario.
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        pendingRestore = backup,
                        message = "El respaldo de Google Drive es válido. Confirma la restauración."
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = e.message
                            ?: "No se pudo descargar o validar el respaldo de Google Drive."
                    )
                }

            } finally {
                encryptedBytes?.fill(0)
                password.fill('\u0000')
            }
        }
    }
}
