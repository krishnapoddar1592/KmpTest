package com.reflect.app

import com.reflect.app.ktor.DogApi
import com.reflect.app.ktor.DogApiImpl
import com.reflect.app.models.BreedRepository
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.reflect.app.auth.repository.UserRepository
import com.reflect.app.auth.repository.UserRepositoryImpl
import com.reflect.app.auth.usecase.AppleSignInUseCase
import com.reflect.app.auth.usecase.GoogleSignInUseCase
import com.reflect.app.auth.usecase.LoginWithEmailUseCase
import com.reflect.app.auth.usecase.RegisterUseCase
import com.reflect.app.auth.viewmodel.AuthViewModel
import com.reflect.app.ml.EmotionDetectorFactory
import com.reflect.app.ml.usecase.EmotionDetectionUseCase
import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
import com.reflect.app.viewmodels.CalendarViewModel
import com.reflect.app.viewmodels.EnhancedEmotionDetectionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun initKoin(appModule: Module): KoinApplication {
    val koinApplication =
        startKoin {
            modules(
                appModule,
                platformModule,
                coreModule,
                authModule,
                mlModule,
                enhancedViewModelModule
            )
        }

    // Dummy initialization logic, making use of appModule declarations for demonstration purposes.
    val koin = koinApplication.koin
    // doOnStartup is a lambda which is implemented in Swift on iOS side
    val doOnStartup = koin.get<() -> Unit>()
    doOnStartup.invoke()

    val kermit = koin.get<Logger> { parametersOf(null) }
    // AppInfo is a Kotlin interface with separate Android and iOS implementations
    val appInfo = koin.get<AppInfo>()
    kermit.v { "App Id ${appInfo.appId}" }

    return koinApplication
}
val mlModule = module {
    single { EmotionDetectorFactory.createEmotionDetector() }
    factory { EmotionDetectionUseCase(get()) }
    factory { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    factory { EmotionDetectionViewModel(get(), get()) }
}
val enhancedViewModelModule = module {
    factory { CalendarViewModel() }
    factory { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    factory {
        EnhancedEmotionDetectionViewModel(
            emotionDetectionUseCase = get(),
            calendarViewModel = get(),
            coroutineScope = get()
        )
    }
}
// Add to the coreModule or create a new module
val authModule = module {
    factory { LoginWithEmailUseCase(get()) }
    factory { GoogleSignInUseCase(get()) }
    factory { AppleSignInUseCase(get()) }
    factory { RegisterUseCase(get()) }

    factory<UserRepository> { UserRepositoryImpl(get()) }
    // Add CoroutineScope for AuthViewModel
    factory { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

    factory {
        AuthViewModel(
            loginWithEmailUseCase = get(),
            googleSignInUseCase = get(),
            appleSignInUseCase = get(),
            registerUseCase = get(),
            coroutineScope = get()
        )
    }
}



private val coreModule =
    module {
        single {
            DatabaseHelper(
                get(),
                getWith("DatabaseHelper"),
                Dispatchers.Default,
            )
        }
        single<DogApi> {
            DogApiImpl(
                getWith("DogApiImpl"),
                get(),
            )
        }
        single<Clock> {
            Clock.System
        }

        // platformLogWriter() is a relatively simple config option, useful for local debugging. For production
        // uses you *may* want to have a more robust configuration from the native platform. In KaMP Kit,
        // that would likely go into platformModule expect/actual.
        // See https://github.com/touchlab/Kermit
        val baseLogger =
            Logger(config = StaticConfig(logWriterList = listOf(platformLogWriter())), "KampKit")
        factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag) else baseLogger }

        single {
            BreedRepository(
                get(),
                get(),
                get(),
                getWith("BreedRepository"),
                get(),
            )
        }
    }

internal inline fun <reified T> Scope.getWith(vararg params: Any?): T {
    return get(parameters = { parametersOf(*params) })
}

// Simple function to clean up the syntax a bit
fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }

expect val platformModule: Module
