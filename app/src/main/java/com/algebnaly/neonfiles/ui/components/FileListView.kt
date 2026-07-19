package com.algebnaly.neonfiles.ui.components

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.algebnaly.neonfiles.ui.MainViewModel
import com.algebnaly.neonfiles.ui.OperationMode
import com.algebnaly.neonfiles.ui.screen.ListItemCard
import com.algebnaly.neonfiles.ui.screen.SelectModeFileItemCard

@Composable
fun FileListView(
    viewState: MainViewModel,
    progressViewModel: ProgressViewModel,
    imageLoader: ImageLoader
) {
    val uiState by viewState.uiState.collectAsStateWithLifecycle()

    val operationMode = uiState.mode
    val isLoading = uiState.isLoading
    val itemsList = uiState.files

    val filteredList = itemsList.filter { !it.name.startsWith(".") }

    val bottomMenuHeight = 56.dp

    val lazyColumnBottomPadding =
        if (operationMode != OperationMode.Browser) bottomMenuHeight + 12.dp else 12.dp

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("正在连接...")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { viewState.cancelLoading() }) {
                    Text("取消")
                }
            }
        } else if (filteredList.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(
                    bottom = lazyColumnBottomPadding
                ),
            ) {
                items(
                    filteredList,
                    key = { item -> item.uniqueKey }
                ) { item ->
                    if (operationMode == OperationMode.Select)
                        SelectModeFileItemCard(item, viewState, imageLoader)
                    else {
                        ListItemCard(item = item, viewState, imageLoader)
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
