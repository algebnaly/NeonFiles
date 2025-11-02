package com.algebnaly.neonfiles.ui.components
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


@Composable
fun ProgressOverlay(progressViewModel: ProgressViewModel) {
    val uiState by progressViewModel.uiState.collectAsState()
    if(uiState.show){
        ProgressBlock(
            onHide = {
                progressViewModel.hide()
            },
            onCancel = {
                progressViewModel.show()
            },
            titleMessage = uiState.titleMessage,
            progressMessage = uiState.progressMessage,
            progression = uiState.progression
        )
    }
}