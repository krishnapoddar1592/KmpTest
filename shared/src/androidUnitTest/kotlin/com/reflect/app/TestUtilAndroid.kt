package com.reflect.app

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.reflect.app.db.ReflectDb

internal actual fun testDbConnection(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        .also { ReflectDb.Schema.create(it) }
