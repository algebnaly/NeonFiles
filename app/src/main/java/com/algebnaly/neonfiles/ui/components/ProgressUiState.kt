package com.algebnaly.neonfiles.ui.components

data class ProgressUiState(val show: Boolean = false, val current: Long = 0, val total: Long = 1, val titleMessage: String = "", val progression: Float = 0f)