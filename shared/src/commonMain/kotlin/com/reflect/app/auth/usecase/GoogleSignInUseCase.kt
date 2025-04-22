package com.reflect.app.auth.usecase

import com.reflect.app.auth.repository.UserRepository
import com.reflect.app.models.AuthResult




class GoogleSignInUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(idToken: String): AuthResult {
        if (idToken.isBlank()) {
            return AuthResult.Error.Unknown("Invalid Google credentials")
        }

        return userRepository.loginWithGoogle(idToken)
    }
}