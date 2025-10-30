package com.algebnaly.neonfiles.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.algebnaly.neonfiles.R

@Composable
fun ProgressScreen(onHide: () -> Unit, onCancel: () -> Unit) {
    val cancel_button_name = stringResource(R.string.cancel_button_name)
    val hide_button_name = stringResource(id=R.string.hide_button_name)
    Box (modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(enabled = false){}) {
        Column(modifier = Modifier.align(alignment = Alignment.Center)) {
            Text("this is a progress bar")
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
}