package com.algebnaly.neonfiles.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.algebnaly.neonfiles.R

@Composable
fun ProgressScreen(onHide: () -> Unit, onCancel: () -> Unit) {
    val cancel_button_name = stringResource(R.string.cancel_button_name)
    val hide_button_name = stringResource(id=R.string.hide_button_name)
    
    Column {
        Row {
            Button(
                onClick = onCancel
            ) {
                Text(cancel_button_name)
            }
            Button(
                onClick = onHide
            ) {
                Text(hide_button_name)
            }
        }
    }
}