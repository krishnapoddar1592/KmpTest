// shared/src/androidMain/kotlin/com/reflect/app/ml/EmotionDetectorFactory.kt
package com.reflect.app.ml

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.Context

actual object EmotionDetectorFactory : KoinComponent {
    private val context: Context by inject()

    actual fun createEmotionDetector(): EmotionDetector {
        return AndroidEmotionDetector(context)
//        return DummyEmotionDetector()
    }
}
// A temporary implementation that doesn't use TensorFlow
class DummyEmotionDetector : EmotionDetector {
    override suspend fun detectEmotion(imageData: ByteArray, width: Int, height: Int): Map<Emotion, Float> {
        // Return mock data
        return mapOf(
            Emotion.JOY to 0.7f,
            Emotion.SADNESS to 0.1f,
            Emotion.ANGER to 0.05f,
            Emotion.NEUTRAL to 0.15f
        )
    }

    override fun close() {
        // Nothing to close
    }
}