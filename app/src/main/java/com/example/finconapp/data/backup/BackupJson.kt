
package com.example.finconapp.data.backup

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

//Modelo de respaldo para convertir los datos a JSON
object BackupJson {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    // Convertir el respaldo a JSON
    fun encode(backup: BackupData): String {
        return json.encodeToString(backup)
    }

    // Convertir JSON a objeto BackupData
    fun decode(content: String): BackupData {
        return json.decodeFromString<BackupData>(content)
    }
}
