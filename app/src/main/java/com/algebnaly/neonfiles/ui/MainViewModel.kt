package com.algebnaly.neonfiles.ui

import androidx.lifecycle.ViewModel
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

enum class OperationMode {
    Browser,
    Select,
    Copy,
    Cut
}

class MainViewModel() : ViewModel() {
    val currentPath: MutableStateFlow<File> by lazy {
        MutableStateFlow(getExternalRootPath())
    }
    val fileOperationManager: BackgroundFileOperationManager = BackgroundFileOperationManager()
    val selectedPathSet: MutableStateFlow<Set<File>> = MutableStateFlow(emptySet())
    val operationMode: MutableStateFlow<OperationMode> = MutableStateFlow(OperationMode.Browser)
}