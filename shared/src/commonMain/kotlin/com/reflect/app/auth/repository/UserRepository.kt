package com.reflect.app.auth.repository



import com.reflect.app.models.AuthResult
import com.reflect.app.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun loginWithEmail(email: String, password: String): AuthResult
    suspend fun loginWithGoogle(idToken: String): AuthResult
    suspend fun loginWithApple(idToken: String, nonce: String? = null): AuthResult
    suspend fun register(email: String, password: String, displayName: String?): AuthResult
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>
    suspend fun refreshUserData(): User?
    suspend fun deleteAccount(): Boolean
    suspend fun sendPasswordResetEmail(email: String): Boolean
}