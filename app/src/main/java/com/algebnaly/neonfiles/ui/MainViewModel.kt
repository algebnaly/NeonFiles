package com.algebnaly.neonfiles.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algebnaly.neonfiles.data.LocationItem
import com.algebnaly.neonfiles.filesystem.FsProvider
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.filesystem.utils.getMimeType
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

enum class OperationMode {
    Browser,
    Select,
    Copy,
    Cut
}

class MainViewModel(val initialPath: Path, val fsProvider: FsProvider, val fileOperationManager: BackgroundFileOperationManager) : ViewModel() {
    val currentPath: MutableStateFlow<Path> = MutableStateFlow(initialPath)

    private val _fileItems = MutableStateFlow<List<PathViewState>>(emptyList())
    val fileItems: StateFlow<List<PathViewState>> = _fileItems.asStateFlow()


    private val _refreshTrigger = MutableStateFlow(0);
    val refreshTrigger = _refreshTrigger.asStateFlow()

    val selectedPathSet: MutableStateFlow<Set<Path>> = MutableStateFlow(emptySet())
    val operationMode: MutableStateFlow<OperationMode> = MutableStateFlow(OperationMode.Browser)

    private val _toastFlow = MutableSharedFlow<String>()
    val toastFlow: SharedFlow<String> = _toastFlow

    var showHidden: Boolean = false

    init {
        viewModelScope.launch {
            fileOperationManager.eventFlow.collect(){
                    event ->
                refresh()
            }
        }
        viewModelScope.launch {
            combine(currentPath, refreshTrigger) { _, _ ->
            }.collectLatest {
                loadFileList()
            }
        }
    }

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

    fun loadFileList() {
        viewModelScope.launch(Dispatchers.IO) {
            val path = currentPath.value
            try {
                val pathList = Files.list(path).use { it.collect(Collectors.toList()) }
                val pathViewStateList = pathList.map {path->
                    PathViewState(
                        path = path,
                        name = path.fileName.toString(),
                        mimeType = Files.probeContentType(path) ?: ""
                    )
                }.sortedBy { it.name }
                _fileItems.value = pathViewStateList
            } catch (e: Exception) {
                _fileItems.value = emptyList()
            }
        }
    }

    fun sendToast(message: String) {
        viewModelScope.launch {
            _toastFlow.emit(message)
        }
    }
}