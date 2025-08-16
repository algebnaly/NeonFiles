package com.algebnaly.neonfiles.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.File

enum class OperationMode {
    Browser,
    Select,
    Copy,
    Cut
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext: Context = application.applicationContext
    val currentPath: MutableStateFlow<File> by lazy {
        MutableStateFlow(getExternalRootPath())
    }
    val fileOperationManager: BackgroundFileOperationManager = BackgroundFileOperationManager(viewModelScope, appContext)
    val selectedPathSet: MutableStateFlow<Set<File>> = MutableStateFlow(emptySet())
    val operationMode: MutableStateFlow<OperationMode> = MutableStateFlow(OperationMode.Browser)
    val toastMessageEvent: SharedFlow<String?> = fileOperationManager.operationEvents
}