package com.algebnaly.neonfiles.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import com.algebnaly.neonfiles.ui.AppViewModelProvider
import com.algebnaly.neonfiles.ui.MainViewModel
import com.algebnaly.neonfiles.ui.components.location.NewLocationButton

@Composable
fun DrawerContentView(
    onCloseDrawer: () -> Unit,
    onAddLocation: () -> Unit,
    drawerContentViewModel: DrawerContentViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    ),
    mainViewModel: MainViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    )
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val containerWidthDp = with(density) { windowInfo.containerSize.width.toDp() }
    val uiState by drawerContentViewModel.uiState.collectAsState()
    ModalDrawerSheet(modifier = Modifier.width(containerWidthDp * 3 / 5)) {
        LazyColumn {
            items(uiState.locations) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            mainViewModel.loadLocationItemAndSwitch(item)
                            onCloseDrawer()
                        }, contentAlignment = Alignment.Center
                ) {
                    Text(
                        fontSize = 5.em,
                        text = item.name,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        NewLocationButton(
            onNewLocationClicked = {
                onAddLocation()
                onCloseDrawer()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
