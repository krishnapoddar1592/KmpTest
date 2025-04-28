// shared/src/iosMain/kotlin/com/reflect/app/ml/EmotionDetectorFactory.kt
package com.reflect.app.ml

actual object EmotionDetectorFactory {
    actual fun createEmotionDetector(): EmotionDetector {
        return IosEmotionDetector()
    }
}