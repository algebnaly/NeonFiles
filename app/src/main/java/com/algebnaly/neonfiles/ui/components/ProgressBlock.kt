package com.algebnaly.neonfiles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.algebnaly.neonfiles.R

@Composable
fun ProgressBlock(onHide: () -> Unit, onCancel: () -> Unit, titleMessage: String, progressMessage: String, progression: Float){
    val cancel_button_name = stringResource(R.string.cancel_button_name)
    val hide_button_name = stringResource(id=R.string.hide_button_name)
    Box (modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).clickable(enabled = false){}) {
        Box(modifier = Modifier.fillMaxWidth(fraction = 0.8f).background(color = Color.White).align(alignment = Alignment.Center)){
            Column(modifier = Modifier.align(alignment = Alignment.Center).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(titleMessage, modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.9f).align(alignment = Alignment.CenterHorizontally),progress = { progression }, gapSize = 0.dp)
                Text(progressMessage, modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(0.2f))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onCancel
                    ) {
                        Text(cancel_button_name)
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onHide
                    ) {
                        Text(hide_button_name)
                    }
                    Spacer(Modifier.weight(0.2f))
                }
            }
        }
    }
}