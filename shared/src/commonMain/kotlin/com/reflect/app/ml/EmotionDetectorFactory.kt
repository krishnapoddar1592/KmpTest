// shared/src/commonMain/kotlin/com/reflect/app/ml/EmotionDetectorFactory.kt
package com.reflect.app.ml

expect object EmotionDetectorFactory {
    fun createEmotionDetector(): EmotionDetector
}