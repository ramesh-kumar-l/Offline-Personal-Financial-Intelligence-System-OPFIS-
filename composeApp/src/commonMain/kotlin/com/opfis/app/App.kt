package com.opfis.app

import androidx.compose.runtime.Composable
import com.opfis.app.dashboard.DashboardScreen
import com.opfis.app.theme.OpfisTheme

@Composable
fun App() {
    OpfisTheme {
        DashboardScreen()
    }
}
