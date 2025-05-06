// File: app/src/main/java/com/reflect/app/android/ui/screens/LoginScreen.kt
package com.reflect.app.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.reflect.app.android.R
import com.reflect.app.android.ui.components.EmotionButton
import com.reflect.app.android.ui.components.EmotionDividerWithText
import com.reflect.app.android.ui.components.EmotionTextField
import com.reflect.app.android.ui.components.SocialButton
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.android.ui.theme.Neutral
import com.reflect.app.auth.viewmodel.AuthState
import com.reflect.app.auth.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onAppleSignInClick: () -> Unit,
    onLoginSuccess: () -> Unit,

) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    // Validation states
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var emailErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }
    // Collect auth state
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    println(authState)

    // Handle authentication state changes
    LaunchedEffect(key1 = Unit) {
        // Force check auth state when entering login screen
        val currentUser = FirebaseAuth.getInstance().currentUser
        println("LOGIN SCREEN: Current user is ${if (currentUser != null) "signed in" else "null"}")
        if (currentUser != null) {
            // Maybe force sign out here to ensure clean state
            FirebaseAuth.getInstance().signOut()
        }
    }
    when (authState) {
        is AuthState.Authenticated -> {
            // Navigate to main screen on successful login
            onLoginSuccess()
        }
        is AuthState.Error -> {
            // You could show an error message here
        }
        else -> {
            // Loading or initial state handled in UI
        }
    }
    // Error message to display
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Function to validate inputs
    fun validateInputs(): Boolean {
        // Email validation
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        isEmailValid = email.matches(emailPattern.toRegex())
        if (!isEmailValid) {
            emailErrorMessage = "Please enter a valid email address"
        }

        // Password validation
        isPasswordValid = password.length >= 6
        if (!isPasswordValid) {
            passwordErrorMessage = "Password must be at least 6 characters"
        }

        return isEmailValid && isPasswordValid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmotionTheme.colors.background)
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            EmotionTheme.colors.interactive,
                            RoundedCornerShape(2.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            EmotionTheme.colors.textSecondary.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            EmotionTheme.colors.textSecondary.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }

            // Placeholder for logo or illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 24.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Neutral)
            ) {
                // Replace with actual logo/illustration
                Image(
                    painter = painterResource(id = R.drawable.ic_favorite_border_24px),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Login form
            Spacer(modifier = Modifier.height(24.dp))

            EmotionTextField(
                value = email,
                onValueChange = {
                    email = it
                    isEmailValid = true  // Reset validation on change
                },
                placeholder = "Email",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = !isEmailValid,
                errorMessage = if (!isEmailValid) emailErrorMessage else null
            )


            Spacer(modifier = Modifier.height(16.dp))

//            EmotionTextField(
//                value = password,
//                onValueChange = { password = it },
//                placeholder = "Password",
//                isPassword = true,
//                imeAction = ImeAction.Done
//            )
            EmotionTextField(
                value = password,
                onValueChange = {
                    password = it
                    isPasswordValid = true  // Reset validation on change
                },
                placeholder = "Password",
                isPassword = true,
                imeAction = ImeAction.Done,
                isError = !isPasswordValid,
                errorMessage = if (!isPasswordValid) passwordErrorMessage else null
            )

            // Password recovery
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Forgot Password",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmotionTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign in button
//            EmotionButton(
//                text = "Sign in",
//                onClick = {
//                    viewModel.loginWithEmail(email, password)
//
//
//                          },
////                isLoading = authState is AuthState.Loading
//            )
            EmotionButton(
                text = "Sign in",
                onClick = {
                    if (validateInputs()) {
                        viewModel.loginWithEmail(email, password)
                        errorMessage = null  // Clear any previous error
                    }
                },
                isLoading = authState is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with text
            EmotionDividerWithText(text = "Or continue with")

            Spacer(modifier = Modifier.height(24.dp))

            // Social login options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialButton(
                    iconResId = R.drawable.ic_google,
                    onClick = onGoogleSignInClick,
                    contentDescription = "Sign in with Google"
                )

                Spacer(modifier = Modifier.width(24.dp))

                SocialButton(
                    iconResId = R.drawable.ic_apple,
                    onClick = onAppleSignInClick,
                    contentDescription = "Sign in with Apple"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Not a member?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmotionTheme.colors.textSecondary
                )

                TextButton(onClick = onRegisterClick) {
                    Text(
                        text = "Register now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmotionTheme.colors.interactive
                    )
                }
            }

            // Bottom spacing for navigation bar
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom navigation - assuming you have this component
        // If not, you can remove this part
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}