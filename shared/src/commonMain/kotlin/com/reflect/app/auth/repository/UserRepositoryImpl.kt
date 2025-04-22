// shared/src/commonMain/kotlin/com/reflect/app/data/repository/UserRepositoryImpl.kt
package com.reflect.app.auth.repository

import com.reflect.app.auth.repository.UserRepository
import com.reflect.app.auth.service.FirebaseAuthFactory
import com.reflect.app.auth.service.FirebaseAuthInterface
import com.reflect.app.data.local.dao.UserDao
import com.reflect.app.models.AuthResult
import com.reflect.app.models.User
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    private val firebaseAuth: FirebaseAuthInterface = FirebaseAuthFactory.createFirebaseAuth()

    override suspend fun loginWithEmail(email: String, password: String): AuthResult {
        return try {
            println( "login with email startedd")
            val result = firebaseAuth.loginWithEmail(email, password)

            result.fold(
                onSuccess = { user ->
                    userDao.saveUser(user)
                    AuthResult.Success(user)
                },
                onFailure = { exception ->
                    when (exception.message) {
                        "INVALID_LOGIN_CREDENTIALS" -> AuthResult.Error.InvalidCredentials
                        "NETWORK_ERROR" -> AuthResult.Error.NetworkError
                        else -> AuthResult.Error.Unknown(exception.message ?: "Unknown error")
                    }
                }
            )
        } catch (e: Exception) {
            println(e.toString())
            AuthResult.Error.Unknown(e.message ?: "Unknown error")
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AuthResult {
        return try {
            val result = firebaseAuth.loginWithGoogle(idToken)
            println("result"+result)

            result.fold(
                onSuccess = { user ->
                    userDao.saveUser(user)
                    AuthResult.Success(user)
                },
                onFailure = { exception ->
                    println(exception)
                    when (exception.message) {

                        "NETWORK_ERROR" -> AuthResult.Error.NetworkError
                        "SIGN_IN_CANCELLED" -> AuthResult.Error.UserCancelled
                        else -> AuthResult.Error.Unknown(exception.message ?: "Google Sign-In failed")
                    }
                }
            )
        } catch (e: Exception) {
            println("exception here"+e)
            AuthResult.Error.Unknown(e.message ?: "Google Sign-In failed")
        }
    }

    override suspend fun loginWithApple(idToken: String, nonce: String?): AuthResult {
        return try {
            val result = firebaseAuth.loginWithApple(idToken, nonce)

            result.fold(
                onSuccess = { user ->
                    userDao.saveUser(user)
                    AuthResult.Success(user)
                },
                onFailure = { exception ->
                    when (exception.message) {
                        "NETWORK_ERROR" -> AuthResult.Error.NetworkError
                        "SIGN_IN_CANCELLED" -> AuthResult.Error.UserCancelled
                        else -> AuthResult.Error.Unknown(exception.message ?: "Apple Sign-In failed")
                    }
                }
            )
        } catch (e: Exception) {
            AuthResult.Error.Unknown(e.message ?: "Apple Sign-In failed")
        }
    }

    override suspend fun register(email: String, password: String, displayName: String?): AuthResult {
        return try {
            val result = firebaseAuth.register(email, password, displayName)

            result.fold(
                onSuccess = { user ->
                    userDao.saveUser(user)
                    AuthResult.Success(user)
                },
                onFailure = { exception ->
                    AuthResult.Error.Unknown(exception.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            AuthResult.Error.Unknown(e.message ?: "Unknown error")
        }
    }

    override suspend fun logout() {
        firebaseAuth.logout()
        userDao.clearCurrentUser()
    }

    override suspend fun getCurrentUser(): User? {
        return firebaseAuth.getCurrentUser() ?: userDao.getUser()
    }

    override fun observeCurrentUser(): Flow<User?> {
        return userDao.observeUser()
    }

    override suspend fun refreshUserData(): User? {
        val user = firebaseAuth.getCurrentUser()
        if (user != null) {
            userDao.saveUser(user)
        }
        return user
    }

    override suspend fun deleteAccount(): Boolean {
        val deleted = firebaseAuth.deleteAccount()
        if (deleted) {
            userDao.clearCurrentUser()
        }
        return deleted
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean {
        return firebaseAuth.sendPasswordResetEmail(email)
    }
}