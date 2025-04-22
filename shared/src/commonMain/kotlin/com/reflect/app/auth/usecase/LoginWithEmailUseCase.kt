package com.reflect.app.auth.usecase

import com.reflect.app.auth.repository.UserRepository
import com.reflect.app.models.AuthResult

class LoginWithEmailUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error.InvalidCredentials
        }

        return userRepository.loginWithEmail(email, password)
    }
}