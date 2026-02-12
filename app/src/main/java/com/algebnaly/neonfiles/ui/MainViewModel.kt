package com.algebnaly.neonfiles.ui

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.video.VideoFrameDecoder
import com.algebnaly.neonfiles.data.LocationItem
import com.algebnaly.neonfiles.filesystem.FsProvider
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.filesystem.utils.getMimeType
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManager
import com.algebnaly.neonfiles.ui.utils.NioPathFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var locationLoadJob: Job? = null
    private var savedPath: Path? = null
    private var savedFileItems: List<PathViewState> = emptyList()

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
                loadFileListSuspend()
            }
        }
    }

    fun refresh() {
        _refreshTrigger.update { it + 1 }
    }

    fun loadLocationItemAndSwitch(item: LocationItem) {
        locationLoadJob?.cancel()

        savedPath = currentPath.value
        savedFileItems = _fileItems.value

        locationLoadJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val path = withContext(Dispatchers.IO) {
                    item.toPath(fsProvider)
                }
                currentPath.value = path
                // loadFileListSuspend 会被 combine flow 触发，完成后会设置 _isLoading = false
            } catch (e: Exception) {
                ensureActive()
                _isLoading.value = false
                sendToast(e.toString())
            }
        }
    }

    fun cancelLoading() {
        locationLoadJob?.cancel()
        locationLoadJob = null
        _isLoading.value = false
        savedPath?.let { path ->
            currentPath.value = path
            _fileItems.value = savedFileItems
        }
    }

    private suspend fun loadFileListSuspend() {
        withContext(Dispatchers.IO) {
            val path = currentPath.value
            try {
                val pathList = Files.list(path).use { it.collect(Collectors.toList()) }
                val pathViewStateList = pathList.map { p ->
                    PathViewState(
                        path = p,
                        name = p.fileName.toString(),
                        mimeType = Files.probeContentType(p) ?: ""
                    )
                }.sortedBy { it.name }
                _fileItems.value = pathViewStateList
                _isLoading.value = false
            } catch (e: Exception) {
                ensureActive()
                _fileItems.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun loadFileList() {
        viewModelScope.launch {
            loadFileListSuspend()
        }
    }

    fun sendToast(message: String) {
        viewModelScope.launch {
            _toastFlow.emit(message)
        }
    }
}