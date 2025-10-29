package com.algebnaly.neonfiles

import android.app.Application
import android.util.Log
import com.algebnaly.neonfiles.data.AppContainer
import com.algebnaly.neonfiles.data.AppDataContainer
import com.algebnaly.neonfiles.data.LocationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NeonFilesApplication: Application() {
    lateinit var container: AppContainer
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }
}