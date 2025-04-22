// File: MainActivity.kt
package com.reflect.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.reflect.app.android.ui.screens.LoginScreen
import com.reflect.app.android.ui.screens.PremiumScreen
import com.reflect.app.android.ui.screens.WelcomeScreen
import com.reflect.app.android.ui.theme.EmotionAppTheme
import com.reflect.app.android.ui.theme.EmotionAppTheme.COSMIC
import com.reflect.app.android.ui.theme.EmotionAppTheme.SERENE
import com.reflect.app.auth.viewmodel.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentTheme by remember { mutableStateOf(COSMIC) }

            EmotionAppTheme(theme = currentTheme) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "welcome") {
                    composable("welcome") {
                        WelcomeScreen(
                            onRegisterClick = { navController.navigate("register") },
                            onSignInClick = { navController.navigate("login") }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onRegisterClick = { navController.navigate("register") },
                            onForgotPasswordClick = { /* Handle forgot password */ },
                            onGoogleSignInClick = { /* Handle Google sign in */ },
                            onAppleSignInClick = { /* Handle Apple sign in */ },
                            onLoginSuccess = { println("success") }
                        )
                    }

                    composable("premium") {
                        PremiumScreen(
                            onCloseClick = { navController.navigateUp() },
                            onContinueClick = { /* Handle continue */ }
                        )
                    }

                    // You would add more screens for the complete app flow
                }
            }
        }
    }
}