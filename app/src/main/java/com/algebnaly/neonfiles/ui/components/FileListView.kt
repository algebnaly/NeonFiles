package com.algebnaly.neonfiles.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algebnaly.neonfiles.ui.MainViewModel
import com.algebnaly.neonfiles.ui.OperationMode
import com.algebnaly.neonfiles.ui.PathViewState
import com.algebnaly.neonfiles.ui.screen.ListItemCard
import com.algebnaly.neonfiles.ui.screen.SelectModeFileItemCard
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.use

@Composable
fun FileListView(viewState: MainViewModel, progressViewModel: ProgressViewModel) {
    val operationMode by viewState.operationMode.collectAsState()

    val itemsList: List<PathViewState> by viewState.fileItems.collectAsState()

    val bottomMenuHeight = 56.dp

    val lazyColumnBottomPadding =
        if (operationMode != OperationMode.Browser) bottomMenuHeight + 12.dp else 12.dp

    Box(modifier = Modifier.fillMaxSize()) {
        if (itemsList.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(
                    bottom = lazyColumnBottomPadding
                ),
            ) {
                items(itemsList,
                    key = { item -> item.uniqueKey }
                    ) { item ->
                    if (operationMode == OperationMode.Select)
                        SelectModeFileItemCard(item, viewState)
                    else {
                        ListItemCard(item = item, viewState)
                    }
                }
            }
        } else {
            EmptyFolder()
        }
        AnimatedVisibility(
            visible = operationMode != OperationMode.Browser,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(0)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .height(bottomMenuHeight)
        ) {
            if (operationMode == OperationMode.Select) {
                SelectMenuView(viewState)
            } else if (operationMode == OperationMode.Copy) {
                CopyMenuView(viewModel = viewState, progressViewModel = progressViewModel)
            }

        }
    }
}
