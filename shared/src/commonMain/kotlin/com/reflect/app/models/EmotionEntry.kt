// File: shared/src/commonMain/kotlin/com/reflect/app/models/EmotionEntry.kt
package com.reflect.app.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Serializable
data class EmotionEntry(
    val id: String,
    val date: String, // Using String for LocalDate serialization
    val time: String, // Using String for LocalTime serialization  
    val dominantEmotion: String, // Emotion enum as string
    val emotionScores: Map<String, Float>, // Emotion -> confidence score
    val capturedImagePath: String? = null,
    val contextTags: List<ContextTag> = emptyList(),
    val moodNote: String = "",
    val userId: String
)

@Serializable
enum class ContextTag(val displayName: String, val icon: String) {
    WORK("Work", "💼"),
    FAMILY("Family", "👨‍👩‍👧‍👦"), 
    GYM("Gym", "🏋️"),
    PET("Pet", "🐕"),
    SOCIAL("Social", "👥"),
    SLEEP("Sleep", "😴"),
    FOOD("Food", "🍽️"),
    TRAVEL("Travel", "✈️")
}

@Serializable
data class DayEmotionSummary(
    val date: String,
    val dominantEmotion: String,
    val averageMood: Float,
    val entryCount: Int,
    val emotionDistribution: Map<String, Float>
)

// Extension functions for easier date handling
fun EmotionEntry.getLocalDate(): LocalDate {
    // You'll need to implement proper date parsing here
    return LocalDate.parse(date)
}

fun EmotionEntry.getLocalTime(): LocalTime {
    // You'll need to implement proper time parsing here  
    return LocalTime.parse(time)
}

fun EmotionEntry.getFormattedTime(): String {
    return try {
        val time = LocalTime.parse(time)
        "${time.hour}:${time.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        time
    }
}