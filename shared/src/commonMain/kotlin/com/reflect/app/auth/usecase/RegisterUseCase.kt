package com.reflect.app.auth.usecase

import com.reflect.app.auth.repository.UserRepository
import com.reflect.app.models.AuthResult

class RegisterUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(email: String, password: String, displayName: String?): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error.InvalidCredentials
        }

        if (password.length < 6) {
            return AuthResult.Error.Unknown("Password must be at least 6 characters")
        }

        return userRepository.register(email, password, displayName)
    }
}