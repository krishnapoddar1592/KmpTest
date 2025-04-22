package com.reflect.app.auth.usecase

import com.reflect.app.auth.repository.UserRepository
import com.reflect.app.models.AuthResult

class AppleSignInUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(idToken: String, nonce: String? = null): AuthResult {
        if (idToken.isBlank()) {
            return AuthResult.Error.Unknown("Invalid Apple credentials")
        }

        return userRepository.loginWithApple(idToken, nonce)
    }
}