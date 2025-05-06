//// File: MainActivity.kt
//package com.reflect.app.android
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.reflect.app.android.ui.screens.EmotionDetectionScreen
//import com.reflect.app.android.ui.screens.LoginScreen
//import com.reflect.app.android.ui.screens.PremiumScreen
//import com.reflect.app.android.ui.screens.RegisterScreen
//import com.reflect.app.android.ui.screens.WelcomeScreen
//import com.reflect.app.android.ui.theme.EmotionAppTheme
//import com.reflect.app.android.ui.theme.EmotionAppTheme.COSMIC
//import com.reflect.app.android.ui.theme.EmotionAppTheme.SERENE
//import com.reflect.app.auth.viewmodel.AuthViewModel
//import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
//import org.koin.androidx.viewmodel.ext.android.viewModel
//
//class MainActivity : ComponentActivity() {
//    private val authViewModel: AuthViewModel by viewModel()
//    private val emotionDetectionViewModel: EmotionDetectionViewModel by viewModel()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            var currentTheme by remember { mutableStateOf(COSMIC) }
//
//            EmotionAppTheme(theme = currentTheme) {
//                val navController = rememberNavController()
//
//                NavHost(navController = navController, startDestination = "welcome") {
//                    composable("welcome") {
//                        WelcomeScreen(
//                            onRegisterClick = { navController.navigate("register") },
//                            onSignInClick = { navController.navigate("login") },
//                            onEmotionclick = {navController.navigate("emotionDetection")}
//                        )
//                    }
//
//                    composable("login") {
//                        LoginScreen(
//                            viewModel = authViewModel,
//                            onRegisterClick = { navController.navigate("register") },
//                            onForgotPasswordClick = { /* Handle forgot password */ },
//                            onGoogleSignInClick = { /* Handle Google sign in */ },
//                            onAppleSignInClick = { /* Handle Apple sign in */ },
//                            onLoginSuccess = { println("success") }
//                        )
//                    }
//                    composable("register") {
//                        RegisterScreen(
//                            viewModel = authViewModel,
//                            onRegisterClick = { navController.navigate("register") },
//                            onForgotPasswordClick = { /* Handle forgot password */ },
//                            onGoogleSignInClick = { /* Handle Google sign in */ },
//                            onAppleSignInClick = { /* Handle Apple sign in */ },
//                            onLoginSuccess = { println("success") }
//                        )
//                    }
//
//                    composable("premium") {
//                        PremiumScreen(
//                            onCloseClick = { navController.navigateUp() },
//                            onContinueClick = { /* Handle continue */ }
//                        )
//                    }
//                    composable("emotionDetection") {
//                        EmotionDetectionScreen(
//                            viewModel=emotionDetectionViewModel,
//                            onNavigateBack = { navController.navigateUp() }
//                        )
//                    }
//
//                    // You would add more screens for the complete app flow
//                }
//            }
//        }
//    }
//}

