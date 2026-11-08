package com.example.finconapp.ui.viewmodel

import com.example.finconapp.data.backup.BackupData

data class BackupUiState(
    val isProcessing: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val pendingRestore: BackupData? = null
)