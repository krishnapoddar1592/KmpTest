package com.reflect.app

import app.cash.sqldelight.db.SqlDriver

internal expect fun testDbConnection(): SqlDriver
