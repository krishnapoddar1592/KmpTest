package com.reflect.app

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import com.reflect.app.db.ReflectDb

internal actual fun testDbConnection(): SqlDriver {
    return inMemoryDriver(ReflectDb.Schema)
}
