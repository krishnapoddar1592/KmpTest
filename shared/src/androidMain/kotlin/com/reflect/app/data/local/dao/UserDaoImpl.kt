// File: shared/src/androidMain/kotlin/com/reflect/app/data/local/dao/UserDaoImpl.kt
package com.reflect.app.data.local.dao

import android.content.Context
import android.content.SharedPreferences
import com.reflect.app.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserDaoImpl(context: Context) : UserDao {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _userFlow = MutableStateFlow<User?>(null)

    init {
        // Load cached user on initialization
        val userJson = sharedPreferences.getString(USER_KEY, null)
        if (userJson != null) {
            try {
                val user = json.decodeFromString<User>(userJson)
                _userFlow.value = user
            } catch (e: Exception) {
                // Handle deserialization errors
                e.printStackTrace()
            }
        }
    }

    override suspend fun saveUser(user: User) {
        try {
            val userJson = json.encodeToString(user)
            sharedPreferences.edit().putString(USER_KEY, userJson).apply()
            _userFlow.value = user
            println("saved used"+user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getUser(): User? {
        val userJson = sharedPreferences.getString(USER_KEY, null) ?: return null
        return try {
            json.decodeFromString(userJson)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun observeUser(): Flow<User?> {
        return _userFlow.asStateFlow()
    }

    override suspend fun clearCurrentUser() {
        sharedPreferences.edit().remove(USER_KEY).apply()
        _userFlow.value = null
    }

    companion object {
        private const val USER_KEY = "current_user"
    }
}