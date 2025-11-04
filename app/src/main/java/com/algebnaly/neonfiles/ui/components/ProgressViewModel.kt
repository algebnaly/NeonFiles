package com.algebnaly.neonfiles.ui.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.text.format.Formatter
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

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

    fun cancel(){
        currentTaskId?.let {
            id ->
            backgroudFileOperationManager.taskManager.cancelTask(id)
        }
    }

    fun updateProgress(){
        val taskInfo = currentTaskId?.let { backgroudFileOperationManager.taskManager.getTaskInfo(it) }
        taskInfo?.let {
            t ->
            _uiState.value = _uiState.value.copy(
                titleMessage = t.name,
                current = t.progressInfo.current,
                total = t.progressInfo.total,
                progression = t.progressInfo.current.toFloat()/max(t.progressInfo.total, 1)
            )
        }
    }

    fun progressMessage(context: Context, current: Long, total: Long): String{
        val currentStr = Formatter.formatShortFileSize(context, current)
        val totalStr = Formatter.formatShortFileSize(context, total)
        return "$currentStr/$totalStr"
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