package com.algebnaly.neonfiles.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.data.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DrawerContentViewModel(
    locationRepository: LocationRepository,
) : ViewModel() {
    val uiState: StateFlow<DrawerContentUiState> = locationRepository.observeAll().map(){
        locations ->
        DrawerContentUiState(locations)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DrawerContentUiState()
    )
}

data class DrawerContentUiState(
    val locations: List<StorageLocation> = emptyList()
)