package com.opfis.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.nio.file.Files
import java.sql.DriverManager
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises the exact auto-migration path production code relies on:
 * `JdbcSqliteDriver(url, properties, schema)` reads `PRAGMA user_version`
 * and calls `Schema.migrate()` itself when it finds an older version -
 * see docs/adr/0005 and `data/src/commonMain/sqldelight/migrations/1.sqm`.
 */
class SchemaMigrationTest {
    @Test
    fun `opening a v1 database automatically migrates to the current schema and keeps data`() {
        val dbFile = Files.createTempFile("opfis-migration-test", ".db")
        Files.deleteIfExists(dbFile)
        val url = "jdbc:sqlite:${dbFile.toAbsolutePath()}"

        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE system_status_indicator (
                        id TEXT NOT NULL PRIMARY KEY,
                        label TEXT NOT NULL,
                        state TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                statement.execute(
                    "INSERT INTO system_status_indicator (id, label, state, created_at, updated_at) " +
                        "VALUES ('offline_mode', 'Offline Mode', 'ACTIVE', 1000, 1000)",
                )
                statement.execute("PRAGMA user_version = 1")
            }
        }

        val driver = JdbcSqliteDriver(url, Properties(), OpfisDatabase.Schema)
        val database = OpfisDatabase(driver)

        val row = database.systemStatusQueries.selectById("offline_mode").executeAsOne()

        assertEquals("Offline Mode", row.label)
        assertEquals(1L, row.version)

        driver.close()
        Files.deleteIfExists(dbFile)
    }
}
