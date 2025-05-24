// File: shared/src/commonMain/kotlin/com/reflect/app/viewmodels/EnhancedEmotionDetectionViewModel.kt
package com.reflect.app.viewmodels

import com.reflect.app.ml.Emotion
import com.reflect.app.ml.usecase.EmotionDetectionUseCase
import com.reflect.app.ml.viewmodel.FaceDetectionState
import com.reflect.app.models.ContextTag
import com.reflect.app.models.EmotionEntry
import com.reflect.app.models.ViewModel
import com.reflect.app.utils.IdGenerator // Import our ID generator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

// Enhanced state to include captured bitmap and save functionality
sealed class EnhancedEmotionDetectionState {
    object Initial : EnhancedEmotionDetectionState()
    object Loading : EnhancedEmotionDetectionState()
    data class Success(
        val dominantEmotion: Emotion,
        val emotionScores: Map<Emotion, Float>,
        val capturedImagePath: String? = null
    ) : EnhancedEmotionDetectionState()
    data class Saved(val entry: EmotionEntry) : EnhancedEmotionDetectionState()
    data class Error(val message: String) : EnhancedEmotionDetectionState()
}

class EnhancedEmotionDetectionViewModel(
    private val emotionDetectionUseCase: EmotionDetectionUseCase,
    private val calendarViewModel: CalendarViewModel,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val _detectionState = MutableStateFlow<EnhancedEmotionDetectionState>(EnhancedEmotionDetectionState.Initial)
    val detectionState: StateFlow<EnhancedEmotionDetectionState> = _detectionState.asStateFlow()

    private val _faceDetectionState = MutableStateFlow<FaceDetectionState>(FaceDetectionState.Initial)
    val faceDetectionState: StateFlow<FaceDetectionState> = _faceDetectionState.asStateFlow()

    // Store the current detection results for saving
    private var currentDetectionResults: Pair<Emotion, Map<Emotion, Float>>? = null
    private var currentImagePath: String? = null

    // Track if this instance should close resources
    private var shouldCloseOnCleared = true

    fun analyzeFaceInImageProxy(imageProxy: Any) {
        coroutineScope.launch {
            try {
                val hasFace = emotionDetectionUseCase.detectFaceInImageProxy(imageProxy)
                val newState = if (hasFace) FaceDetectionState.FaceDetected else FaceDetectionState.NoFaceDetected
                _faceDetectionState.value = newState
            } catch (e: Exception) {
                _faceDetectionState.value = FaceDetectionState.NoFaceDetected
            }
        }
    }

    fun detectEmotion(imageData: ByteArray, width: Int, height: Int, imagePath: String? = null) {
        _detectionState.value = EnhancedEmotionDetectionState.Loading

        coroutineScope.launch {
            try {
                val emotionScores = emotionDetectionUseCase.detectEmotionScores(imageData, width, height)
                val dominantEmotion = emotionScores.maxByOrNull { it.value }?.key ?: Emotion.NEUTRAL

                // Store results for later saving
                currentDetectionResults = dominantEmotion to emotionScores
                currentImagePath = imagePath

                _detectionState.value = EnhancedEmotionDetectionState.Success(
                    dominantEmotion = dominantEmotion,
                    emotionScores = emotionScores,
                    capturedImagePath = imagePath
                )
            } catch (e: Exception) {
                _detectionState.value = EnhancedEmotionDetectionState.Error("Failed to detect emotion: ${e.message}")
            }
        }
    }

    fun saveEmotionEntry(
        contextTags: List<ContextTag>,
        moodNote: String,
        userId: String = "default_user" // In a real app, get from auth
    ) {
        val results = currentDetectionResults ?: return
        val (dominantEmotion, emotionScores) = results

        coroutineScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

                val entry = EmotionEntry(
                    id = IdGenerator.generateUuidLikeId(), // Use our platform-agnostic ID generator
                    date = now.date.toString(),
                    time = "${now.hour}:${now.minute.toString().padStart(2, '0')}",
                    dominantEmotion = dominantEmotion.name,
                    emotionScores = emotionScores.mapKeys { it.key.name },
                    capturedImagePath = currentImagePath,
                    contextTags = contextTags,
                    moodNote = moodNote,
                    userId = userId
                )

                // Save to calendar
                calendarViewModel.addEmotionEntry(entry)

                _detectionState.value = EnhancedEmotionDetectionState.Saved(entry)
            } catch (e: Exception) {
                _detectionState.value = EnhancedEmotionDetectionState.Error("Failed to save entry: ${e.message}")
            }
        }
    }

    fun resetState() {
        _detectionState.value = EnhancedEmotionDetectionState.Initial
        _faceDetectionState.value = FaceDetectionState.Initial
        currentDetectionResults = null
        currentImagePath = null
        // Don't close the detector here, it's still needed
    }

    /**
     * Call this when the screen is being destroyed permanently
     * (not just when navigating away temporarily)
     */
    fun prepareForDestroy() {
        shouldCloseOnCleared = true
    }

    /**
     * Call this when the screen is just being paused/hidden
     * (when navigating away but might come back)
     */
    fun prepareForPause() {
        shouldCloseOnCleared = false
    }

    override fun onCleared() {
        super.onCleared()
        // Only close resources if this is a permanent destroy
        if (shouldCloseOnCleared) {
            emotionDetectionUseCase.close()
        }
    }
}

// Mock data generator for testing
fun generateMockEmotionEntries(): List<EmotionEntry> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val baseDate = now.date

    return listOf(
        EmotionEntry(
            id = IdGenerator.generateUuidLikeId(), // Use our ID generator
            date = baseDate.toString(),
            time = "8:00",
            dominantEmotion = "JOY",
            emotionScores = mapOf("JOY" to 0.8f, "NEUTRAL" to 0.2f),
            contextTags = listOf(ContextTag.GYM, ContextTag.PET),
            moodNote = "I enjoyed a good gym session today and also took my dog for a walk in the park",
            userId = "test_user"
        ),
        EmotionEntry(
            id = IdGenerator.generateUuidLikeId(),
            date = baseDate.toString(),
            time = "14:30",
            dominantEmotion = "JOY",
            emotionScores = mapOf("JOY" to 0.7f, "NEUTRAL" to 0.3f),
            contextTags = listOf(ContextTag.WORK),
            moodNote = "Great progress on the project today",
            userId = "test_user"
        ),
        EmotionEntry(
            id = IdGenerator.generateUuidLikeId(),
            date = baseDate.minus(1, DateTimeUnit.DAY).toString(),
            time = "16:45",
            dominantEmotion = "SADNESS",
            emotionScores = mapOf("SADNESS" to 0.6f, "NEUTRAL" to 0.4f),
            contextTags = listOf(ContextTag.WORK),
            moodNote = "Difficult day at work, feeling overwhelmed",
            userId = "test_user"
        ),
        EmotionEntry(
            id = IdGenerator.generateUuidLikeId(),
            date = baseDate.minus(2, DateTimeUnit.DAY).toString(),
            time = "10:15",
            dominantEmotion = "ANGER",
            emotionScores = mapOf("ANGER" to 0.7f, "NEUTRAL" to 0.3f),
            contextTags = listOf(ContextTag.FAMILY),
            moodNote = "Argument with family member this morning",
            userId = "test_user"
        )
    )
}