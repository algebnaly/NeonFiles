package com.algebnaly.neonfiles.ui

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.algebnaly.neonfiles.R
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManagerInfo
import com.algebnaly.neonfiles.tasks.OperationType
import com.algebnaly.neonfiles.ui.components.DrawerContentView
import com.algebnaly.neonfiles.ui.components.ProgressViewModel
import com.algebnaly.neonfiles.ui.screen.FileListScreen
import com.algebnaly.neonfiles.ui.screen.NFS4AddLocationScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class NeonFilesScreen() {
    FileListScreen,
    NFS4AddLocationScreen
}

@Composable
fun NeonFilesNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    progressViewModel: ProgressViewModel
) {

    NavHost(
        navController = navController,
        startDestination = NeonFilesScreen.FileListScreen.name,
        modifier = modifier
    ) {
        composable(route = NeonFilesScreen.FileListScreen.name) {
            FileListScreen(mainViewModel,progressViewModel)
        }
        composable(route = NeonFilesScreen.NFS4AddLocationScreen.name) {
            NFS4AddLocationScreen(onBack = {
                navController.popBackStack()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeonFilesTopAppBar(scope: CoroutineScope, drawerState: DrawerState) {
    TopAppBar(title = { Text("NeonFiles") }, navigationIcon = {
        IconButton(onClick = {
            scope.launch {
                if (drawerState.isClosed) {
                    drawerState.open()
                } else {
                    drawerState.close()
                }
            }
        }) {
            Icon(Icons.Filled.Menu, contentDescription = "open drawer")
        }
    })
}


@Composable
fun NeonFilesApp(mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory), progressViewModel: ProgressViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val context = LocalContext.current

    val copyOperationName = stringResource(R.string.copy_operation_name)
    val cutOperationName = stringResource(R.string.cut_operation_name)
    val deleteOperationName = stringResource(R.string.delete_operation_name)
    val successName = stringResource(R.string.success)

    LaunchedEffect(Unit) {
        mainViewModel.toastFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.fileOperationManager.eventFlow.collect { message ->
            val messageStr = when(message){
                is BackgroundFileOperationManagerInfo.Ok -> {
                    val opName = when(message.type) {
                        OperationType.Copy -> copyOperationName
                        OperationType.Cut -> cutOperationName
                        OperationType.Delete -> deleteOperationName
                    }
                    "$opName ${message.message} $successName"
                }
                is BackgroundFileOperationManagerInfo.Err -> message.message
                is BackgroundFileOperationManagerInfo.Cancel -> ""
            }
            Toast.makeText(context, messageStr, Toast.LENGTH_SHORT).show()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContentView(
                onAddLocation = {
                    navController.navigate(NeonFilesScreen.NFS4AddLocationScreen.name)
                },
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(topBar = { NeonFilesTopAppBar(scope, drawerState) }) { paddingValues ->
            NeonFilesNavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                mainViewModel = mainViewModel,
                progressViewModel = progressViewModel
            )
        }
    }
}


