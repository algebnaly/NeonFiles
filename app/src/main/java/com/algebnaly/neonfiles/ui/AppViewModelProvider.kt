package com.algebnaly.neonfiles.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.algebnaly.neonfiles.NeonFilesApplication
import com.algebnaly.neonfiles.ui.components.DrawerContentViewModel
import com.algebnaly.neonfiles.ui.screen.NFS4AddLocationViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            DrawerContentViewModel(
                neonFilesApplication().container.locationRepository,
                neonFilesApplication().container.fsProvider
            )
        }
        initializer {
            NFS4AddLocationViewModel(neonFilesApplication().container.locationRepository)
        }
        initializer {
            MainViewModel(fsProvider = neonFilesApplication().container.fsProvider)
        }
    }
}

fun CreationExtras.neonFilesApplication(): NeonFilesApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NeonFilesApplication)