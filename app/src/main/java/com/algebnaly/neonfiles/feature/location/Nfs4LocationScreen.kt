package com.algebnaly.neonfiles.feature.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.algebnaly.neonfiles.R
import kotlinx.coroutines.launch

@Composable
fun Nfs4LocationScreen(
    onBack: () -> Unit = {},
    name: String = "",
    onUpdateName: (value: String) -> Unit,
    serverAddress: String = "",
    onUpdateServerAddress: (value: String) -> Unit,
    path: String = "",
    onUpdatePath: (value: String) -> Unit,
    onSaveLocation: () -> Unit,
    isEditMode: Boolean = false,
    onDeleteLocation: () -> Unit = {},
    warningMessage: String = "",
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)

    ) {
        androidx.compose.material3.OutlinedTextField(
            value = name,
            onValueChange = onUpdateName,
            label = { Text(stringResource(id = R.string.location_item_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        androidx.compose.material3.OutlinedTextField(
            value = serverAddress,
            onValueChange = onUpdateServerAddress,
            label = { Text(stringResource(id = R.string.server_address_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        androidx.compose.material3.OutlinedTextField(
            value = path,
            onValueChange = onUpdatePath,
            label = { Text(stringResource(id = R.string.export_path_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(id = R.string.cancel_button_name))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        onSaveLocation()
                        onBack()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(id = R.string.save_button_name))
            }
        }

        if (isEditMode) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.TextButton(
                    onClick = onDeleteLocation,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.delete_operation_name))
                }
            }
        }

        if (warningMessage.isNotEmpty()) {
            Text(
                text = warningMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}