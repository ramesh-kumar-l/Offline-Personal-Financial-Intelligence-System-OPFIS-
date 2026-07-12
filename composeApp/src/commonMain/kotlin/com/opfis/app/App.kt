package com.opfis.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.opfis.app.dashboard.DashboardScreen
import com.opfis.app.document.DocumentVaultScreen
import com.opfis.app.memory.MemoryScreen
import com.opfis.app.navigation.AppDestination
import com.opfis.app.search.SearchScreen
import com.opfis.app.theme.OpfisTheme

@Composable
fun App() {
    OpfisTheme {
        var destination by remember { mutableStateOf(AppDestination.Dashboard) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    AppDestination.entries.forEach { entry ->
                        NavigationBarItem(
                            selected = destination == entry,
                            onClick = { destination = entry },
                            icon = { Text(entry.glyph) },
                            label = { Text(entry.label) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (destination) {
                    AppDestination.Dashboard -> DashboardScreen()
                    AppDestination.Search -> SearchScreen()
                    AppDestination.Vault -> DocumentVaultScreen()
                    AppDestination.Memory -> MemoryScreen()
                }
            }
        }
    }
}
