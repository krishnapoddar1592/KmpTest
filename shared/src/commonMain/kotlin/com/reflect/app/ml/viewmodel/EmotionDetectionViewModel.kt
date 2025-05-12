package com.reflect.app.ml.viewmodel

import androidx.lifecycle.viewModelScope
import com.reflect.app.ml.Emotion
import com.reflect.app.ml.usecase.EmotionDetectionUseCase
import com.reflect.app.models.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// 2. Updated ViewModel with face detection state
sealed class EmotionDetectionState {
    object Initial : EmotionDetectionState()
    object Loading : EmotionDetectionState()
    data class Success(val dominantEmotion: Emotion, val emotionScores: Map<Emotion, Float>) : EmotionDetectionState()
    data class Error(val message: String) : EmotionDetectionState()
}

// ADD: Face detection state
sealed class FaceDetectionState {
    object Initial : FaceDetectionState()
    object FaceDetected : FaceDetectionState()
    object NoFaceDetected : FaceDetectionState()
}

class EmotionDetectionViewModel(
    private val emotionDetectionUseCase: EmotionDetectionUseCase,
//    private val emotionDetector: AndroidEmotionDetector, // ADD: Direct access to detector
    private val coroutineScope: CoroutineScope
) : ViewModel() {private val _detectionState = MutableStateFlow<EmotionDetectionState>(EmotionDetectionState.Initial)
    val detectionState: StateFlow<EmotionDetectionState> = _detectionState.asStateFlow()

    private val _faceDetectionState = MutableStateFlow<FaceDetectionState>(FaceDetectionState.Initial)
    val faceDetectionState: StateFlow<FaceDetectionState> = _faceDetectionState.asStateFlow()

    // Existing method for ByteArray processing
    fun analyzeFaceInFrame(imageData: ByteArray, width: Int, height: Int) {
        // Using print for simplicity as in your example, replace with proper logging
        print("EmotionDetectionViewModel, Analyzing frame (ByteArray) - size: ${imageData.size}, dimensions: ${width}x${height}\n")
        viewModelScope.launch {
            try {
                val hasFace = emotionDetectionUseCase.detectFace(imageData, width, height)
                val newState = if (hasFace) FaceDetectionState.FaceDetected else FaceDetectionState.NoFaceDetected
                print("EmotionDetectionViewModel Face detection (ByteArray) result: $hasFace, updating state to: $newState\n")
                _faceDetectionState.value = newState
            } catch (e: Exception) {
                print("EmotionDetectionViewModel Error during face detection (ByteArray) $e\n")
                _faceDetectionState.value = FaceDetectionState.NoFaceDetected
            }
        }
    }

    // New method for ImageProxy processing from ImageAnalysis
    fun analyzeFaceInImageProxy(imageProxy: Any) { // Parameter is Any
        // Using print for simplicity, replace with proper logging
        // print("EmotionDetectionViewModel, Analyzing frame (ImageProxy)\n")
        viewModelScope.launch {
            try {
                // The imageProxy object is passed through.
                // The AndroidEmotionDetector is responsible for casting and closing it.
                val hasFace = emotionDetectionUseCase.detectFaceInImageProxy(imageProxy)
                val newState = if (hasFace) FaceDetectionState.FaceDetected else FaceDetectionState.NoFaceDetected
                // print("EmotionDetectionViewModel Face detection (ImageProxy) result: $hasFace, updating state to: $newState\n")
                _faceDetectionState.value = newState
            } catch (e: Exception) {
                // The imageProxy should be closed by the detector even in case of exceptions there.
                print("EmotionDetectionViewModel Error during ImageProxy face detection: $e\n")
                _faceDetectionState.value = FaceDetectionState.NoFaceDetected
            }
        }
    }

    fun detectEmotion(imageData: ByteArray, width: Int, height: Int) {
        _detectionState.value = EmotionDetectionState.Loading
        // Using viewModelScope if private coroutineScope is removed
        viewModelScope.launch {
            try {
                val emotionScores = emotionDetectionUseCase.detectEmotionScores(imageData, width, height)
                val dominantEmotion = emotionScores.maxByOrNull { it.value }?.key ?: Emotion.NEUTRAL
                _detectionState.value = EmotionDetectionState.Success(dominantEmotion, emotionScores)
                println("Detection successful: ${dominantEmotion.name}")
            } catch (e: Exception) {
                _detectionState.value = EmotionDetectionState.Error("Failed to detect emotion: ${e.message}")
                println("Detection error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _detectionState.value = EmotionDetectionState.Initial
        _faceDetectionState.value = FaceDetectionState.Initial
        println("EmotionDetectionState reset to Initial")
    }

    override fun onCleared() {
        super.onCleared()
        emotionDetectionUseCase.close()
        println("EmotionDetectionViewModel cleared and resources released.")
    }
}