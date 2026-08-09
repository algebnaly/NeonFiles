package com.algebnaly.neonfiles.feature.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.ui.AppViewModelProvider

@Composable
fun NFS4LocationRoute(
    onBack: () -> Unit,
    nfs4LocationViewModel: Nfs4LocationViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState by nfs4LocationViewModel.uiState.collectAsState()
    Nfs4LocationScreen(
        onBack = onBack,
        name= uiState.name,
        onUpdateName = {
            nfs4LocationViewModel.updateName(it)
        },
        serverAddress = uiState.serverAddress,
        onUpdateServerAddress = {
            nfs4LocationViewModel.updateServerAddress(it)
        },
        path = uiState.path,
        onUpdatePath = {
            nfs4LocationViewModel.updatePath(it)
        },
        onSaveLocation = {
            nfs4LocationViewModel.saveLocation(onSaved = onBack)
        },
        isEditMode = uiState.isEditMode,
        onDeleteLocation = {
            nfs4LocationViewModel.deleteLocation(onDeleted = onBack)
        },
        warningMessage = uiState.warningMessage
    )
}