package com.algebnaly.neonfiles.ui.screen

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.filesystem.utils.isDirectorySafe
import com.algebnaly.neonfiles.ui.MainViewModel
import com.algebnaly.neonfiles.ui.OperationMode
import com.algebnaly.neonfiles.ui.components.FileListView
import com.algebnaly.neonfiles.ui.components.FileView
import com.algebnaly.neonfiles.ui.components.SelectableFileView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.io.path.isDirectory

@Composable
fun FileListScreen(viewState: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentPath by viewState.currentPath.collectAsState()
    val operationMode by viewState.operationMode.collectAsState()
    val context = LocalContext.current
    BackHandler(enabled = true) {
        handleBackPress(
            drawerState = drawerState,
            scope = scope,
            operationMode = operationMode,
            viewState = viewState,
            currentPath = currentPath,
            getExternalRootPath = {
                getExternalRootPath()
            },
            context = context
        )
    }
    Box(modifier = Modifier.fillMaxSize()){
        FileListView(viewState)
        ProgressScreen(onHide = {}, onCancel = {})
    }
}

@Composable
fun SelectModeFileItemCard(file: Path, viewState: MainViewModel) {
    val selectedPathSet by viewState.selectedPathSet.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (selectedPathSet.contains(file)) {
                        viewState.selectedPathSet.update { f ->
                            f.minusElement(file)
                        }
                    } else {
                        viewState.selectedPathSet.update { f ->
                            f.plusElement(file)
                        }
                    }
                }
            )
    )
    {
        SelectableFileView(selected = selectedPathSet.contains(file)) {
            FileView(file)
        }
        Text(text = file.fileName.toString())
    }
}

@Composable
fun ListItemCard(item: Path, viewState: MainViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (item.isDirectorySafe()) {
                        viewState.currentPath.value = item
                    } else {
                        // TODO: open file with other app
                    }
                }, onLongClick = {
                    viewState.selectedPathSet.update { s ->
                        s.plusElement(item)
                    }
                    viewState.operationMode.value = OperationMode.Select
                })
    ) {
        FileView(item)
        Text(text = item.fileName.toString())
    }
}

fun handleBackPress(
    drawerState: DrawerState,
    scope: CoroutineScope,
    operationMode: OperationMode,
    viewState: MainViewModel,
    currentPath: Path,
    getExternalRootPath: () -> Path,
    context: Context
) {
    when {
        drawerState.isOpen -> {
            scope.launch { drawerState.close() }
        }

        operationMode != OperationMode.Browser -> {
            viewState.selectedPathSet.value = emptySet()
            viewState.operationMode.value = OperationMode.Browser
        }

        currentPath.parent != null && currentPath != getExternalRootPath() -> {
            viewState.currentPath.value = currentPath.parent!!
        }

        else -> {
            (context as? Activity)?.finish()
        }
    }
}
