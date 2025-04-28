// shared/src/commonMain/kotlin/com/reflect/app/ml/EmotionDetector.kt
package com.reflect.app.ml

import kotlin.jvm.JvmSuppressWildcards

enum class Emotion {
    JOY, SADNESS, ANGER, NEUTRAL
}

interface EmotionDetector {
    /**
     * Detect emotion from image data
     * @param imageData ByteArray representation of the image
     * @param width Image width
     * @param height Image height
     * @return Map of emotions with confidence scores
     */
    suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float>
    
    /**
     * Release resources when no longer needed
     */
    fun close()
}