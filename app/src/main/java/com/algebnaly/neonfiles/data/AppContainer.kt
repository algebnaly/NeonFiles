package com.algebnaly.neonfiles.data

import android.content.Context
import com.algebnaly.neonfiles.filesystem.FsProvider
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val locationRepository: LocationRepository
    val fsProvider: FsProvider

    val fileOperationManager: BackgroundFileOperationManager
}

/**
 * [AppContainer] implementation that provides instance of [LocationRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    override val locationRepository: LocationRepository by lazy {
        LocationRepository(LocationDataBase.getDatabase(context).locationItemDao())
    }
    override val fsProvider: FsProvider = FsProvider()

    override val fileOperationManager: BackgroundFileOperationManager =  BackgroundFileOperationManager(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )
}
