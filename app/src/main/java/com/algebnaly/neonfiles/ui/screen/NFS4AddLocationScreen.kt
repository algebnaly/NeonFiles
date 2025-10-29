package com.algebnaly.neonfiles.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.R
import com.algebnaly.neonfiles.ui.AppViewModelProvider
import kotlinx.coroutines.launch

@Composable
fun NFS4AddLocationScreen(
    onBack: () -> Unit = {},
    nfS4AddLocationViewModel: NFS4AddLocationViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val uiState by nfS4AddLocationViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(id=R.string.location_item_name), textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            TextField(
                value = uiState.name,
                onValueChange = {
                    nfS4AddLocationViewModel.updateName(it)
                },
                singleLine = true,
                modifier = Modifier.weight(2f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(id=R.string.server_address_name), textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            TextField(
                value = uiState.serverAddress,
                onValueChange = {
                    nfS4AddLocationViewModel.updateServerAddress(it)
                },
                singleLine = true,
                modifier = Modifier.weight(2f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(id = R.string.export_path_name), textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            TextField(
                value = uiState.path,
                onValueChange = {
                    nfS4AddLocationViewModel.updatePath(it)
                },
                singleLine = true,
                modifier = Modifier.weight(2f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(id = R.string.cancel_button_name),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(onClick = {
                coroutineScope.launch {
                    nfS4AddLocationViewModel.saveLocation()
                    onBack()
                }
            }, modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(id = R.string.save_button_name),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Text(
            text = uiState.warningMessage,// TODO: check input and write warning message
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}