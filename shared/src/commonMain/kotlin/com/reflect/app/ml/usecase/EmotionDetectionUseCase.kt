// In shared/src/commonMain/kotlin/com/reflect/app/ml/usecase/EmotionDetectionUseCase.kt
package com.reflect.app.ml.usecase

import com.reflect.app.ml.Emotion
import com.reflect.app.ml.EmotionDetector

class EmotionDetectionUseCase(private val emotionDetector: EmotionDetector) {

    suspend fun detectDominantEmotion(imageData: ByteArray, width: Int, height: Int): Emotion {
        val emotionsWithScores = emotionDetector.detectEmotion(imageData, width, height)
        return emotionsWithScores.maxByOrNull { it.value }?.key ?: Emotion.NEUTRAL
    }

    suspend fun detectEmotionScores(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
        return emotionDetector.detectEmotion(imageData, width, height)
    }

    suspend fun detectFace(imageData: ByteArray, width: Int, height: Int): Boolean {
        return emotionDetector.detectFace(imageData, width, height)
    }

    // New method for ImageAnalysis flow
    suspend fun detectFaceInImageProxy(imageProxy: Any): Boolean {
        return emotionDetector.detectFaceInImageProxy(imageProxy)
    }

    fun close() {
        emotionDetector.close()
    }
}
