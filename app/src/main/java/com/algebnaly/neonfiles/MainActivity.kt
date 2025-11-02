package com.algebnaly.neonfiles

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.ui.AppViewModelProvider
import com.algebnaly.neonfiles.ui.NeonFilesApp
import com.algebnaly.neonfiles.ui.components.ProgressOverlay
import com.algebnaly.neonfiles.ui.components.ProgressViewModel
import com.algebnaly.neonfiles.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Environment.isExternalStorageManager()) {
            try {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData("package:$packageName".toUri())
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }

        enableEdgeToEdge()
        setContent {
            NeonFilesMainApp()
        }
    }
}

@Composable
fun NeonFilesMainApp(progressViewModel: ProgressViewModel = viewModel(factory = AppViewModelProvider.Factory)){
    AppTheme {
        Box(modifier = Modifier.fillMaxSize()){
            NeonFilesApp(progressViewModel = progressViewModel)
            ProgressOverlay(progressViewModel)
        }
    }
}