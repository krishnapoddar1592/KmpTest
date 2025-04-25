// shared/src/iosMain/kotlin/com/reflect/app/auth/service/IosFirebaseAuth.kt
package com.reflect.app.auth.service


import com.reflect.app.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This is a stub implementation that doesn't do much.
 * The actual Firebase authentication is handled directly in Swift.
 */
class IosFirebaseAuth : FirebaseAuthInterface {
    // A flow that can be updated from Swift if needed
    private val _authStateFlow = MutableStateFlow<User?>(null)

    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        // This is handled in Swift - return a placeholder result
        return Result.failure(NotImplementedError("Auth is handled in Swift"))
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        // This is handled in Swift - return a placeholder result
        return Result.failure(NotImplementedError("Auth is handled in Swift"))
    }

    override suspend fun loginWithApple(idToken: String, nonce: String?): Result<User> {
        // This is handled in Swift - return a placeholder result
        return Result.failure(NotImplementedError("Auth is handled in Swift"))
    }

    override suspend fun register(email: String, password: String, displayName: String?): Result<User> {
        // This is handled in Swift - return a placeholder result
        return Result.failure(NotImplementedError("Auth is handled in Swift"))
    }

    override suspend fun logout() {
        // This is handled in Swift
    }

    override suspend fun getCurrentUser(): User? {
        // This is handled in Swift
        return null
    }

    override suspend fun deleteAccount(): Boolean {
        // This is handled in Swift
        return false
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean {
        // This is handled in Swift
        return false
    }



}
