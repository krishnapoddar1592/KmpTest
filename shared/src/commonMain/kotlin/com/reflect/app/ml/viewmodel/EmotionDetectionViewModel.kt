// shared/src/commonMain/kotlin/com/reflect/app/ml/viewmodel/EmotionDetectionViewModel.kt
package com.reflect.app.ml.viewmodel

import com.reflect.app.ml.Emotion
import com.reflect.app.ml.usecase.EmotionDetectionUseCase
import com.reflect.app.models.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EmotionDetectionState {
    object Initial : EmotionDetectionState()
    object Loading : EmotionDetectionState()
    data class Success(val dominantEmotion: Emotion, val emotionScores: Map<Emotion, Float>) : EmotionDetectionState()
    data class Error(val message: String) : EmotionDetectionState()
}

class EmotionDetectionViewModel(
    private val emotionDetectionUseCase: EmotionDetectionUseCase,
    private val coroutineScope: CoroutineScope
) : ViewModel() {
    
    private val _detectionState = MutableStateFlow<EmotionDetectionState>(EmotionDetectionState.Initial)
    val detectionState: StateFlow<EmotionDetectionState> = _detectionState.asStateFlow()
    
    fun detectEmotion(imageData: ByteArray, width: Int, height: Int) {
        _detectionState.value = EmotionDetectionState.Loading
        
        coroutineScope.launch {
            try {
                val emotionScores = emotionDetectionUseCase.detectEmotionScores(imageData, width, height)
                val dominantEmotion = emotionScores.maxByOrNull { it.value }?.key ?: Emotion.NEUTRAL
                
                _detectionState.value = EmotionDetectionState.Success(dominantEmotion, emotionScores)
            } catch (e: Exception) {
                _detectionState.value = EmotionDetectionState.Error("Failed to detect emotion: ${e.message}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        emotionDetectionUseCase.close()
    }
}