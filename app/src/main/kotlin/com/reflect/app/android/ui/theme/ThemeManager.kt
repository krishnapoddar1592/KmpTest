// File: ui/theme/ThemeManager.kt
package com.reflect.app.android.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager class to handle theme changes in the app
 */
class ThemeManager {
    private val _currentTheme = MutableStateFlow(EmotionAppTheme.COSMIC)
    val currentTheme: StateFlow<EmotionAppTheme> = _currentTheme.asStateFlow()

    /**
     * Toggle between available themes
     */
    fun toggleTheme() {
        print("current theme"+" "+_currentTheme.value)

        _currentTheme.value = when (_currentTheme.value) {
            EmotionAppTheme.COSMIC -> EmotionAppTheme.SERENE
            EmotionAppTheme.SERENE -> EmotionAppTheme.COSMIC
        }
        print("theme changed to"+" "+_currentTheme.value)
    }

    /**
     * Set a specific theme
     */
    fun setTheme(theme: EmotionAppTheme) {
        _currentTheme.value = theme
    }
}