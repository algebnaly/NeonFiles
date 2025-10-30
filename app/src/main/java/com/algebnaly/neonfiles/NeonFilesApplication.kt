package com.algebnaly.neonfiles

import android.app.Application
import android.util.Log
import com.algebnaly.neonfiles.data.AppContainer
import com.algebnaly.neonfiles.data.AppDataContainer
import com.algebnaly.neonfiles.data.LocationItem
import com.algebnaly.neonfiles.filesystem.FsConfig
import com.algebnaly.neonfiles.filesystem.FsType
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NeonFilesApplication: Application() {
    lateinit var container: AppContainer
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        appScope.launch {
            val homeExists = container.locationRepository.getAllLocationStream().first().any {
                it.fsType == FsType.Local && it.path == getExternalRootPath().toString()
            }
            if(!homeExists){
                container.locationRepository.insertLocation(LocationItem(
                    name = "home",
                    fsType = FsType.Local,
                    path = getExternalRootPath().toString(),
                    fsConfig = FsConfig.Local
                ))
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }
}