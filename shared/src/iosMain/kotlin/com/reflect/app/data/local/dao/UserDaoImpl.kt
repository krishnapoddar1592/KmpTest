package com.reflect.app.data.local.dao


import com.reflect.app.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

class UserDaoImpl : UserDao {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _userFlow = MutableStateFlow<User?>(null)

    init {
        // Load cached user on initialization
        val userJson = userDefaults.stringForKey(USER_KEY)
        if (userJson != null) {
            try {
                val user = json.decodeFromString<User>(userJson)
                _userFlow.value = user
            } catch (e: Exception) {
                println("Error deserializing user: ${e.message}")
            }
        }
    }

    override suspend fun saveUser(user: User) {
        try {
            val userJson = json.encodeToString(user)
            userDefaults.setObject(userJson, USER_KEY)
            _userFlow.value = user
        } catch (e: Exception) {
            println("Error serializing user: ${e.message}")
        }
    }

    override suspend fun getUser(): User? {
        val userJson = userDefaults.stringForKey(USER_KEY) ?: return null
        return try {
            json.decodeFromString(userJson)
        } catch (e: Exception) {
            println("Error deserializing user: ${e.message}")
            null
        }
    }

    override fun observeUser(): Flow<User?> {
        return _userFlow.asStateFlow()
    }

    override suspend fun clearCurrentUser() {
        userDefaults.removeObjectForKey(USER_KEY)
        _userFlow.value = null
    }

    companion object {
        private const val USER_KEY = "current_user"
    }
}