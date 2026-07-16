package com.opfis.app.io

import android.os.Process
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

@Composable
actual fun rememberAppExitLauncher(): () -> Unit {
    val activity = LocalContext.current as? FragmentActivity
    return {
        activity?.finishAffinity()
        Process.killProcess(Process.myPid())
    }
}
