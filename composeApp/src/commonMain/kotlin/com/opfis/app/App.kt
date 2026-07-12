package com.opfis.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.opfis.app.ai.AiAssistantScreen
import com.opfis.app.dashboard.DashboardScreen
import com.opfis.app.document.DocumentVaultScreen
import com.opfis.app.memory.MemoryScreen
import com.opfis.app.navigation.AppDestination
import com.opfis.app.search.SearchScreen
import com.opfis.app.security.AppLockState
import com.opfis.app.security.LockScreen
import com.opfis.app.security.SecurityScreen
import com.opfis.app.theme.OpfisTheme
import kotlinx.coroutines.delay

private const val AUTO_LOCK_CHECK_INTERVAL_MILLIS = 1000L

@Composable
fun App() {
    OpfisTheme {
        val lockState = remember { AppLockState() }
        LaunchedEffect(Unit) {
            while (true) {
                delay(AUTO_LOCK_CHECK_INTERVAL_MILLIS)
                lockState.checkIdleTimeout()
            }
        }

        if (lockState.isLocked) {
            LockScreen(onUnlocked = { lockState.unlock() })
        } else {
            var destination by remember { mutableStateOf(AppDestination.Dashboard) }

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        AppDestination.entries.forEach { entry ->
                            NavigationBarItem(
                                selected = destination == entry,
                                onClick = {
                                    destination = entry
                                    lockState.recordInteraction()
                                },
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
                        AppDestination.Assistant -> AiAssistantScreen()
                        AppDestination.Security -> SecurityScreen()
                    }
                }
            }
        }
    }
}
