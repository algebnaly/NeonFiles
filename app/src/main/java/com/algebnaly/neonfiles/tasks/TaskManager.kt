package com.algebnaly.neonfiles.tasks

import kotlinx.coroutines.Job
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class ProgressInfo(var current: Long, val total: Long)
data class TaskInfo(val name: String, val job: Job, var progressInfo: ProgressInfo)

typealias OnProgressType = (p: ProgressInfo) -> Unit
class TaskManager {
    private val tasks = ConcurrentHashMap<UUID, TaskInfo>()

    var onAddTask: ((UUID) -> Unit)? = null
    var onRemove: ((UUID) -> Unit)? = null
    fun addTask(id: UUID, taskInfo: TaskInfo) {
        tasks.put(id, taskInfo)
        onAddTask?.let { it(id) }
    }

    fun removeTask(id: UUID): TaskInfo? {
        onRemove?.let { it(id) }
        return tasks.remove(id)
    }

    fun cancelTask(id: UUID) {
        tasks.get(id)?.job?.cancel()
    }

    fun onProgress(p: ProgressInfo, id: UUID) {
        getTaskInfo(id)?.progressInfo = p// remember, this operation is not thread safe, but for our case, this is safe.

    }

    fun getTaskInfo(id: UUID): TaskInfo? {
        return tasks.get(id)
    }
}