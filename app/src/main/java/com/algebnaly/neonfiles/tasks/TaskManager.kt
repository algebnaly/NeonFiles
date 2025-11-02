package com.algebnaly.neonfiles.tasks

import kotlinx.coroutines.Job
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class TaskInfo(val name: String, val job: Job, var progression: Float)

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

    fun onProgress(progress: Float, id: UUID) {
        getTaskInfo(id)?.progression = progress// remember, this operation is not thread safe, but for our case, this is safe.
    }

    fun getTaskInfo(id: UUID): TaskInfo? {
        return tasks.get(id)
    }
}