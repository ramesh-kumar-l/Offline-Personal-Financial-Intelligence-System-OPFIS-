package com.opfis.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.nio.file.Files
import java.sql.DriverManager
import java.sql.Statement
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

    @Test
    fun `opening a v2 database automatically adds the Phase 2 financial tables`() {
        val dbFile = Files.createTempFile("opfis-migration-v2-test", ".db")
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
                        updated_at INTEGER NOT NULL,
                        version INTEGER NOT NULL DEFAULT 1
                    )
                    """.trimIndent(),
                )
                statement.execute("PRAGMA user_version = 2")
            }
        }

        val driver = JdbcSqliteDriver(url, Properties(), OpfisDatabase.Schema)
        val database = OpfisDatabase(driver)

        database.accountQueries.insertOrReplace(
            id = "acc-1",
            name = "Checking",
            type = "CHECKING",
            balance_minor_units = 0L,
            is_archived = 0L,
            created_at = 1000L,
            updated_at = 1000L,
            version = 1L,
        )
        assertEquals(
            "Checking",
            database.accountQueries
                .selectById("acc-1")
                .executeAsOne()
                .name,
        )

        driver.close()
        Files.deleteIfExists(dbFile)
    }

    @Test
    fun `opening a v3 database adds the Phase 4 tag and search tables and backfills data`() {
        val dbFile = Files.createTempFile("opfis-migration-v3-test", ".db")
        Files.deleteIfExists(dbFile)
        val url = "jdbc:sqlite:${dbFile.toAbsolutePath()}"

        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement -> createV3Schema(statement) }
        }

        val driver = JdbcSqliteDriver(url, Properties(), OpfisDatabase.Schema)
        val database = OpfisDatabase(driver)

        database.tagQueries.insertOrReplace(
            id = "tag-1",
            name = "Essentials",
            color_hex = null,
            created_at = 1000L,
            updated_at = 1000L,
            version = 1L,
        )
        val tagRows =
            database.tagQueries
                .selectAll()
                .executeAsList()
        assertEquals(1, tagRows.size)

        val backfilled = database.searchIndexQueries.searchAccounts("\"checking\"*").executeAsList()
        assertEquals("Primary Checking", backfilled.single().name)

        driver.close()
        Files.deleteIfExists(dbFile)
    }

    /** Recreates the v3 schema (Phases 0-2) that migration 3.sqm upgrades from. */
    private fun createV3Schema(statement: Statement) {
        statement.execute(
            """
            CREATE TABLE system_status_indicator (
                id TEXT NOT NULL PRIMARY KEY,
                label TEXT NOT NULL,
                state TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                version INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        statement.execute(
            """
            CREATE TABLE account (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                balance_minor_units INTEGER NOT NULL,
                is_archived INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                version INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        statement.execute(
            """
            CREATE TABLE category (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                parent_id TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                version INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        statement.execute(
            """
            CREATE TABLE financial_transaction (
                id TEXT NOT NULL PRIMARY KEY,
                account_id TEXT NOT NULL,
                category_id TEXT,
                type TEXT NOT NULL,
                amount_minor_units INTEGER NOT NULL,
                transfer_account_id TEXT,
                description TEXT NOT NULL DEFAULT '',
                occurred_at INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                version INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent(),
        )
        statement.execute(
            "INSERT INTO account " +
                "(id, name, type, balance_minor_units, is_archived, created_at, updated_at, version) " +
                "VALUES ('acc-1', 'Primary Checking', 'CHECKING', 0, 0, 1000, 1000, 1)",
        )
        statement.execute("PRAGMA user_version = 3")
    }
}
