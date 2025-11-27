package com.algebnaly.neonfiles.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.algebnaly.neonfiles.NeonFilesApplication
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.ui.components.DrawerContentViewModel
import com.algebnaly.neonfiles.ui.screen.NFS4AddLocationViewModel
import com.algebnaly.neonfiles.ui.components.ProgressViewModel

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
            MainViewModel(initialPath = getExternalRootPath() ,fsProvider = neonFilesApplication().container.fsProvider, neonFilesApplication().container.fileOperationManager)
        }
        initializer {
            ProgressViewModel(neonFilesApplication().container.fileOperationManager)
        }
    }
}

fun CreationExtras.neonFilesApplication(): NeonFilesApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NeonFilesApplication)