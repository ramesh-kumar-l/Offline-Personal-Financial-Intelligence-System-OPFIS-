package com.opfis.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.opfis.data.db.OpfisDatabase
import java.util.Properties

/** An in-memory, unencrypted database for exercising repository logic in isolation. */
fun testDatabase(): OpfisDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties(), OpfisDatabase.Schema)
    return OpfisDatabase(driver)
}
