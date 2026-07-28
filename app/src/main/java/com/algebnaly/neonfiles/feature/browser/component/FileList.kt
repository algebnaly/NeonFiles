package com.algebnaly.neonfiles.feature.browser.component

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algebnaly.neonfiles.ui.PathViewState
import java.nio.file.Path
import androidx.compose.foundation.lazy.items

@Composable
fun FileList(
    files: List<PathViewState>,
    selectedPaths: Set<Path>,
    isSelectMode: Boolean,
    onFileClick: (PathViewState) -> Unit,
    onFileLongClick: (PathViewState) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(
            items = files,
            key = { item -> item.uniqueKey },
        ) { item ->
            FileListItem(
                item = item,
                selected = item.path in selectedPaths,
                isSelectMode = isSelectMode,
                onClick = { onFileClick(item) },
                onLongClick = { onFileLongClick(item) },
            )
        }
    }
}

@Composable
private fun FileListItem(
    item: PathViewState,
    selected: Boolean,
    isSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        // 直接复用旧版组件，保证 UI 一致
        if (isSelectMode) {
            SelectableFileView(selected = selected) {
                FileView(file = item)
            }
        } else {
            FileView(file = item)
        }
        Text(text = item.name)
    }
}