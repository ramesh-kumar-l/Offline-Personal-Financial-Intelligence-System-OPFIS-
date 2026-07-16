package com.opfis.app

import android.app.Application
import com.opfis.app.di.appModule
import com.opfis.data.db.OpfisDatabase
import com.opfis.data.di.androidDataModule
import com.opfis.data.di.dataModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OpfisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SQLiteDatabase.loadLibs(this)
        val koinApp =
            startKoin {
                androidContext(this@OpfisApplication)
                modules(androidDataModule, dataModule, appModule)
            }

        // Opens the encrypted driver (SQLCipher key derivation + schema migration) off the UI
        // thread while the user is still looking at the lock screen (ROADMAP Phase 10 startup
        // optimization), instead of blocking the first screen's composition on cold DB open.
        CoroutineScope(Dispatchers.IO).launch {
            koinApp.koin.get<OpfisDatabase>()
        }
    }
}
