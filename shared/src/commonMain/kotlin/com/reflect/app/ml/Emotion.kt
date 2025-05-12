// In shared/src/commonMain/kotlin/com/reflect/app/ml/EmotionDetector.kt
package com.reflect.app.ml

import kotlin.jvm.JvmSuppressWildcards

enum class Emotion {
    JOY, SADNESS, ANGER, NEUTRAL
}

interface EmotionDetector {
    /**
     * Detect emotion from image data (ByteArray)
     * @param imageData ByteArray representation of the image
     * @param width Image width
     * @param height Image height
     * @return Map of emotions with confidence scores
     */
    suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float>

    /**
     * Detect face from image data (ByteArray)
     */
    suspend fun detectFace(imageData: ByteArray, width: Int, height: Int): Boolean

    /**
     * Detect face from an ImageProxy (for CameraX ImageAnalysis)
     * The imageProxy parameter will be platform-specific (e.g., androidx.camera.core.ImageProxy on Android).
     * It's crucial that the implementation closes the imageProxy after processing.
     * @param imageProxyInput The platform-specific image proxy object.
     * @return True if a face is detected, false otherwise.
     */
    @JvmSuppressWildcards // Recommended if imageProxyInput could be a generic type with wildcards on JVM
    suspend fun detectFaceInImageProxy(imageProxyInput: Any): Boolean

    /**
     * Release resources when no longer needed
     */
    fun close()
}
