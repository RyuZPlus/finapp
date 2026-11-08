
package com.example.finconapp.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Creación de archivo para guardar los datos de respaldo
object BackupFileManager {

    // Guardar respaldo JSON
    suspend fun writeBackup(
        context: Context,
        uri: Uri,
        backup: BackupData
    ) = withContext(Dispatchers.IO) {

        val jsonContent = BackupJson.encode(backup)

        val outputStream = context.contentResolver
            .openOutputStream(uri, "wt")
            ?: throw IllegalStateException(
                "No se pudo abrir el archivo para escritura."
            )

        outputStream.bufferedWriter(Charsets.UTF_8).use {
            it.write(jsonContent)
        }
    }

    // Leer respaldo JSON
    suspend fun readBackup(
        context: Context,
        uri: Uri
    ): BackupData = withContext(Dispatchers.IO) {

        val inputStream = context.contentResolver
            .openInputStream(uri)
            ?: throw IllegalStateException(
                "No se pudo abrir el archivo para lectura."
            )

        val jsonContent = inputStream
            .bufferedReader(Charsets.UTF_8)
            .use {
                it.readText()
            }

        BackupJson.decode(jsonContent)
    }
}
