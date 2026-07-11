package com.opfis.app

import android.app.Application
import com.opfis.app.di.appModule
import com.opfis.data.di.androidDataModule
import com.opfis.data.di.dataModule
import net.sqlcipher.database.SQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OpfisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SQLiteDatabase.loadLibs(this)
        startKoin {
            androidContext(this@OpfisApplication)
            modules(androidDataModule, dataModule, appModule)
        }
    }
}
