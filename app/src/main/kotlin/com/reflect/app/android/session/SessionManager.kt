// app/src/main/kotlin/com/reflect/app/android/session/SessionManager.kt
package com.reflect.app.android.session

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.reflect.app.models.SubscriptionType
import com.reflect.app.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.Date

/**
 * Manages user session for the application.
 * Handles session persistence, authentication state, and provides
 * a convenient way to access the current user throughout the app.
 */
class SessionManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        SESSION_PREFS_NAME, Context.MODE_PRIVATE
    )
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    init {
        // Set up auth state listener
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is logged in
                val kotlinUser = User(
                    id = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName,
                    isEmailVerified = user.isEmailVerified,
                    subscriptionType = SubscriptionType.FREE,
                    createdAt = user.metadata?.creationTimestamp ?: System.currentTimeMillis(),
                    lastLoginAt = user.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
                )
                _currentUser.value = kotlinUser
                _isAuthenticated.value = true
                
                // Save session for backup
                saveSession(kotlinUser)
            } else {
                // Try to restore from saved session
                val restoredUser = restoreSession()
                if (restoredUser != null) {
                    _currentUser.value = restoredUser
                    _isAuthenticated.value = true
                } else {
                    _currentUser.value = null
                    _isAuthenticated.value = false
                }
            }
        }
    }
    
    /**
     * Save user session data as a backup in case Firebase session is lost
     */
    private fun saveSession(user: User) {
        val sessionData = JSONObject().apply {
            put("id", user.id)
            put("email", user.email)
            put("displayName", user.displayName ?: "")
            put("isEmailVerified", user.isEmailVerified)
            put("subscriptionType", user.subscriptionType.toString())
            put("createdAt", user.createdAt)
            put("lastLoginAt", user.lastLoginAt)
            put("expiryDate", System.currentTimeMillis() + SESSION_EXPIRY_MS)
        }
        
        sharedPreferences.edit()
            .putString(KEY_USER_SESSION, sessionData.toString())
            .apply()
    }
    
    /**
     * Restore session from SharedPreferences
     * @return User if valid session exists, null otherwise
     */
    private fun restoreSession(): User? {
        val sessionJson = sharedPreferences.getString(KEY_USER_SESSION, null) ?: return null
        
        try {
            val sessionData = JSONObject(sessionJson)
            val expiryDate = sessionData.optLong("expiryDate", 0)
            
            // Check if session is still valid
            if (expiryDate < System.currentTimeMillis()) {
                // Session expired
                clearSession()
                return null
            }
            
            val subscriptionTypeStr = sessionData.optString("subscriptionType", "FREE")
            val subscriptionType = when (subscriptionTypeStr) {
                "PREMIUM" -> SubscriptionType.PREMIUM
                "TRIAL" -> SubscriptionType.TRIAL
                else -> SubscriptionType.FREE
            }
            
            return User(
                id = sessionData.getString("id"),
                email = sessionData.getString("email"),
                displayName = sessionData.optString("displayName").takeIf { it.isNotEmpty() },
                isEmailVerified = sessionData.getBoolean("isEmailVerified"),
                subscriptionType = subscriptionType,
                createdAt = sessionData.getLong("createdAt"),
                lastLoginAt = sessionData.getLong("lastLoginAt")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Clear the saved session data
     */
    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_USER_SESSION)
            .apply()
        
        _currentUser.value = null
        _isAuthenticated.value = false
    }
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        auth.signOut()
        clearSession()
    }
    
    /**
     * Check if the auth token needs refresh and update if necessary
     */
    fun refreshTokenIfNeeded(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.getIdToken(true)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        } else {
            onComplete(false)
        }
    }
    
    companion object {
        private const val SESSION_PREFS_NAME = "reflect_session"
        private const val KEY_USER_SESSION = "user_session"
        
        // 30 days in milliseconds
        private const val SESSION_EXPIRY_MS = 30 * 24 * 60 * 60 * 1000L
    }
}