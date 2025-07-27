package com.algebnaly.neonfiles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.R
import com.algebnaly.neonfiles.ui.MainViewModel

@Composable
fun SelectMenuView(viewModel: MainViewModel = viewModel()) {
    val copy_operation_name = stringResource(R.string.copy_operation_name)
    val cut_operation_name = stringResource(R.string.cut_operation_name)
    val delete_operation_name = stringResource(R.string.delete_operation_name)
    val rename_operation_name = stringResource(R.string.rename_operation_name)
    val more_operation_name = stringResource(R.string.more_operation_name)


    Row(modifier = Modifier.fillMaxSize()) {
        SelectMenuItem(
            Icons.Outlined.FileCopy,
            label = copy_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
        SelectMenuItem(
            Icons.Outlined.ContentCut,
            label = cut_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
        SelectMenuItem(
            Icons.Outlined.Delete,
            label = delete_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
        SelectMenuItem(
            Icons.Outlined.Edit,
            label = rename_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
        SelectMenuItem(
            Icons.Outlined.MoreVert,
            label = more_operation_name,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )
    }
}

@Composable
fun SelectMenuItem(
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