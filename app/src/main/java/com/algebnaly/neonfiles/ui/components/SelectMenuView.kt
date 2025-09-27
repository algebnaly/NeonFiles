package com.algebnaly.neonfiles.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.R
import com.algebnaly.neonfiles.ui.MainViewModel
import com.algebnaly.neonfiles.ui.OperationMode

@Composable
fun SelectMenuView(viewModel: MainViewModel) {
    val copy_operation_name = stringResource(R.string.copy_operation_name)
    val cut_operation_name = stringResource(R.string.cut_operation_name)
    val delete_operation_name = stringResource(R.string.delete_operation_name)
    val rename_operation_name = stringResource(R.string.rename_operation_name)
    val more_operation_name = stringResource(R.string.more_operation_name)

    val context = LocalContext.current

    Row(modifier = Modifier.fillMaxSize()) {
        BottomMenuItem(
            Icons.Outlined.FileCopy,
            label = copy_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    viewModel.operationMode.value = OperationMode.Copy
                }
        )
        BottomMenuItem(
            Icons.Outlined.ContentCut,
            label = cut_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    Toast.makeText(context, "not implemented", Toast.LENGTH_SHORT).show()
                }
        )
        BottomMenuItem(
            Icons.Outlined.Delete,
            label = delete_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    viewModel.fileOperationManager.doDelete(viewModel.selectedPathSet.value)
                    viewModel.selectedPathSet.value = emptySet()
                    viewModel.operationMode.value = OperationMode.Browser

                }
        )
        BottomMenuItem(
            Icons.Outlined.Edit,
            label = rename_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    Toast.makeText(context, "not implemented", Toast.LENGTH_SHORT).show()
                }
        )
        BottomMenuItem(
            Icons.Outlined.MoreVert,
            label = more_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clickable {
                    Toast.makeText(context, "not implemented", Toast.LENGTH_SHORT).show()
                }
        )
    }
}

@Composable
fun BottomMenuItem(
    imageVector: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        ) {
        Icon(
            imageVector,
            contentDescription = label,
        )
        Text(label)
    }
}