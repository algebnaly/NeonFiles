package com.algebnaly.neonfiles.ui.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algebnaly.neonfiles.data.LocationRepository
import com.algebnaly.neonfiles.filesystem.StorageConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NFS4EditLocationViewModel(
    savedStateHandle: SavedStateHandle,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val locationId: Int = checkNotNull(savedStateHandle["locationId"])

    private val _uiState = MutableStateFlow(NFS4EditLocationUiState())
    val uiState: StateFlow<NFS4EditLocationUiState> = _uiState

    init {
        viewModelScope.launch {
            val location = locationRepository.observe(locationId).firstOrNull()
            if (location != null) {
                val serverAddress = (location.config as? StorageConfig.NFS)?.serverAddress ?: ""
                _uiState.value = NFS4EditLocationUiState(
                    id = location.id,
                    name = location.name,
                    serverAddress = serverAddress,
                    serverPort = 2049, // Assuming default or add from config if existing
                    path = location.path
                )
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

    fun updatePath(newPath: String) {
        _uiState.value = _uiState.value.copy(path = newPath)
    }

    fun updateName(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
    }

    suspend fun saveLocation() {
        locationRepository.save(uiState.value.toStorageLocation())
    }

    suspend fun deleteLocation() {
        locationRepository.delete(uiState.value.toStorageLocation())
    }
}
