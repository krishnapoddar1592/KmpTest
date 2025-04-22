// User.kt
package com.reflect.app.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val isEmailVerified: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val createdAt: Long = 0,
    val lastLoginAt: Long = 0
)

@Serializable
enum class SubscriptionType {
    FREE, TRIAL, PREMIUM
}