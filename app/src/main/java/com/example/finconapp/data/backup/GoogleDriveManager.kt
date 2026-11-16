
package com.example.finconapp.data.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import java.io.ByteArrayOutputStream

data class DriveBackupFile(
    val id: String,
    val name: String,
    val mimeType: String?,
    val modifiedTime: String?
)

object GoogleDriveManager {

    private const val DRIVE_FILES_URL =
        "https://www.googleapis.com/drive/v3/files"

    private const val MAX_FILE_SIZE = 64L * 1024 * 1024

    private fun readLimited(
        input: java.io.InputStream
    ): ByteArray {

        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)

        var totalBytes = 0L

        try {
            while (true) {

                val bytesRead = input.read(buffer)

                if (bytesRead == -1) break

                totalBytes += bytesRead

                require(totalBytes <= MAX_FILE_SIZE) {
                    "El respaldo supera el tamaño permitido de 64 MB."
                }

                output.write(buffer, 0, bytesRead)
            }

            return output.toByteArray()

        } finally {
            buffer.fill(0)
        }
    }
    // Consultar los archivos almacenados en appDataFolder
    suspend fun listAppDataFiles(
        accessToken: String
    ): List<DriveBackupFile> = withContext(Dispatchers.IO) {

        require(accessToken.isNotBlank()) {
            "El token de acceso está vacío."
        }

        val url = URL(
            "$DRIVE_FILES_URL?spaces=appDataFolder" +
                    "&fields=files(id,name,mimeType,modifiedTime)"
        )

        val connection =
            url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"

            connection.setRequestProperty(
                "Authorization",
                "Bearer $accessToken"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode

            val responseStream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response = responseStream
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: ""

            if (responseCode !in 200..299) {
                throw Exception(
                    "Error de Google Drive ($responseCode): $response"
                )
            }

            val json = JSONObject(response)
            val files = json.optJSONArray("files")

            buildList {
                if (files != null) {
                    for (i in 0 until files.length()) {
                        val file = files.getJSONObject(i)

                        add(
                            DriveBackupFile(
                                id = file.getString("id"),
                                name = file.getString("name"),
                                mimeType = file.optString("mimeType")
                                    .takeIf { it.isNotBlank() },
                                modifiedTime = file.optString("modifiedTime")
                                    .takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
            }

        } finally {
            connection.disconnect()
        }
    }

    // SUBIR O ACTUALIZAR RESPALDO CIFRADO
    suspend fun uploadEncryptedBackup(
        accessToken: String,
        encryptedData: ByteArray,
        fileName: String = "finconapp_backup.enc"
    ): DriveBackupFile = withContext(Dispatchers.IO) {

        require(accessToken.isNotBlank()) {
            "El token de acceso está vacío."
        }

        require(encryptedData.isNotEmpty()) {
            "El respaldo está vacío."
        }

        // Buscar si ya existe nuestro respaldo.
        val existingFile = listAppDataFiles(accessToken)
            .firstOrNull { it.name == fileName }

        val isUpdate = existingFile != null

        val url = if (isUpdate) {
            URL(
                "https://www.googleapis.com/upload/drive/v3/files/" +
                        "${existingFile.id}?uploadType=media" +
                        "&fields=id,name,mimeType,modifiedTime"
            )
        } else {
            URL(
                "https://www.googleapis.com/upload/drive/v3/files" +
                        "?uploadType=multipart" +
                        "&fields=id,name,mimeType,modifiedTime"
            )
        }

        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = if (isUpdate) "PATCH" else "POST"
            connection.doOutput = true

            connection.setRequestProperty(
                "Authorization",
                "Bearer $accessToken"
            )

            connection.setRequestProperty(
                "Content-Type",
                if (isUpdate) {
                    "application/octet-stream"
                } else {
                    "multipart/related; boundary=FinconappBoundary"
                }
            )

            connection.connectTimeout = 15000
            connection.readTimeout = 30000

            if (isUpdate) {

                // Actualizar el contenido del archivo existente.
                connection.setFixedLengthStreamingMode(encryptedData.size)

                connection.outputStream.use {
                    it.write(encryptedData)
                }

            } else {

                // Crear un archivo nuevo en appDataFolder.
                val boundary = "FinconappBoundary"

                val metadata = JSONObject().apply {
                    put("name", fileName)
                    put("mimeType", "application/octet-stream")
                    put("parents", JSONArray().put("appDataFolder"))
                }

                val body = ByteArrayOutputStream().apply {

                    write("--$boundary\r\n".toByteArray())

                    write(
                        "Content-Type: application/json; charset=UTF-8\r\n\r\n"
                            .toByteArray()
                    )

                    write(metadata.toString().toByteArray(Charsets.UTF_8))
                    write("\r\n".toByteArray())

                    write("--$boundary\r\n".toByteArray())

                    write(
                        "Content-Type: application/octet-stream\r\n\r\n"
                            .toByteArray()
                    )

                    write(encryptedData)
                    write("\r\n".toByteArray())

                    write("--$boundary--\r\n".toByteArray())

                }.toByteArray()

                connection.setRequestProperty(
                    "Content-Type",
                    "multipart/related; boundary=$boundary"
                )

                connection.setFixedLengthStreamingMode(body.size)

                connection.outputStream.use {
                    it.write(body)
                }
            }

            val responseCode = connection.responseCode

            val responseStream =
                if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response = responseStream
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: ""

            if (responseCode !in 200..299) {
                throw Exception(
                    "Error de Google Drive ($responseCode): $response"
                )
            }

            val json = JSONObject(response)

            DriveBackupFile(
                id = json.getString("id"),
                name = json.getString("name"),
                mimeType = json.optString("mimeType")
                    .takeIf { it.isNotBlank() },
                modifiedTime = json.optString("modifiedTime")
                    .takeIf { it.isNotBlank() }
            )

        } finally {
            connection.disconnect()
        }

    }
    // DESCARGAR RESPALDO CIFRADO DESDE GOOGLE DRIVE
    suspend fun downloadEncryptedBackup(
        accessToken: String,
        fileId: String
    ): ByteArray = withContext(Dispatchers.IO) {

        require(accessToken.isNotBlank()) {
            "El token de acceso está vacío."
        }

        require(fileId.isNotBlank()) {
            "El identificador del archivo está vacío."
        }

        val url = URL(
            "$DRIVE_FILES_URL/$fileId?alt=media"
        )

        val connection =
            url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"

            connection.setRequestProperty(
                "Authorization",
                "Bearer $accessToken"
            )

            connection.setRequestProperty(
                "Accept",
                "application/octet-stream"
            )

            connection.connectTimeout = 15000
            connection.readTimeout = 30000

            val responseCode = connection.responseCode

            if (responseCode !in 200..299) {

                val errorResponse = connection.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: ""

                throw Exception(
                    "Error al descargar el respaldo ($responseCode): $errorResponse"
                )
            }

            val contentLength = connection.contentLengthLong

            require(contentLength <= MAX_FILE_SIZE) {
                "El respaldo supera el tamaño permitido de 64 MB."
            }

            connection.inputStream.use { input ->
                readLimited(input)
            }

        } finally {
            connection.disconnect()
        }
    }
}
