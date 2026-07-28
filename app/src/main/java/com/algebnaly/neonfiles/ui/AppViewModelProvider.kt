package com.algebnaly.neonfiles.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.algebnaly.neonfiles.NeonFilesApplication
import com.algebnaly.neonfiles.filesystem.utils.getExternalRootPath
import com.algebnaly.neonfiles.ui.components.DrawerContentViewModel
import androidx.lifecycle.createSavedStateHandle
import com.algebnaly.neonfiles.feature.browser.FileBrowserViewModel
import com.algebnaly.neonfiles.feature.location.Nfs4LocationViewModel
import com.algebnaly.neonfiles.ui.components.ProgressViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            DrawerContentViewModel(
                neonFilesApplication().container.locationRepository,
            )
        }
        initializer {
            Nfs4LocationViewModel(
                this.createSavedStateHandle(), neonFilesApplication().container.locationRepository
            )
        }
        initializer {
            FileBrowserViewModel(
                initialPath = getExternalRootPath(),
                neonFilesApplication().container.storageConnector,
                neonFilesApplication().container.fileOperationManager
            )
        }
        initializer {
            ProgressViewModel(neonFilesApplication().container.fileOperationManager)
        }
    }
}

fun CreationExtras.neonFilesApplication(): NeonFilesApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NeonFilesApplication)