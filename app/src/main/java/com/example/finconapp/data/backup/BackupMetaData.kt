package com.example.finconapp.data.backup

data class BackupMetadata(
    val backupId: String,
    val backupVersion: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val deviceId: String
)