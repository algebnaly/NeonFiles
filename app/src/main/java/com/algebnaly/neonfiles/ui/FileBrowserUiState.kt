package com.algebnaly.neonfiles.ui

import java.nio.file.Path

data class FileBrowserUiState(
    val currentPath: Path,
    val files: List<PathViewState> = emptyList(),
    val selectedPaths: Set<Path> = emptySet(),
    val mode: OperationMode = OperationMode.Browser,
    val isLoading: Boolean = false,
    val showHidden: Boolean = false,
    val errorMessage: String? = null,
)

