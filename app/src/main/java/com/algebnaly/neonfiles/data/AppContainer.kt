package com.algebnaly.neonfiles.data

import android.content.Context
import com.algebnaly.neonfiles.filesystem.FsProvider

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val locationRepository: LocationRepository
    val fsProvider: FsProvider
}

/**
 * [AppContainer] implementation that provides instance of [LocationRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    override val locationRepository: LocationRepository by lazy {
        LocationRepository(LocationDataBase.getDatabase(context).locationItemDao())
    }
    override val fsProvider: FsProvider = FsProvider()
}
