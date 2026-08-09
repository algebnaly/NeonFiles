package com.algebnaly.neonfiles.feature.browser

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.platform.intent.openWithExternalApplication
import com.algebnaly.neonfiles.ui.AppViewModelProvider
import com.algebnaly.neonfiles.ui.FileBrowserAction
import com.algebnaly.neonfiles.ui.FileBrowserEffect
import kotlinx.coroutines.flow.Flow

@Composable
fun FileBrowserRoute(
    fileBrowserViewModel: FileBrowserViewModel = viewModel(factory = AppViewModelProvider.Factory),
    openLocationEvents: Flow<StorageLocation>,
    ) {
    val state by fileBrowserViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(fileBrowserViewModel, openLocationEvents) {
        fileBrowserViewModel.observeOpenLocationEvents(
            openLocationEvents,
        )
    }

    val context = LocalContext.current

    LaunchedEffect(fileBrowserViewModel) {
        fileBrowserViewModel.effects.collect { effect ->
            when (effect) {
                is FileBrowserEffect.ShowMessage -> {
                    Toast.makeText(
                        context,
                        effect.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                is FileBrowserEffect.OpenExternal -> {
                    openWithExternalApplication(
                        context = context,
                        path = effect.path,
                        mimeType = effect.mimeType,
                    )
                }
            }
        }
    }

    BackHandler(enabled = true) {
        when {
            state.mode == OperationMode.Select -> fileBrowserViewModel.onAction(FileBrowserAction.CancelSelection)
            state.currentPath.parent != null
                    && state.currentPath != getExternalRootPath() -> {
                fileBrowserViewModel.open(state.currentPath.parent!!)
            }

            else -> (context as? Activity)?.finish()
        }
    }

    FileBrowserScreen(
        state = state,
        onAction = fileBrowserViewModel::onAction,
    )
}