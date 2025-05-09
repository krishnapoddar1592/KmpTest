package com.reflect.app.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.reflect.app.AppInfo
import com.reflect.app.android.session.SessionManager
import com.reflect.app.auth.viewmodel.AuthViewModel
import com.reflect.app.initKoin
import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
import com.reflect.app.models.BreedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class MainApp : Application(), KoinComponent {

    // Lazy inject SessionManager
    private val sessionManager: SessionManager by inject()

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase first
        FirebaseApp.initializeApp(this)

        // Then initialize Koin with your modules
        initKoin(
            module {
                // Existing KaMPKit dependencies
                single<Context> { this@MainApp }
                viewModel { BreedViewModel(get(), get { parametersOf("BreedViewModel") }) }
                single<SharedPreferences> {
                    get<Context>().getSharedPreferences(
                        "KAMPSTARTER_SETTINGS",
                        Context.MODE_PRIVATE,
                    )
                }
                single<AppInfo> { AndroidAppInfo }
                single {
                    { Log.i("Startup", "Hello from Android/Kotlin!") }
                }

                // Add AuthViewModel
                viewModel {
                    AuthViewModel(
                        loginWithEmailUseCase = get(),
                        googleSignInUseCase = get(),
                        appleSignInUseCase = get(),
                        registerUseCase = get(),
                        coroutineScope = get()
                    )
                }
                viewModel { EmotionDetectionViewModel(get(), get()) }

                // Add SessionManager
                singleOf(::SessionManager)

                // Add Firebase-related dependencies if needed
                // factory<FirebaseAuthInterface> { AndroidFirebaseAuth() }
                // factory<UserDao> { UserDaoImpl(get()) }
            }
        )

        // Initialize the SessionManager
        sessionManager
    }
}

object AndroidAppInfo : AppInfo {
    override val appId: String = BuildConfig.APPLICATION_ID
}