package com.algebnaly.neonfiles.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.algebnaly.neonfiles.R
import com.algebnaly.neonfiles.ui.MainViewModel
import com.algebnaly.neonfiles.ui.OperationMode

@Composable
fun CopyMenuView(viewModel: MainViewModel) {
    val copy_operation_name = stringResource(R.string.copy_operation_name)
    val cancel_operation_name = stringResource(R.string.cancel_operation_name)
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxSize()) {
        BottomMenuItem(
            Icons.Outlined.FileCopy,
            label = copy_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    viewModel.fileOperationManager.doCopy(
                        viewModel.selectedPathSet.value,
                        viewModel.currentPath.value
                    )
                    viewModel.selectedPathSet.value = emptySet()
                    viewModel.operationMode.value = OperationMode.Browser
                }
        )
        BottomMenuItem(
            Icons.Outlined.Cancel,
            label = cancel_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    viewModel.selectedPathSet.value = emptySet()
                    viewModel.operationMode.value = OperationMode.Browser
                }
        )

    }
}