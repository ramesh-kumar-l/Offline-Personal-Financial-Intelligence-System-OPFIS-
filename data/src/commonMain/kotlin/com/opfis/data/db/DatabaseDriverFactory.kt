package com.opfis.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Constructs the platform's encrypted [SqlDriver] for [OpfisDatabase].
 * See docs/adr/0005-sqldelight-sqlcipher-persistence.md for why the
 * Android and Desktop actuals use different underlying drivers.
 */
expect class DatabaseDriverFactory {
    fun createDriver(passphrase: CharArray): SqlDriver

    fun databaseFilePath(): String
}
