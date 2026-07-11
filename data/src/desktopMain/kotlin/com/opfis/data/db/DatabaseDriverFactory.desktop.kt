package com.opfis.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.sqlite.mc.SQLiteMCSqlCipherConfig
import java.nio.file.Files
import java.nio.file.Path

/**
 * Uses io.github.willena:sqlite-jdbc (SQLite3MultipleCiphers) in its
 * SQLCipher-4-compatible mode, so the on-disk format matches Android's
 * net.zetetic driver (docs/adr/0005).
 */
actual class DatabaseDriverFactory(
    private val databaseDirectory: Path,
) {
    actual fun createDriver(passphrase: CharArray): SqlDriver {
        Files.createDirectories(databaseDirectory)
        val dbFile = databaseDirectory.resolve(DATABASE_FILE_NAME)
        val properties =
            SQLiteMCSqlCipherConfig
                .getV4Defaults()
                .withKey(String(passphrase))
                .build()
                .toProperties()
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${dbFile.toAbsolutePath()}",
            properties = properties,
            schema = OpfisDatabase.Schema,
        )
    }

    actual fun databaseFilePath(): String = databaseDirectory.resolve(DATABASE_FILE_NAME).toAbsolutePath().toString()

    private companion object {
        const val DATABASE_FILE_NAME = "opfis.db"
    }
}
