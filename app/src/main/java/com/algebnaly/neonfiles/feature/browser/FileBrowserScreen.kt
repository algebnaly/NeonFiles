package com.algebnaly.neonfiles.feature.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algebnaly.neonfiles.feature.browser.component.FileList
import com.algebnaly.neonfiles.feature.browser.component.LoadingContent
import com.algebnaly.neonfiles.ui.FileBrowserAction
import com.algebnaly.neonfiles.ui.FileBrowserUiState
import com.algebnaly.neonfiles.feature.browser.component.CopyMenuView
import com.algebnaly.neonfiles.feature.browser.component.EmptyFolder
import com.algebnaly.neonfiles.feature.browser.component.SelectMenuView

@Composable
fun FileBrowserScreen(
    state: FileBrowserUiState,
    onAction: (FileBrowserAction) -> Unit,
) {
    val bottomMenuHeight = 56.dp
    val lazyColumnBottomPadding = if (state.mode != OperationMode.Browser) bottomMenuHeight + 12.dp else 12.dp

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingContent(
                onCancel = { onAction(FileBrowserAction.CancelLoading) }
            )

            state.files.isEmpty() -> EmptyFolder()

            else -> FileList(
                files = state.files.filter { !it.name.startsWith(".") },
                selectedPaths = state.selectedPaths,
                isSelectMode = state.mode === OperationMode.Select,
                onFileClick = {
                    if (state.mode == OperationMode.Select) {
                        onAction(FileBrowserAction.ToggleSelection(it.path))
                    } else {
                        onAction(FileBrowserAction.Open(it.path))
                    }
                },
                onFileLongClick = { onAction(FileBrowserAction.Select(it.path)) },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = lazyColumnBottomPadding)
            )
        }

        AnimatedVisibility(
            visible = state.mode != OperationMode.Browser,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(0)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .height(bottomMenuHeight)
        ) {
            when (state.mode) {
                OperationMode.Select -> SelectMenuView(onAction = onAction)
                OperationMode.Copy -> CopyMenuView(onAction = onAction)
                else -> {}
            }
        }
    }
}