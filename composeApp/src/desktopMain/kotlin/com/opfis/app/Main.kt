package com.opfis.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.opfis.app.di.appModule
import com.opfis.data.di.dataModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(dataModule, appModule)
    }

    application {
        Window(onCloseRequest = ::exitApplication, title = "OPFIS") {
            App()
        }
    }
}
