package com.algebnaly.neonfiles.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algebnaly.neonfiles.data.LocationItem
import com.algebnaly.neonfiles.filesystem.FsProvider
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Path

enum class OperationMode {
    Browser,
    Select,
    Copy,
    Cut
}

class MainViewModel(val fsProvider: FsProvider, val fileOperationManager: BackgroundFileOperationManager) : ViewModel() {
    val currentPath: MutableStateFlow<Path> by lazy {
        MutableStateFlow(getExternalRootPath())
    }

    init {
        viewModelScope.launch {
            fileOperationManager.eventFlow.collect(){
                event ->
                refresh()
            }
        }
    }

    private val _refreshTrigger = MutableStateFlow(0);
    val refreshTrigger = _refreshTrigger.asStateFlow()

    val selectedPathSet: MutableStateFlow<Set<Path>> = MutableStateFlow(emptySet())
    val operationMode: MutableStateFlow<OperationMode> = MutableStateFlow(OperationMode.Browser)

    private val _toastFlow = MutableSharedFlow<String>()
    val toastFlow: SharedFlow<String> = _toastFlow

    fun refresh() {
        _refreshTrigger.update { it + 1 }
    }

    fun loadLocationItemAndSwitch(item: LocationItem) {
        viewModelScope.launch {
            try {
                val path = item.toPath(fsProvider)
                currentPath.value = path
            } catch (e: Exception) {
                sendToast(e.toString())
            }
        }
    }

    fun sendToast(message: String) {
        viewModelScope.launch {
            _toastFlow.emit(message)
        }
    }
}