// File: MainActivity.kt
package com.reflect.app.android

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.reflect.app.android.session.SessionManager
import com.reflect.app.android.ui.screens.MainScreen
import com.reflect.app.android.ui.screens.EmotionDetectionScreen
import com.reflect.app.android.ui.screens.LoginScreen
import com.reflect.app.android.ui.screens.MainScreen
import com.reflect.app.android.ui.screens.PremiumScreen
import com.reflect.app.android.ui.screens.RegisterScreen
import com.reflect.app.android.ui.screens.WelcomeScreen
import com.reflect.app.android.ui.theme.EmotionAppTheme
import com.reflect.app.android.ui.theme.EmotionAppTheme.COSMIC
import com.reflect.app.android.ui.theme.EmotionAppTheme.SERENE
import com.reflect.app.android.ui.theme.ThemeManager
import com.reflect.app.auth.viewmodel.AuthState
import com.reflect.app.auth.viewmodel.AuthViewModel
import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModel()
    private val emotionDetectionViewModel: EmotionDetectionViewModel by viewModel()

    // Initialize the GoogleSignInHelper
    private lateinit var googleSignInHelper: GoogleSignInHelper

    // Initialize the SessionManager
    private lateinit var sessionManager: SessionManager

    // Google sign-in launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }
    private val themeManager = ThemeManager()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the GoogleSignInHelper
        googleSignInHelper = GoogleSignInHelper(this)

        // Initialize the SessionManager
        sessionManager = SessionManager(this)

        // Observe the AuthViewModel state to sync with SessionManager
        observeAuthState()

        setContent {
//            var currentTheme by remember { mutableStateOf(COSMIC) }

            // Get authentication state from SessionManager
            val isAuthenticated by sessionManager.isAuthenticated.collectAsState(initial = false)

            val currentTheme by themeManager.currentTheme.collectAsState()

            // Apply the theme to the app
            EmotionAppTheme(theme = currentTheme) {
                val navController = rememberNavController()

                // Use the isAuthenticated state to determine the start destination
                val startDestination = if (isAuthenticated) "home" else "welcome"

                NavHost(navController = navController,
                    startDestination = "splash",
                    modifier = Modifier,
//                    route = if (isAuthenticated) "authenticated_graph" else "unauthenticated_graph" // 👈 this forces NavHost recomposition
                ) {
                    // Authentication screens
                    composable("splash") {
                        LaunchedEffect(Unit) {

                            if (!isAuthenticated) {
                                Toast.makeText(this@MainActivity, "user not detected", Toast.LENGTH_SHORT).show()
                                navController.navigate("welcome")
                            } else {
                                navController.navigate("home") {
                                    popUpTo(0) { inclusive = true }  // This clears the entire back stack.
                                    launchSingleTop = true  // Ensures only a single instance of the destination.
                                }
                            }
                        }
                    }



                    composable("welcome") {
                        WelcomeScreen(
                            onRegisterClick = { navController.navigate("register") },
                            onSignInClick = { navController.navigate("login") },
                            onEmotionclick = {
                                if (isAuthenticated) {
                                    navController.navigate("emotionDetection")
                                } else {
                                    Toast.makeText(this@MainActivity, "Please login first", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onRegisterClick = { navController.navigate("register") },
                            onForgotPasswordClick = { /* Handle forgot password */ },
                            onGoogleSignInClick = {
                                // Launch Google Sign-in
                                val signInIntent = googleSignInHelper.getSignInIntent()
                                googleSignInLauncher.launch(signInIntent)
                            },
                            onAppleSignInClick = { /* Handle Apple sign in */ },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    // Clear the back stack so users can't go back to login screen
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onRegisterClick = { /* Already on register screen */ },
                            onForgotPasswordClick = { /* Handle forgot password */ },
                            onGoogleSignInClick = {
                                // Launch Google Sign-in
                                val signInIntent = googleSignInHelper.getSignInIntent()
                                googleSignInLauncher.launch(signInIntent)
                            },
                            onAppleSignInClick = { /* Handle Apple sign in */ },
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    // Clear the back stack so users can't go back to login screen
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Main app screens (protected by authentication)
                    composable("home") {
                        // Check if user is authenticated
//                        if (!isAuthenticated) {
//                            navController.navigate("welcome") {
//                                popUpTo("welcome") { inclusive = true }
//                            }
//                            return@composable
//                        }

                        // Home screen with bottom navigation
                        MainScreen(
                            onEmotionDetectionClick = { navController.navigate("emotionDetection") },
                            onPremiumClick = { navController.navigate("premium") },
                            onSignOut = {
                                sessionManager.signOut()
                                authViewModel.setLoggedOut()
//                                googleSignInHelper.signOut()
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onToggleTheme = { themeManager.toggleTheme() },
                            currentTheme = currentTheme,
                            emotionDetectionViewModel = emotionDetectionViewModel,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }

                    composable("premium") {
                        PremiumScreen(
                            onCloseClick = { navController.navigateUp() },
                            onContinueClick = { /* Handle continue */ }
                        )
                    }

                    composable("emotionDetection") {
                        EmotionDetectionScreen(
                            viewModel = emotionDetectionViewModel,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> {
                        // User authenticated, update SessionManager if needed
                        if (sessionManager.currentUser.first()?.id != state.user.id) {
                            // This is a new login, save to SessionManager
                            // The SessionManager already listens to Firebase, but this ensures
                            // the AuthViewModel and SessionManager stay in sync
                        }
                    }
                    is AuthState.Error -> {
                        // Show error to user
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(Exception::class.java)
            // Get ID token from Google account
            account?.idToken?.let { idToken ->
                // Pass token to AuthViewModel
                authViewModel.loginWithGoogle(idToken)
            } ?: run {
                Toast.makeText(this, "Failed to get ID token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}