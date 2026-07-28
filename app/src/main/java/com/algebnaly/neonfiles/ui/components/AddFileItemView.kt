package com.algebnaly.neonfiles.ui.components

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.ui.AppViewModelProvider
import com.algebnaly.neonfiles.feature.browser.FileBrowserViewModel

@Composable
fun AddFileItemFloatingButton(viewModel: FileBrowserViewModel = viewModel(
    factory = AppViewModelProvider.Factory
)){

}