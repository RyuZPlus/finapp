
package com.example.finconapp.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

object BackupFileManager {

    private const val MAX_FILE_SIZE = 64 * 1024 * 1024

    // EXPORTAR RESPALDO CIFRADO
    suspend fun writeBackup(
        context: Context,
        uri: Uri,
        backup: BackupData,
        password: CharArray
    ) = withContext(Dispatchers.IO) {

        val jsonContent = BackupJson.encode(backup)
        val jsonBytes = jsonContent.toByteArray(StandardCharsets.UTF_8)

        try {
            // Cifrar el contenido JSON.
            val encryptedBytes = BackupCrypto.encrypt(
                data = jsonBytes,
                password = password
            )

            try {
                val outputStream = context.contentResolver
                    .openOutputStream(uri, "wt")
                    ?: throw IllegalStateException(
                        "No se pudo abrir el archivo para escritura."
                    )

                outputStream.use {
                    it.write(encryptedBytes)
                    it.flush()
                }
            } finally {
                encryptedBytes.fill(0)
            }

        } finally {
            jsonBytes.fill(0)
        }
    }

    // IMPORTAR RESPALDO CIFRADO
    suspend fun readBackup(
        context: Context,
        uri: Uri,
        password: CharArray
    ): BackupData = withContext(Dispatchers.IO) {

        val encryptedBytes = context.contentResolver
            .openInputStream(uri)
            ?.use { input ->
                readLimited(input)
            }
            ?: throw IllegalStateException(
                "No se pudo abrir el archivo para lectura."
            )

        var decryptedBytes: ByteArray? = null

        try {
            // Verificar autenticidad y descifrar.
            decryptedBytes = BackupCrypto.decrypt(
                encryptedBackup = encryptedBytes,
                password = password
            )

            // Convertir el contenido descifrado a JSON.
            val jsonContent = String(
                decryptedBytes,
                StandardCharsets.UTF_8
            )

            // Reconstruir BackupData.
            BackupJson.decode(jsonContent)

        } finally {
            encryptedBytes.fill(0)
            decryptedBytes?.fill(0)
        }
    }

    // Evitar leer archivos excesivamente grandes.
    private fun readLimited(
        input: java.io.InputStream
    ): ByteArray {

        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var totalBytes = 0

        while (true) {
            val bytesRead = input.read(buffer)

            if (bytesRead == -1) break

            totalBytes += bytesRead

            require(totalBytes <= MAX_FILE_SIZE) {
                "El archivo de respaldo supera el tamaño permitido."
            }

            output.write(buffer, 0, bytesRead)
        }

        buffer.fill(0)

        return output.toByteArray()
    }
    // CREAR RESPALDO CIFRADO EN MEMORIA
    suspend fun createEncryptedBackup(
        backup: BackupData,
        password: CharArray
    ): ByteArray = withContext(Dispatchers.IO) {

        val jsonContent = BackupJson.encode(backup)

        val jsonBytes = jsonContent.toByteArray(
            StandardCharsets.UTF_8
        )

        try {
            BackupCrypto.encrypt(
                data = jsonBytes,
                password = password
            )
        } finally {
            jsonBytes.fill(0)
        }
    }


    // DESCIFRAR RESPALDO DESCARGADO DE GOOGLE DRIVE
    suspend fun readEncryptedBackup(
        encryptedBytes: ByteArray,
        password: CharArray
    ): BackupData = withContext(Dispatchers.IO) {

        require(encryptedBytes.isNotEmpty()) {
            "El respaldo está vacío."
        }

        require(encryptedBytes.size <= MAX_FILE_SIZE) {
            "El respaldo supera el tamaño permitido."
        }

        var decryptedBytes: ByteArray? = null

        try {

            decryptedBytes = BackupCrypto.decrypt(
                encryptedBackup = encryptedBytes,
                password = password
            )

            val jsonContent = String(
                decryptedBytes,
                StandardCharsets.UTF_8
            )

            BackupJson.decode(jsonContent)

        } finally {
            decryptedBytes?.fill(0)
        }
    }
}