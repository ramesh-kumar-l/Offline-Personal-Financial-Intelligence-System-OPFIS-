package com.opfis.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.opfis.app.di.appModule
import com.opfis.data.db.OpfisDatabase
import com.opfis.data.di.dataModule
import com.opfis.data.di.desktopDataModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin

fun main() {
    val koinApp =
        startKoin {
            modules(desktopDataModule, dataModule, appModule)
        }

    // Opens the encrypted driver (SQLCipher key derivation + schema migration) off the UI
    // thread while the user is still looking at the lock screen (ROADMAP Phase 10 startup
    // optimization), instead of blocking the first screen's composition on cold DB open.
    CoroutineScope(Dispatchers.IO).launch {
        koinApp.koin.get<OpfisDatabase>()
    }

    application {
        Window(onCloseRequest = ::exitApplication, title = "OPFIS") {
            App()
        }
    }
}
