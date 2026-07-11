package com.opfis.app

import android.app.Application
import com.opfis.app.di.appModule
import com.opfis.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OpfisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OpfisApplication)
            modules(dataModule, appModule)
        }
    }
}
