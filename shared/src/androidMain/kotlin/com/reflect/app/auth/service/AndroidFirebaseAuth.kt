// shared/src/androidMain/kotlin/com/reflect/app/data/remote/api/AndroidFirebaseAuth.kt
package com.reflect.app.auth.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.reflect.app.models.SubscriptionType
import com.reflect.app.models.User
import kotlinx.coroutines.tasks.await
import java.io.IOException

class AndroidFirebaseAuth : FirebaseAuthInterface {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    isEmailVerified = firebaseUser.isEmailVerified,
                    subscriptionType = SubscriptionType.FREE,
                    createdAt = firebaseUser.metadata?.creationTimestamp ?: 0,
                    lastLoginAt = firebaseUser.metadata?.lastSignInTimestamp ?: 0
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> Result.failure(Exception("NETWORK_ERROR"))
                else -> Result.failure(Exception(e.message ?: "Unknown error"))
            }
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            println(credential)
            val authResult = auth.signInWithCredential(credential).await()
            println(authResult)
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    isEmailVerified = firebaseUser.isEmailVerified,
                    subscriptionType = SubscriptionType.FREE,
                    createdAt = firebaseUser.metadata?.creationTimestamp ?: 0,
                    lastLoginAt = firebaseUser.metadata?.lastSignInTimestamp ?: 0
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Google authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithApple(idToken: String, nonce: String?): Result<User> {
        // Not implemented for Android
        return Result.failure(Exception("Apple Sign-In is not supported on Android"))
    }

    override suspend fun register(email: String, password: String, displayName: String?): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Set display name if provided
                if (!displayName.isNullOrBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()
                }

                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = displayName,
                    isEmailVerified = firebaseUser.isEmailVerified,
                    subscriptionType = SubscriptionType.FREE,
                    createdAt = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis(),
                    lastLoginAt = firebaseUser.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null

        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            isEmailVerified = firebaseUser.isEmailVerified,
            subscriptionType = SubscriptionType.FREE,
            createdAt = firebaseUser.metadata?.creationTimestamp ?: 0,
            lastLoginAt = firebaseUser.metadata?.lastSignInTimestamp ?: 0
        )
    }

    override suspend fun deleteAccount(): Boolean {
        return try {
            auth.currentUser?.delete()?.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}