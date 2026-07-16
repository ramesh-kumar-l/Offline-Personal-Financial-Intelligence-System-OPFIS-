package com.opfis.app.io

import androidx.compose.runtime.Composable
import kotlin.system.exitProcess

@Composable
actual fun rememberAppExitLauncher(): () -> Unit = { exitProcess(0) }
