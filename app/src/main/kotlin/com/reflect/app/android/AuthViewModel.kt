package com.reflect.app.android;

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.reflect.app.domain.model.AuthResult
//import com.reflect.app.domain.usecase.auth.GoogleSignInUseCase
//import com.reflect.app.domain.usecase.auth.LoginWithEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
//    private val loginWithEmailUseCase: LoginWithEmailUseCase,
//    private val googleSignInUseCase: GoogleSignInUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
//
//        viewModelScope.launch {
////            val result = loginWithEmailUseCase(email, password)
//
//            _authState.value = when (result) {
//                is AuthResult.Success -> AuthState.Authenticated(result.user.id)
//                is AuthResult.Error.InvalidCredentials -> AuthState.Error("Invalid email or password")
//                is AuthResult.Error.NetworkError -> AuthState.Error("Network error. Please check your connection")
//                is AuthResult.Error.UserCancelled -> AuthState.Error("Login cancelled")
//                is AuthResult.Error.Unknown -> AuthState.Error(result.message)
//            }
//        }
    }
    
    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        
//        viewModelScope.launch {
//            val result = googleSignInUseCase(idToken)
//
//            _authState.value = when (result) {
//                is AuthResult.Success -> AuthState.Authenticated(result.user.id)
//                is AuthResult.Error.NetworkError -> AuthState.Error("Network error. Please check your connection")
//                is AuthResult.Error.UserCancelled -> AuthState.Error("Google login cancelled")
//                is AuthResult.Error.Unknown -> AuthState.Error(result.message)
//                else -> AuthState.Error("Unknown error during Google Sign-In")
//            }
//        }
    }
}
