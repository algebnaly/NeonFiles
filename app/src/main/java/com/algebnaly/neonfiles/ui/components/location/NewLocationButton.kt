package com.algebnaly.neonfiles.ui.components.location

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NewLocationButton(modifier: Modifier = Modifier, onNewLocationClicked: () -> Unit = {}) {
    Button(
        onClick = {
            onNewLocationClicked()
        },
        modifier = modifier
    ) {
        Icon(imageVector = Icons.Filled.AddCircleOutline, contentDescription = "add new location")
    }
}