// shared/src/iosMain/kotlin/com/reflect/app/KoinIOS.kt

package com.reflect.app

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.reflect.app.auth.service.FirebaseAuthInterface
import com.reflect.app.auth.service.IosFirebaseAuth
import com.reflect.app.auth.viewmodel.AuthViewModel
import com.reflect.app.data.local.dao.UserDao
import com.reflect.app.data.local.dao.UserDaoImpl
import com.reflect.app.db.ReflectDb
import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.logger.Logger
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults


fun initKoinIos(
    userDefaults: NSUserDefaults,
    appInfo: AppInfo,
    doOnStartup: () -> Unit
): KoinApplication = initKoin(
    module {
        single<Settings> { NSUserDefaultsSettings(userDefaults) }
        single { appInfo }
        single { doOnStartup }
    }
)

actual val platformModule = module {
    // Original KaMPKit dependencies - maintain these
    single<SqlDriver> { NativeSqliteDriver(ReflectDb.Schema, "ReflectDb") }
    single { Darwin.create() }

    // Add your auth-related dependencies
    factory<UserDao> { UserDaoImpl() }
    factory<FirebaseAuthInterface> { IosFirebaseAuth() }

    // Register your AuthViewModel for access from Swift
    factory { AuthViewModel(get(), get(), get(), get(), get()) }
}

// Access from Swift to create a logger
@Suppress("unused")
fun Koin.loggerWithTag(tag: String) = get<Logger>(qualifier = null) { parametersOf(tag) }

// Enable access to AuthViewModel from Swift
@Suppress("unused") // Called from Swift
object KotlinDependencies : KoinComponent {
    fun getAuthViewModel() = getKoin().get<AuthViewModel>()
    fun getEmotionDetectionViewModel() = getKoin().get<EmotionDetectionViewModel>()

}