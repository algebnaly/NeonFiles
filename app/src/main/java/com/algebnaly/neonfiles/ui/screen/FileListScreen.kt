package com.algebnaly.neonfiles.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.filesystem.utils.isApkFile
import com.algebnaly.neonfiles.filesystem.utils.isDirectorySafe
import com.algebnaly.neonfiles.filesystem.utils.isImage
import com.algebnaly.neonfiles.filesystem.utils.toContentUri
import com.algebnaly.neonfiles.filesystem.utils.toContentUriString
import com.algebnaly.neonfiles.ui.MainViewModel
import com.algebnaly.neonfiles.ui.NeonFilesAuthority
import com.algebnaly.neonfiles.ui.OperationMode
import com.algebnaly.neonfiles.ui.PathViewState
import com.algebnaly.neonfiles.ui.components.FileListView
import com.algebnaly.neonfiles.ui.components.FileView
import com.algebnaly.neonfiles.ui.components.ProgressViewModel
import com.algebnaly.neonfiles.ui.components.SelectableFileView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.io.path.isDirectory
import androidx.core.net.toUri
import coil3.ImageLoader
import coil3.video.VideoFrameDecoder
import com.algebnaly.neonfiles.filesystem.utils.isText
import com.algebnaly.neonfiles.filesystem.utils.isVideo
import com.algebnaly.neonfiles.ui.utils.NioPathFetcher
import com.algebnaly.neonfiles.utils.startApkInstallationIntent

@Composable
fun FileListScreen(viewState: MainViewModel, progressViewModel: ProgressViewModel) {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(NioPathFetcher.Factory())
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentPath by viewState.currentPath.collectAsState()
    val operationMode by viewState.operationMode.collectAsState()
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
    FileListView(viewState = viewState, progressViewModel = progressViewModel, imageLoader)
}

@Composable
fun SelectModeFileItemCard(
    file: PathViewState,
    viewState: MainViewModel,
    imageLoader: ImageLoader
) {
    val selectedPathSet by viewState.selectedPathSet.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (selectedPathSet.contains(file.path)) {
                        viewState.selectedPathSet.update { f ->
                            f.minusElement(file.path)
                        }
                    } else {
                        viewState.selectedPathSet.update { f ->
                            f.plusElement(file.path)
                        }
                    }
                }
            )
    )
    {
        SelectableFileView(selected = selectedPathSet.contains(file.path)) {
            FileView(file, imageLoader)
        }
        Text(text = file.name)
    }
}

@Composable
fun ListItemCard(item: PathViewState, viewState: MainViewModel, imageLoader: ImageLoader) {
    val context = LocalContext.current

    val fileToInstall = remember { mutableStateOf<PathViewState?>(null) }
    val installPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || context.packageManager.canRequestPackageInstalls()) {
            fileToInstall.value?.let { file ->
                startApkInstallationIntent(context, file.path)
            }
        }
        fileToInstall.value = null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (item.isDirectory) {
                        viewState.currentPath.value = item.path
                    } else {
                        var mimeType = item.mimeType
                        if (isApkFile(mimeType)) {
                            if (!context.packageManager.canRequestPackageInstalls()) {
                                fileToInstall.value = item
                                val permissionIntent =
                                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                        data = "package:${context.packageName}".toUri()
                                    }
                                installPermissionLauncher.launch(permissionIntent)
                            } else {
                                startApkInstallationIntent(context, item.path)
                            }
                        } else {
                            val uri = item.path.toContentUri()
                            if (mimeType == "") {
                                mimeType = "*/*"
                            }
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }, onLongClick = {
                    viewState.selectedPathSet.update { s ->
                        s.plusElement(item.path)
                    }
                    viewState.operationMode.value = OperationMode.Select
                })
    ) {
        FileView(item, imageLoader)
        Text(text = item.name)
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

        operationMode == OperationMode.Select -> {
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
