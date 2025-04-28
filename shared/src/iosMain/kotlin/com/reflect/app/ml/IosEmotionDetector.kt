// shared/src/iosMain/kotlin/com/reflect/app/ml/IosEmotionDetector.kt
package com.reflect.app.ml

import com.reflect.app.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This is a stub implementation that doesn't do much.
 * The actual emotion detection is handled directly in Swift.
 */
class IosEmotionDetector : EmotionDetector {
    override suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
        // This is handled in Swift - return a placeholder result
        return Emotion.values().associateWith { 0f }
    }

    override fun close() {
        // This is handled in Swift
    }
}