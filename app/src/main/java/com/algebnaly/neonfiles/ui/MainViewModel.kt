package com.algebnaly.neonfiles.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algebnaly.neonfiles.core.filesystem.StorageConnector
import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.collections.emptySet
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.text.Collator

enum class OperationMode {
    Browser,
    Select,
    Copy,
    Cut
}

class MainViewModel(
    val initialPath: Path,
    val storageConnector: StorageConnector,
    val fileOperationManager: BackgroundFileOperationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FileBrowserUiState(currentPath = initialPath)
    )

    val uiState: StateFlow<FileBrowserUiState> = _uiState.asStateFlow()

    private var locationLoadJob: Job? = null
    private var savedPath: Path? = null
    private var savedFileItems: List<PathViewState> = emptyList()

    private val _refreshTrigger = MutableStateFlow(0);
    val refreshTrigger = _refreshTrigger.asStateFlow()


    private val _effects = MutableSharedFlow<FileBrowserEffect>()
    val effects: SharedFlow<FileBrowserEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            fileOperationManager.eventFlow.collect() { event ->
                refresh()
            }
        }
        viewModelScope.launch {
            combine(
                uiState.map { it.currentPath }.distinctUntilChanged(),
                refreshTrigger
            ) { path, _ ->
                path
            }.collectLatest { path ->
                loadFileListSuspend(path)
            }
        }
    }

    fun refresh() {
        _refreshTrigger.update { it + 1 }
    }

    fun open(path: Path) {
        _uiState.update { state ->
            state.copy(currentPath = path)
        }
    }

    fun toggleSelection(path: Path) {
        _uiState.update { state ->
            val nextSelection = if (path in state.selectedPaths) {
                state.selectedPaths.minusElement(path)
            } else {
                state.selectedPaths.plusElement(path)
            }
            state.copy(selectedPaths = nextSelection)
        }
    }

    fun enterSelection(path: Path) {
        _uiState.update { state ->
            state.copy(selectedPaths = setOf(path), mode = OperationMode.Select)
        }
    }

    fun enterCopy() {
        if (uiState.value.selectedPaths.isEmpty()) return

        _uiState.update { state ->
            state.copy(mode = OperationMode.Copy)
        }
    }

    fun enterCut() {
        if (uiState.value.selectedPaths.isEmpty()) return
        _uiState.update { state ->
            state.copy(mode = OperationMode.Cut)
        }
    }

    fun returnToBrowser() {
        _uiState.update { state ->
            state.copy(selectedPaths = emptySet(), mode = OperationMode.Browser)
        }
    }

    fun openLocation(location: StorageLocation) {
        locationLoadJob?.cancel()

        savedPath = uiState.value.currentPath
        savedFileItems = uiState.value.files

        locationLoadJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true)
            }
            try {
                val path = storageConnector.connect(location)
                _uiState.update { state ->
                    state.copy(currentPath = path)
                }
            } catch (e: Exception) {
                ensureActive()
                _uiState.update { state ->
                    state.copy(isLoading = true)
                }
                _effects.emit(FileBrowserEffect.ShowMessage(e.message ?: e.toString()))
            }
        }
    }

    fun cancelLoading() {
        locationLoadJob?.cancel()
        locationLoadJob = null
        _uiState.update { state ->
            state.copy(isLoading = false)
        }
        savedPath?.let { path ->
            open(path)
            _uiState.update { state ->
                state.copy(files = savedFileItems)
            }
        }
    }

    private suspend fun loadFileListSuspend(path: Path) {
        withContext(Dispatchers.IO) {
            val path = uiState.value.currentPath
            try {
                val nameCollator = Collator.getInstance()
                val pathList = Files.list(path).use { it.collect(Collectors.toList()) }
                val pathViewStateList = pathList.map { p ->
                    PathViewState(
                        path = p,
                        name = p.fileName.toString(),
                        mimeType = Files.probeContentType(p) ?: ""
                    )
                }.sortedWith(
                    compareBy<PathViewState> {
                        if (it.isDirectory) 0 else 1
                    }.thenComparator { left, right ->
                        nameCollator.compare(left.name, right.name)
                    }
                )
                _uiState.update { state ->
                    state.copy(
                        files = pathViewStateList,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                ensureActive()
                _uiState.update { state ->
                    state.copy(
                        files = emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadFileList() {
        viewModelScope.launch {
            loadFileListSuspend(uiState.value.currentPath)
        }
    }

    fun requestOpenExternal(path: Path, mimeType: String) {
        viewModelScope.launch {
            _effects.emit(
                FileBrowserEffect.OpenExternal(
                    path = path,
                    mimeType = mimeType.ifBlank { "*/*" },
                )
            )
        }
    }

    fun showMessage(message: String) {
        viewModelScope.launch {
            _effects.emit(FileBrowserEffect.ShowMessage(message))
        }
    }
}