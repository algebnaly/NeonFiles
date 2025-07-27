package com.algebnaly.neonfiles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.ui.MainViewModel
import com.algebnaly.neonfiles.ui.theme.NeonFilesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.net.toUri
import com.algebnaly.neonfiles.ui.OperationMode
import com.algebnaly.neonfiles.ui.components.EmptyFolder
import com.algebnaly.neonfiles.ui.components.FileView
import com.algebnaly.neonfiles.ui.components.SelectMenuView
import com.algebnaly.neonfiles.ui.components.SelectableFileView
import kotlinx.coroutines.flow.update


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Environment.isExternalStorageManager()) {
            try {
                val intent: Intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData("package:$packageName".toUri())
                startActivity(intent)
            } catch (e: Exception) {
                val intent: Intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }

        enableEdgeToEdge()
        setContent {
            NeonFilesTheme {
                MainDrawer()
            }
        }
    }
}

@Composable
fun MainDrawer(viewState: MainViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentPath by viewState.currentPath.collectAsState()
    val operationMode by viewState.operationMode.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (operationMode == OperationMode.Select) {
            viewState.selectedPathSet.value = emptySet()
            viewState.operationMode.value = OperationMode.Browser
        } else if (currentPath.absolutePath != getExternalRootPath().absolutePath) {
            viewState.currentPath.value = currentPath.parentFile!!
        } else {
            (context as? Activity)?.finish()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("drawer item 1", modifier = Modifier.padding(16.dp))
                Text("drawer item 2", modifier = Modifier.padding(16.dp))
            }
        }
    ) {
        Scaffold(topBar = { MainTopAppBar(scope, drawerState) }) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                FileListView()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(scope: CoroutineScope, drawerState: DrawerState) {
    TopAppBar(title = { Text("NeonFiles") }, navigationIcon = {
        IconButton(onClick = {
            scope.launch {
                if (drawerState.isClosed) {
                    drawerState.open()
                } else {
                    drawerState.close()
                }
            }
        }) {
            Icon(Icons.Filled.Menu, contentDescription = "open drawer")
        }
    })
}

@Composable
fun FileListView(viewState: MainViewModel = viewModel()) {
    val operationMode by viewState.operationMode.collectAsState()
    val currentPath by viewState.currentPath.collectAsState()
    val itemsList = currentPath.listFiles() ?: emptyArray()
    val selectMenuHeight = 64.dp

    if (itemsList.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 0.dp,
                    top = 0.dp,
                    end = 0.dp,
                    bottom = 12.dp
                ),
            ) {
                items(itemsList) { item ->
                    if (operationMode == OperationMode.Select)
                        SelectModeFileItemCard(item)
                    else {
                        ListItemCard(item = item)
                    }
                }
                item {
                    AnimatedVisibility(visible = operationMode == OperationMode.Select) {
                        Box(modifier = Modifier.height(selectMenuHeight))
                    }
                }
            }
            AnimatedVisibility(
                visible = operationMode == OperationMode.Select,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .height(selectMenuHeight)
            ) {
                SelectMenuView()
            }
        }
    } else {
        EmptyFolder()
    }
}

@Composable
fun SelectModeFileItemCard(file: File, viewState: MainViewModel = viewModel()) {
    val selectedPathSet by viewState.selectedPathSet.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (selectedPathSet.contains(file)) {
                        viewState.selectedPathSet.update { f ->
                            f - file
                        }
                    } else {
                        viewState.selectedPathSet.update { f ->
                            f + file
                        }
                    }
                }
            )
    )
    {
        SelectableFileView(selected = selectedPathSet.contains(file)) {
            FileView(file)
        }
        Text(text = file.name)
    }
}

@Composable
fun ListItemCard(item: File, viewState: MainViewModel = viewModel()) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (item.isDirectory) {
                        viewState.currentPath.value = item
                    } else {
                        // TODO: open file with other app
                    }
                }, onLongClick = {
                    viewState.selectedPathSet.update { s ->
                        s + item
                    }
                    viewState.operationMode.value = OperationMode.Select
                })
    ) {
        FileView(item)
        Text(text = item.name)
    }
}


