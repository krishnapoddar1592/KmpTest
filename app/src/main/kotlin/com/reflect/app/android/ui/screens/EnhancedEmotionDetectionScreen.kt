package com.reflect.app.android.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.reflect.app.viewmodels.EnhancedEmotionDetectionViewModel

@Composable
fun EnhancedEmotionDetectionScreen(
    viewModel: EnhancedEmotionDetectionViewModel,
    onNavigateBack: () -> Unit
) {
    var capturedImagePath by remember { mutableStateOf<String?>(null) }

    // Reset state when leaving the screen
    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    // Always use EmotionDetectionScreen - it now handles all states internally
    EmotionDetectionScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}