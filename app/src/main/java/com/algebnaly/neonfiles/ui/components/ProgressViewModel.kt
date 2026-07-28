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
import kotlin.time.Duration.Companion.milliseconds

class ProgressViewModel(val backgroundFileOperationManager: BackgroundFileOperationManager): ViewModel() {
    private var currentTaskId: UUID? = null
    private val _uiState = MutableStateFlow(ProgressUiState())

    val uiState: StateFlow<ProgressUiState> = _uiState

    init {
        backgroundFileOperationManager.taskManager.onAddTask = { id ->
            onAddTask(id)
        }

        backgroundFileOperationManager.taskManager.onRemove = { id ->
            onRemove(id)
        }

        viewModelScope.launch {
            while (isActive){
                updateProgress()
                delay(1000L.milliseconds)
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
            backgroundFileOperationManager.taskManager.cancelTask(id)
        }
    }

    fun updateProgress(){
        val taskInfo = currentTaskId?.let { backgroundFileOperationManager.taskManager.getTaskInfo(it) }
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
        show()
    }

    fun onRemove(id: UUID){
        if(id == currentTaskId){
            hide()
            currentTaskId = null
        }
    }
}