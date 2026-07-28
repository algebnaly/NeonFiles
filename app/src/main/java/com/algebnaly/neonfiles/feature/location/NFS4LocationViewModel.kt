package com.algebnaly.neonfiles.feature.location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.algebnaly.neonfiles.data.LocationRepository
import com.algebnaly.neonfiles.filesystem.StorageConfig
import com.algebnaly.neonfiles.ui.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Nfs4LocationViewModel(
    savedStateHandle: SavedStateHandle,
    private val locationRepository: LocationRepository,
    ) : ViewModel() {
    private val route = savedStateHandle.toRoute<Screen.NfsLocation>()
    private val locationId: Int? = route.locationId?.takeIf { it > 0 }
    private val _uiState = MutableStateFlow(Nfs4LocationUiState())
    val uiState: StateFlow<Nfs4LocationUiState> = _uiState


    init {
        // 2. 如果是编辑模式，异步从数据库获取既有配置并填充到 _uiState
        if (locationId != null) {
            viewModelScope.launch {
                val location = locationRepository.observe(locationId).firstOrNull()
                if (location != null) {
                    val serverAddress = (location.config as? StorageConfig.NFS)?.serverAddress ?: ""
                    _uiState.update {
                        Nfs4LocationUiState(
                            id = location.id,
                            name = location.name,
                            serverAddress = serverAddress,
                            serverPort = 2049,
                            path = location.path
                        )
                    }
                }
            }
        }
    }

    fun updateServerAddress(newAddress: String) {
        _uiState.value = _uiState.value.copy(serverAddress = newAddress)
    }
    @Suppress("unused")
    fun updateServerPort(newPort: Short) {
        _uiState.value = _uiState.value.copy(serverPort = newPort)
    }
    fun updatePath(newPath: String){
        _uiState.value = _uiState.value.copy(path = newPath)
    }

    fun updateName(newName: String){
        _uiState.value = _uiState.value.copy(name = newName)
    }

    suspend fun saveLocation(){
        locationRepository.save(uiState.value.toStorageLocation())
    }
}