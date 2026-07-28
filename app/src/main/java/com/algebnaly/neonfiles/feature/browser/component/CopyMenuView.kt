package com.algebnaly.neonfiles.feature.browser.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.algebnaly.neonfiles.R
import com.algebnaly.neonfiles.ui.FileBrowserAction

@Composable
fun CopyMenuView(onAction: (FileBrowserAction) -> Unit) {
    val copy_operation_name = stringResource(R.string.copy_operation_name)
    val cancel_operation_name = stringResource(R.string.cancel_operation_name)
    Row(modifier = Modifier.fillMaxSize()) {
        BottomMenuItem(
            Icons.Outlined.FileCopy,
            label = copy_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    onAction(FileBrowserAction.Paste)
                }
        )
        BottomMenuItem(
            Icons.Outlined.Cancel,
            label = cancel_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    onAction(FileBrowserAction.CancelPendingCopy)
                }
        )

    }
}