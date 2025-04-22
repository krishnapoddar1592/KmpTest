package com.reflect.app.data.local.dao

import com.reflect.app.models.User
import kotlinx.coroutines.flow.Flow

interface UserDao {
    suspend fun saveUser(user: User)
    suspend fun getUser(): User?
    fun observeUser(): Flow<User?>
    suspend fun clearCurrentUser()
}