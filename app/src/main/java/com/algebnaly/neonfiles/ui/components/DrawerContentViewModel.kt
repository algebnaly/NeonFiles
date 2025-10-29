package com.algebnaly.neonfiles.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algebnaly.neonfiles.data.LocationItem
import com.algebnaly.neonfiles.data.LocationRepository
import com.algebnaly.neonfiles.filesystem.FsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DrawerContentViewModel(
    private val locationRepository: LocationRepository,
    val fsProvider: FsProvider
) : ViewModel() {
    val uiState: StateFlow<DrawerContentUiState> = locationRepository.getAllLocationStream().map {
        DrawerContentUiState(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DrawerContentUiState()
    )
}

data class DrawerContentUiState(
    val locations: List<LocationItem> = emptyList()
)