package com.opfis.data.di

import app.cash.sqldelight.db.SqlDriver
import com.opfis.data.backup.FileBackupPort
import com.opfis.data.db.DatabaseDriverFactory
import com.opfis.data.db.DatabaseKeyProvider
import com.opfis.data.db.OpfisDatabase
import com.opfis.domain.backup.BackupPort
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDataModule =
    module {
        single { DatabaseDriverFactory(androidContext()) }
        single { DatabaseKeyProvider(androidContext()) }
        single<SqlDriver> { get<DatabaseDriverFactory>().createDriver(get<DatabaseKeyProvider>().getOrCreateKey()) }
        single { OpfisDatabase(get()) }
        single<BackupPort> { FileBackupPort(get(), get<DatabaseDriverFactory>().databaseFilePath()) }
    }
