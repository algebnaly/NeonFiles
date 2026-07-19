package com.algebnaly.neonfiles.data

import android.content.Context
import com.algebnaly.neonfiles.core.filesystem.NioStorageConnector
import com.algebnaly.neonfiles.core.filesystem.StorageConnector
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
    val storageConnector: StorageConnector

    val fileOperationManager: BackgroundFileOperationManager
}

/**
 * [AppContainer] implementation that provides instance of [LocationRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    override val locationRepository: LocationRepository by lazy {
        OfflineLocationRepository(LocationDatabase.getDatabase(context).locationEntityDao())
    }

    override val fsProvider: FsProvider = FsProvider()

    override val storageConnector: StorageConnector = NioStorageConnector(fsProvider)

    override val fileOperationManager: BackgroundFileOperationManager =  BackgroundFileOperationManager(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )
}
