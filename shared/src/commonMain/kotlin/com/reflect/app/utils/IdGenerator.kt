// File: shared/src/commonMain/kotlin/com/reflect/app/utils/IdGenerator.kt
package com.reflect.app.utils

import kotlinx.datetime.Clock
import kotlin.random.Random

/**
 * Platform-agnostic ID generator for creating unique identifiers
 */
object IdGenerator {

    private val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    /**
     * Generates a UUID-like string identifier
     * Format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     */
    fun generateUuidLikeId(): String {
        return buildString {
            repeat(8) { append(chars.random()) }
            append('-')
            repeat(4) { append(chars.random()) }
            append('-')
            repeat(4) { append(chars.random()) }
            append('-')
            repeat(4) { append(chars.random()) }
            append('-')
            repeat(12) { append(chars.random()) }
        }
    }

    /**
     * Generates a simple timestamp-based ID with random suffix
     */
    fun generateTimestampId(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val randomSuffix = Random.nextInt(1000, 9999)
        return "${timestamp}_$randomSuffix"
    }

    /**
     * Generates a short random ID (16 characters)
     */
    fun generateShortId(): String {
        return buildString {
            repeat(16) { append(chars.random()) }
        }
    }
}