// File: shared/src/commonMain/kotlin/com/reflect/app/data/MockEmotionData.kt
package com.reflect.app.data.local.dao

import com.reflect.app.models.ContextTag
import com.reflect.app.models.EmotionEntry
import com.reflect.app.utils.IdGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

object MockEmotionData {
    
    /**
     * Generates comprehensive mock emotion entries for testing
     * Covers the last 14 days with realistic emotion patterns
     */
    fun generateMockEmotionEntries(): List<EmotionEntry> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val baseDate = now.date
        
        return buildList {
            // Today - Multiple entries
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.toString(),
                time = "8:30",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.85f, "NEUTRAL" to 0.15f),
                contextTags = listOf(ContextTag.GYM),
                moodNote = "Great morning workout! Feeling energized and ready for the day.",
                userId = "test_user"
            ))
            
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.toString(),
                time = "14:15",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.75f, "NEUTRAL" to 0.25f),
                contextTags = listOf(ContextTag.WORK),
                moodNote = "Productive afternoon meeting. Team collaboration was excellent.",
                userId = "test_user"
            ))
            
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.toString(),
                time = "19:45",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.9f, "NEUTRAL" to 0.1f),
                contextTags = listOf(ContextTag.FAMILY, ContextTag.FOOD),
                moodNote = "Family dinner was amazing. Mom made my favorite pasta!",
                userId = "test_user"
            ))
            
            // Yesterday - Mixed emotions
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(1, DateTimeUnit.DAY).toString(),
                time = "9:00",
                dominantEmotion = "NEUTRAL",
                emotionScores = mapOf("NEUTRAL" to 0.7f, "JOY" to 0.3f),
                contextTags = listOf(ContextTag.WORK),
                moodNote = "Regular Monday morning. Coffee helping me wake up.",
                userId = "test_user"
            ))
            
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(1, DateTimeUnit.DAY).toString(),
                time = "16:30",
                dominantEmotion = "ANGER",
                emotionScores = mapOf("ANGER" to 0.65f, "NEUTRAL" to 0.35f),
                contextTags = listOf(ContextTag.WORK),
                moodNote = "Frustrating meeting with difficult client. Need to decompress.",
                userId = "test_user"
            ))
            
            // 2 days ago - Pet day
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(2, DateTimeUnit.DAY).toString(),
                time = "7:45",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.8f, "NEUTRAL" to 0.2f),
                contextTags = listOf(ContextTag.PET),
                moodNote = "Morning walk with Luna. She found so many interesting smells!",
                userId = "test_user"
            ))
            
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(2, DateTimeUnit.DAY).toString(),
                time = "20:00",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.85f, "NEUTRAL" to 0.15f),
                contextTags = listOf(ContextTag.PET, ContextTag.FAMILY),
                moodNote = "Family movie night with the dog curled up on the couch.",
                userId = "test_user"
            ))
            
            // 3 days ago - Stressful day
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(3, DateTimeUnit.DAY).toString(),
                time = "10:30",
                dominantEmotion = "ANGER",
                emotionScores = mapOf("ANGER" to 0.6f, "NEUTRAL" to 0.4f),
                contextTags = listOf(ContextTag.WORK),
                moodNote = "Project deadline pressure. Feeling overwhelmed with tasks.",
                userId = "test_user"
            ))
            
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(3, DateTimeUnit.DAY).toString(),
                time = "18:15",
                dominantEmotion = "NEUTRAL",
                emotionScores = mapOf("NEUTRAL" to 0.75f, "ANGER" to 0.25f),
                contextTags = listOf(ContextTag.GYM),
                moodNote = "Gym session helped release some stress. Feeling more balanced now.",
                userId = "test_user"
            ))
            
            // 4 days ago - Social day
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(4, DateTimeUnit.DAY).toString(),
                time = "11:00",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.9f, "NEUTRAL" to 0.1f),
                contextTags = listOf(ContextTag.SOCIAL, ContextTag.FOOD),
                moodNote = "Brunch with college friends! So good to catch up after months.",
                userId = "test_user"
            ))
            
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(4, DateTimeUnit.DAY).toString(),
                time = "22:30",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.7f, "NEUTRAL" to 0.3f),
                contextTags = listOf(ContextTag.SOCIAL),
                moodNote = "Game night went late but was so much fun. Laughed until my stomach hurt.",
                userId = "test_user"
            ))
            
            // 5 days ago - Sad day
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(5, DateTimeUnit.DAY).toString(),
                time = "14:45",
                dominantEmotion = "SADNESS",
                emotionScores = mapOf("SADNESS" to 0.7f, "NEUTRAL" to 0.3f),
                contextTags = listOf(ContextTag.FAMILY),
                moodNote = "Got news about grandma's health. Feeling worried and emotional.",
                userId = "test_user"
            ))
            
            // 6 days ago - Recovery day
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(6, DateTimeUnit.DAY).toString(),
                time = "12:00",
                dominantEmotion = "NEUTRAL",
                emotionScores = mapOf("NEUTRAL" to 0.8f, "SADNESS" to 0.2f),
                contextTags = listOf(ContextTag.FAMILY),
                moodNote = "Quiet day at home. Taking time to process emotions and be with family.",
                userId = "test_user"
            ))
            
            // 7 days ago - Work success
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(7, DateTimeUnit.DAY).toString(),
                time = "16:00",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.95f, "NEUTRAL" to 0.05f),
                contextTags = listOf(ContextTag.WORK),
                moodNote = "Presentation went amazing! Client loved our proposal. Team celebration tonight!",
                userId = "test_user"
            ))
            
            // 8 days ago - Travel day
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(8, DateTimeUnit.DAY).toString(),
                time = "6:30",
                dominantEmotion = "NEUTRAL",
                emotionScores = mapOf("NEUTRAL" to 0.6f, "ANGER" to 0.4f),
                contextTags = listOf(ContextTag.TRAVEL),
                moodNote = "Early flight today. Airport was crowded and stressful.",
                userId = "test_user"
            ))
            
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(8, DateTimeUnit.DAY).toString(),
                time = "19:15",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.8f, "NEUTRAL" to 0.2f),
                contextTags = listOf(ContextTag.TRAVEL, ContextTag.FOOD),
                moodNote = "Made it safely! Trying local cuisine at this amazing restaurant.",
                userId = "test_user"
            ))
            
            // 9 days ago - Gym achievement
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(9, DateTimeUnit.DAY).toString(),
                time = "7:00",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.85f, "NEUTRAL" to 0.15f),
                contextTags = listOf(ContextTag.GYM),
                moodNote = "New personal record on deadlift! All that training is paying off.",
                userId = "test_user"
            ))
            
            // 10 days ago - Food day
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(10, DateTimeUnit.DAY).toString(),
                time = "13:30",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.75f, "NEUTRAL" to 0.25f),
                contextTags = listOf(ContextTag.FOOD, ContextTag.SOCIAL),
                moodNote = "Cooking class was so much fun! Made pasta from scratch with friends.",
                userId = "test_user"
            ))
            
            // 11 days ago - Work stress
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(11, DateTimeUnit.DAY).toString(),
                time = "15:45",
                dominantEmotion = "ANGER",
                emotionScores = mapOf("ANGER" to 0.7f, "NEUTRAL" to 0.3f),
                contextTags = listOf(ContextTag.WORK),
                moodNote = "System crashed right before important demo. Tech issues are so frustrating!",
                userId = "test_user"
            ))
            
            // 12 days ago - Sleep issues
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(12, DateTimeUnit.DAY).toString(),
                time = "8:45",
                dominantEmotion = "NEUTRAL",
                emotionScores = mapOf("NEUTRAL" to 0.6f, "SADNESS" to 0.4f),
                contextTags = listOf(ContextTag.SLEEP),
                moodNote = "Didn't sleep well last night. Feeling groggy and need more coffee.",
                userId = "test_user"
            ))
            
            // 13 days ago - Family time
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(13, DateTimeUnit.DAY).toString(),
                time = "17:00",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.88f, "NEUTRAL" to 0.12f),
                contextTags = listOf(ContextTag.FAMILY),
                moodNote = "Sunday family barbecue. Dad told his classic jokes and everyone laughed.",
                userId = "test_user"
            ))
            
            // 14 days ago - Mixed day
            add(EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(14, DateTimeUnit.DAY).toString(),
                time = "11:15",
                dominantEmotion = "NEUTRAL",
                emotionScores = mapOf("NEUTRAL" to 0.8f, "JOY" to 0.2f),
                contextTags = listOf(ContextTag.WORK),
                moodNote = "Regular Saturday work catch-up. Nothing special but productive.",
                userId = "test_user"
            ))
        }
    }
    
    /**
     * Quick mock data for minimal testing (last 3 days)
     */
    fun generateSimpleMockData(): List<EmotionEntry> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val baseDate = now.date
        
        return listOf(
            EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.toString(),
                time = "9:00",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.8f, "NEUTRAL" to 0.2f),
                contextTags = listOf(ContextTag.WORK),
                moodNote = "Great start to the day!",
                userId = "test_user"
            ),
            EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(1, DateTimeUnit.DAY).toString(),
                time = "14:30",
                dominantEmotion = "NEUTRAL",
                emotionScores = mapOf("NEUTRAL" to 0.7f, "JOY" to 0.3f),
                contextTags = listOf(ContextTag.GYM),
                moodNote = "Solid workout session",
                userId = "test_user"
            ),
            EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.minus(2, DateTimeUnit.DAY).toString(),
                time = "19:15",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.9f, "NEUTRAL" to 0.1f),
                contextTags = listOf(ContextTag.FAMILY, ContextTag.PET),
                moodNote = "Family time with the dog",
                userId = "test_user"
            )
        )
    }
    
    /**
     * Generate today's entries only
     */
    fun generateTodayMockData(): List<EmotionEntry> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val baseDate = now.date
        
        return listOf(
            EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.toString(),
                time = "8:00",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.85f, "NEUTRAL" to 0.15f),
                contextTags = listOf(ContextTag.GYM),
                moodNote = "Morning workout complete! Ready to tackle the day.",
                userId = "test_user"
            ),
            EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.toString(),
                time = "12:30",
                dominantEmotion = "NEUTRAL",
                emotionScores = mapOf("NEUTRAL" to 0.7f, "JOY" to 0.3f),
                contextTags = listOf(ContextTag.WORK, ContextTag.FOOD),
                moodNote = "Lunch break. Productive morning at the office.",
                userId = "test_user"
            ),
            EmotionEntry(
                id = IdGenerator.generateUuidLikeId(),
                date = baseDate.toString(),
                time = "18:45",
                dominantEmotion = "JOY",
                emotionScores = mapOf("JOY" to 0.9f, "NEUTRAL" to 0.1f),
                contextTags = listOf(ContextTag.SOCIAL, ContextTag.FOOD),
                moodNote = "Dinner with friends at that new restaurant we've been wanting to try!",
                userId = "test_user"
            )
        )
    }
}
