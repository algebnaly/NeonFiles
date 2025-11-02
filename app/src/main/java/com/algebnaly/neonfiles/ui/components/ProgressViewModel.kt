package com.algebnaly.neonfiles.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class ProgressViewModel(val backgroudFileOperationManager: BackgroundFileOperationManager): ViewModel() {
    private var currentTaskId: UUID? = null
    private val _uiState = MutableStateFlow(ProgressUiState())

    val uiState: StateFlow<ProgressUiState> = _uiState

    init {
        backgroudFileOperationManager.taskManager.onAddTask = { id ->
            onAddTask(id)
        }

        backgroudFileOperationManager.taskManager.onRemove = { id ->
            onRemove(id)
        }

        viewModelScope.launch {
            while (isActive){
                updateProgress()
                delay(1000L)
            }
        }
    }

    fun show(){
        _uiState.value = _uiState.value.copy(show = true)
    }

    fun hide(){
        _uiState.value = _uiState.value.copy(show = false)
    }

    fun updateProgress(){
        val taskInfo = currentTaskId?.let { backgroudFileOperationManager.taskManager.getTaskInfo(it) }
        taskInfo?.let {
            _uiState.value = _uiState.value.copy(
                titleMessage = taskInfo.name,
                progression = taskInfo.progression
            )
        }
    }

    fun onAddTask(id: UUID){
        currentTaskId = id
        updateProgress()
    }

    fun onRemove(id: UUID){
        if(id == currentTaskId){
            hide()
            currentTaskId = null
        }
    }
}