package com.reflect.app

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.reflect.app.auth.service.AndroidFirebaseAuth
import com.reflect.app.auth.service.FirebaseAuthInterface
import com.reflect.app.data.local.dao.UserDao
import com.reflect.app.data.local.dao.UserDaoImpl
import com.reflect.app.db.ReflectDb
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module



actual val platformModule: Module = module {
    // Original KaMPKit dependencies - maintain these
    single<SqlDriver> {
        AndroidSqliteDriver(
            ReflectDb.Schema,
            get(),
            "ReflectDb"
        )
    }

    single<Settings> {
        SharedPreferencesSettings(get())
    }

    single {
        OkHttp.create()
    }

    // Add your auth-related dependencies
    factory<UserDao> { UserDaoImpl(get()) }
    factory<FirebaseAuthInterface> { AndroidFirebaseAuth() }
    factory { get<Context>().getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
}
