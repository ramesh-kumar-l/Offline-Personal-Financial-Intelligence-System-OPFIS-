package com.opfis.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.nio.file.Files
import java.sql.DriverManager
import java.sql.Statement
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises `migrations/4.sqm` (ROADMAP Phase 5) the same way
 * `SchemaMigrationTest` exercises `3.sqm`: recreate the exact v4 schema
 * migration 4 upgrades from, then assert the `document` table and its
 * `search_index` sync triggers exist and work.
 */
class DocumentSchemaMigrationTest {
    @Test
    fun `opening a v4 database adds the Phase 5 document table and indexes it for search`() {
        val dbFile = Files.createTempFile("opfis-migration-v4-test", ".db")
        Files.deleteIfExists(dbFile)
        val url = "jdbc:sqlite:${dbFile.toAbsolutePath()}"

        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement -> createV4Schema(statement) }
        }

        val driver = JdbcSqliteDriver(url, Properties(), OpfisDatabase.Schema)
        val database = OpfisDatabase(driver)

        database.documentQueries.insertOrReplace(
            id = "doc-1",
            file_name = "receipt.pdf",
            storage_path = "/documents/doc-1.pdf",
            mime_type = "application/pdf",
            document_type = "RECEIPT",
            extracted_text = "Coffee shop total 4.50",
            linked_transaction_id = null,
            imported_at = 0L,
            created_at = 0L,
            updated_at = 0L,
            version = 1L,
        )

        val documentRows = database.documentQueries.selectAll().executeAsList()
        assertEquals(1, documentRows.size)

        val found = database.searchIndexQueries.searchDocuments("\"coffee\"*").executeAsList()
        assertEquals("receipt.pdf", found.single().file_name)

        driver.close()
        Files.deleteIfExists(dbFile)
    }

    /** Recreates the v4 schema (Phases 0-4) that migration 4.sqm upgrades from. */
    private fun createV4Schema(statement: Statement) {
        statement.execute(
            "CREATE TABLE system_status_indicator (id TEXT NOT NULL PRIMARY KEY, label TEXT NOT NULL, " +
                "state TEXT NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, " +
                "version INTEGER NOT NULL DEFAULT 1)",
        )
        statement.execute(
            "CREATE TABLE account (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, " +
                "balance_minor_units INTEGER NOT NULL, is_archived INTEGER NOT NULL DEFAULT 0, " +
                "created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, version INTEGER NOT NULL DEFAULT 1)",
        )
        statement.execute(
            "CREATE TABLE category (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, " +
                "parent_id TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, " +
                "version INTEGER NOT NULL DEFAULT 1)",
        )
        statement.execute(
            "CREATE TABLE financial_transaction (id TEXT NOT NULL PRIMARY KEY, account_id TEXT NOT NULL, " +
                "category_id TEXT, type TEXT NOT NULL, amount_minor_units INTEGER NOT NULL, " +
                "transfer_account_id TEXT, description TEXT NOT NULL DEFAULT '', occurred_at INTEGER NOT NULL, " +
                "created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, version INTEGER NOT NULL DEFAULT 1)",
        )
        statement.execute(
            "CREATE TABLE tag (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, color_hex TEXT, " +
                "created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, version INTEGER NOT NULL DEFAULT 1)",
        )
        statement.execute(
            "CREATE TABLE transaction_tag (transaction_id TEXT NOT NULL, tag_id TEXT NOT NULL, " +
                "created_at INTEGER NOT NULL, PRIMARY KEY (transaction_id, tag_id))",
        )
        statement.execute(
            "CREATE VIRTUAL TABLE search_index USING fts5(entity_type UNINDEXED, entity_id UNINDEXED, text)",
        )
        statement.execute("PRAGMA user_version = 4")
    }
}
