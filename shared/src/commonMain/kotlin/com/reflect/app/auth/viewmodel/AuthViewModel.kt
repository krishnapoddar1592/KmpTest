package com.reflect.app.auth.viewmodel

// File: shared/src/commonMain/kotlin/com/reflect.app/presentation/auth/AuthViewModel.kt


import com.reflect.app.auth.usecase.AppleSignInUseCase
import com.reflect.app.auth.usecase.GoogleSignInUseCase
import com.reflect.app.auth.usecase.LoginWithEmailUseCase
import com.reflect.app.auth.usecase.RegisterUseCase
import com.reflect.app.models.AuthResult
import com.reflect.app.models.User
import com.reflect.app.models.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val appleSignInUseCase: AppleSignInUseCase,
    private val registerUseCase: RegisterUseCase,
    private val coroutineScope: CoroutineScope
) :ViewModel(){
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Initial)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading

        coroutineScope.launch {
            val result = loginWithEmailUseCase(email, password)

            _authState.value = when (result) {
                is AuthResult.Success -> AuthState.Authenticated(result.user)
                is AuthResult.Error.InvalidCredentials -> AuthState.Error("Invalid email or password")
                is AuthResult.Error.NetworkError -> AuthState.Error("Network error. Please check your connection")
                is AuthResult.Error.UserCancelled -> AuthState.Error("Login cancelled")
                is AuthResult.Error.Unknown -> AuthState.Error(result.message)
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading

        coroutineScope.launch {
            val result = googleSignInUseCase(idToken)

            _authState.value = when (result) {
                is AuthResult.Success -> AuthState.Authenticated(result.user)
                is AuthResult.Error.NetworkError -> AuthState.Error("Network error. Please check your connection")
                is AuthResult.Error.UserCancelled -> AuthState.Error("Google login cancelled")
                is AuthResult.Error.Unknown -> AuthState.Error(result.message)
                else -> AuthState.Error("Unknown error during Google Sign-In")
            }
        }
    }

    fun loginWithApple(idToken: String, nonce: String? = null) {
        _authState.value = AuthState.Loading

        coroutineScope.launch {
            val result = appleSignInUseCase(idToken, nonce)

            _authState.value = when (result) {
                is AuthResult.Success -> AuthState.Authenticated(result.user)
                is AuthResult.Error.NetworkError -> AuthState.Error("Network error. Please check your connection")
                is AuthResult.Error.UserCancelled -> AuthState.Error("Apple login cancelled")
                is AuthResult.Error.Unknown -> AuthState.Error(result.message)
                else -> AuthState.Error("Unknown error during Apple Sign-In")
            }
        }
    }

    fun register(email: String, password: String, displayName: String?) {
        _registrationState.value = RegistrationState.Loading

        coroutineScope.launch {
            val result = registerUseCase(email, password, displayName)

            _registrationState.value = when (result) {
                is AuthResult.Success -> RegistrationState.Success(result.user)
                is AuthResult.Error.NetworkError -> RegistrationState.Error("Network error. Please check your connection")
                is AuthResult.Error.Unknown -> RegistrationState.Error(result.message)
                else -> RegistrationState.Error("Unknown error during registration")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Initial
    }

    fun resetRegistrationState() {
        _registrationState.value = RegistrationState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class RegistrationState {
    object Initial : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val user: User) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}