package com.algebnaly.neonfiles

import android.app.Application
import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.data.AppContainer
import com.algebnaly.neonfiles.data.AppDataContainer
import com.algebnaly.neonfiles.filesystem.StorageConfig
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NeonFilesApplication : Application() {
    companion object {
        lateinit var instance: NeonFilesApplication
            private set
    }

    val container: AppContainer by lazy { AppDataContainer(this) }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            val homeExists = container.locationRepository.observeAll().first().any {
                it.config == StorageConfig.Local && it.path == getExternalRootPath().toString()
            }
            if (!homeExists) {
                container.locationRepository.save(
                    StorageLocation(
                        name = "home",
                        path = getExternalRootPath().toString(),
                        config = StorageConfig.Local
                    )
                )
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }
}