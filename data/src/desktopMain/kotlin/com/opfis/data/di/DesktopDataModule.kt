package com.opfis.data.di

import app.cash.sqldelight.db.SqlDriver
import com.opfis.data.backup.FileBackupPort
import com.opfis.data.db.DatabaseDriverFactory
import com.opfis.data.db.DatabaseKeyProvider
import com.opfis.data.db.OpfisDatabase
import com.opfis.data.document.DesktopDocumentStorage
import com.opfis.data.document.DesktopDocumentTextExtractor
import com.opfis.domain.backup.BackupPort
import com.opfis.domain.document.DocumentStoragePort
import com.opfis.domain.document.DocumentTextExtractorPort
import org.koin.dsl.module
import java.nio.file.Path
import java.nio.file.Paths

/**
 * `.opfis` under the user's home directory. Not yet using
 * OS-conventional app-data locations (APPDATA/XDG/Application Support)
 * - acceptable for Phase 1, revisit as desktop UX polish later.
 */
val desktopDataModule =
    module {
        single<Path> { Paths.get(System.getProperty("user.home"), ".opfis") }
        single { DatabaseDriverFactory(get()) }
        single { DatabaseKeyProvider(get()) }
        single<SqlDriver> { get<DatabaseDriverFactory>().createDriver(get<DatabaseKeyProvider>().getOrCreateKey()) }
        single { OpfisDatabase(get()) }
        single<BackupPort> { FileBackupPort(get(), get<DatabaseDriverFactory>().databaseFilePath()) }
        single<DocumentStoragePort> { DesktopDocumentStorage(get()) }
        single<DocumentTextExtractorPort> { DesktopDocumentTextExtractor() }
    }
