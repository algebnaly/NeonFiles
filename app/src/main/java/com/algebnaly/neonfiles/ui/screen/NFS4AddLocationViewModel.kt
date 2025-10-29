package com.algebnaly.neonfiles.ui.screen

import androidx.lifecycle.ViewModel
import com.algebnaly.neonfiles.data.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NFS4AddLocationViewModel(
    private val locationRepository: LocationRepository,
    ) : ViewModel() {
    private val _uiState = MutableStateFlow(NFS4AddLocationUiState())
    val uiState: StateFlow<NFS4AddLocationUiState> = _uiState
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
        locationRepository.insertLocation(uiState.value.toLocationItem())
    }
}