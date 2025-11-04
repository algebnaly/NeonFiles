package com.algebnaly.neonfiles.ui.components
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext


@Composable
fun ProgressOverlay(progressViewModel: ProgressViewModel) {
    val uiState by progressViewModel.uiState.collectAsState()
    val context = LocalContext.current
    if(uiState.show){
        ProgressBlock(
            onHide = {
                progressViewModel.hide()
            },
            onCancel = {
                progressViewModel.cancel()
                progressViewModel.hide()
            },
            titleMessage = uiState.titleMessage,
            progressMessage = progressViewModel.progressMessage(context, current = uiState.current, total =  uiState.total),
            progression = uiState.progression
        )
    }
}