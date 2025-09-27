package com.algebnaly.neonfiles.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.algebnaly.neonfiles.filesystem.utils.getMimeType
import com.algebnaly.neonfiles.filesystem.utils.isAudio
import com.algebnaly.neonfiles.filesystem.utils.isDirectorySafe
import com.algebnaly.neonfiles.filesystem.utils.isImage
import com.algebnaly.neonfiles.filesystem.utils.isVideo
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Composable
fun FileView(file: Path) {
    if (file.isDirectorySafe()) {
        Icon(
            Icons.Filled.Folder,
            contentDescription = "file",
            modifier = Modifier
                .size(63.dp)
                .padding(start = 11.dp, end = 12.dp),
        )
    } else {
        val iconModifier = Modifier
            .size(64.dp)
            .padding(start = 12.dp, end = 12.dp)
        val mime = getMimeType(file.absolutePathString())
        if (isImage(mime)) {
            AsyncImage(
                model = file.toFile(),
                contentDescription = "image file",
                modifier = iconModifier
            )
            // TODO: preview
        } else if (isAudio(mime)) {
            Icon(
                Icons.Outlined.AudioFile,
                contentDescription = "audio file",
                modifier = iconModifier
            )
        } else if (isVideo(mime)) {
            // TODO: preview
        } else {
            Icon(
                Icons.Outlined.Description,
                contentDescription = "normal file",
                modifier = iconModifier
            )
        }
    }
}


@Composable
fun EmptyFolder() {
    val icon_modifier = Modifier
        .size(128.dp)
        .padding(start = 12.dp, end = 12.dp)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                Icons.Outlined.FolderOpen,
                contentDescription = "empty folder",
                modifier = icon_modifier
            )
            Text("文件夹为空")
        }
    }
}

@Composable
fun SelectableFileView(
    selected: Boolean = false,
    content: @Composable (() -> Unit) = {},
) {
    Box {
        content()
        if (selected) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "checked",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
            )
        } else {
            Icon(
                Icons.Outlined.Circle,
                contentDescription = "unchecked",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}
