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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.algebnaly.neonfiles.NeonFilesApplication
import com.algebnaly.neonfiles.R
import com.algebnaly.neonfiles.tasks.BackgroundFileOperationManagerInfo
import com.algebnaly.neonfiles.tasks.OperationType
import com.algebnaly.neonfiles.ui.components.DrawerContentView
import com.algebnaly.neonfiles.feature.browser.FileBrowserRoute
import com.algebnaly.neonfiles.feature.location.NFS4LocationRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NeonFilesNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {

    NavHost(
        navController = navController,
        startDestination = Screen.FileBrowser,
        modifier = modifier
    ) {
        composable<Screen.FileBrowser> {
            FileBrowserRoute()
        }
        composable<Screen.NfsLocation> {
            NFS4LocationRoute(
                onBack = {
                    navController.popBackStack()
                }
            )
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
fun NeonFilesApp(
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val context = LocalContext.current
    val fileOperationManager = remember {
        (context.applicationContext as NeonFilesApplication).container.
        fileOperationManager
    }

    val copyOperationName = stringResource(R.string.copy_operation_name)
    val cutOperationName = stringResource(R.string.cut_operation_name)
    val deleteOperationName = stringResource(R.string.delete_operation_name)
    val successName = stringResource(R.string.success)

    LaunchedEffect(Unit) {
        fileOperationManager.eventFlow.collect { message ->
            val messageStr = when (message) {
                is BackgroundFileOperationManagerInfo.Ok -> {
                    val opName = when (message.type) {
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
                    navController.navigate(Screen.NfsLocation())
                },
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                onEditLocation = { locationId ->
                    navController.navigate(Screen.NfsLocation(locationId = locationId))
                }
            )
        }
    ) {
        Scaffold(topBar = { NeonFilesTopAppBar(scope, drawerState) }) { paddingValues ->
            NeonFilesNavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
            )
        }
    }
}


