package com.opfis.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.sqlcipher.database.SupportFactory

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    actual fun createDriver(passphrase: CharArray): SqlDriver {
        val factory = SupportFactory(String(passphrase).toByteArray(Charsets.UTF_8))
        return AndroidSqliteDriver(
            schema = OpfisDatabase.Schema,
            context = context,
            name = DATABASE_FILE_NAME,
            factory = factory,
        )
    }

    actual fun databaseFilePath(): String = context.getDatabasePath(DATABASE_FILE_NAME).absolutePath

    private companion object {
        const val DATABASE_FILE_NAME = "opfis.db"
    }
}
