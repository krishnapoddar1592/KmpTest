// shared/src/commonMain/kotlin/com/reflect/app/ml/usecase/EmotionDetectionUseCase.kt
package com.reflect.app.ml.usecase

import com.reflect.app.ml.Emotion
import com.reflect.app.ml.EmotionDetector

class EmotionDetectionUseCase(private val emotionDetector: EmotionDetector) {
    
    suspend fun detectDominantEmotion(imageData: ByteArray, width: Int, height: Int): Emotion {
        val emotionsWithScores = emotionDetector.detectEmotion(imageData, width, height)
        
        // Find the emotion with the highest score
        return emotionsWithScores.maxByOrNull { it.value }?.key ?: Emotion.NEUTRAL
    }
    
    suspend fun detectEmotionScores(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
        return emotionDetector.detectEmotion(imageData, width, height)
    }
    
    fun close() {
        emotionDetector.close()
    }
}
