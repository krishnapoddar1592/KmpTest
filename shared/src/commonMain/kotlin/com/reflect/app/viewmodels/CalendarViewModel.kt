package com.reflect.app.viewmodels

import com.reflect.app.data.local.dao.MockEmotionData
import com.reflect.app.models.DayEmotionSummary
import com.reflect.app.models.EmotionEntry
import com.reflect.app.models.ViewModel
import com.reflect.app.models.getLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

class CalendarViewModel : ViewModel() {
    private val _emotionEntries = MutableStateFlow<Map<LocalDate, List<EmotionEntry>>>(emptyMap())
    val emotionEntries: StateFlow<Map<LocalDate, List<EmotionEntry>>> = _emotionEntries.asStateFlow()

    private val _dayEmotionSummaries = MutableStateFlow<Map<LocalDate, DayEmotionSummary>>(emptyMap())
    val dayEmotionSummaries: StateFlow<Map<LocalDate, DayEmotionSummary>> = _dayEmotionSummaries.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    fun addEmotionEntry(entry: EmotionEntry) {
        val date = entry.getLocalDate()
        val currentEntries = _emotionEntries.value
        val dateEntries = currentEntries[date]?.toMutableList() ?: mutableListOf()
        dateEntries.add(entry)

        _emotionEntries.value = currentEntries + (date to dateEntries)

        // Update summary for this date
        updateDayEmotionSummary(date, dateEntries)
    }

    fun getEntriesForDate(date: LocalDate): List<EmotionEntry> {
        return _emotionEntries.value[date] ?: emptyList()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    private fun updateDayEmotionSummary(date: LocalDate, entries: List<EmotionEntry>) {
        if (entries.isEmpty()) return

        // Calculate dominant emotion (most frequent)
        val emotionCounts = mutableMapOf<String, Int>()
        var totalMoodScore = 0f

        entries.forEach { entry ->
            emotionCounts[entry.dominantEmotion] = (emotionCounts[entry.dominantEmotion] ?: 0) + 1

            // Calculate a simple mood score (Joy=1, Neutral=0.5, Sadness/Anger=0)
            totalMoodScore += when (entry.dominantEmotion.uppercase()) {
                "JOY" -> 1f
                "NEUTRAL" -> 0.5f
                else -> 0f
            }
        }

        val dominantEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: "NEUTRAL"
        val averageMood = totalMoodScore / entries.size

        // Calculate emotion distribution
        val totalEntries = entries.size.toFloat()
        val emotionDistribution = emotionCounts.mapValues { it.value / totalEntries }

        val summary = DayEmotionSummary(
            date = date.toString(),
            dominantEmotion = dominantEmotion,
            averageMood = averageMood,
            entryCount = entries.size,
            emotionDistribution = emotionDistribution
        )

        val currentSummaries = _dayEmotionSummaries.value
        _dayEmotionSummaries.value = currentSummaries + (date to summary)
    }

    // Enhanced mock data loading methods
    fun loadFullMockData() {
        val mockEntries = MockEmotionData.generateMockEmotionEntries()
        mockEntries.forEach { entry ->
            addEmotionEntry(entry)
        }
    }

    fun loadSimpleMockData() {
        val mockEntries = MockEmotionData.generateSimpleMockData()
        mockEntries.forEach { entry ->
            addEmotionEntry(entry)
        }
    }

    fun loadTodayMockData() {
        val mockEntries = MockEmotionData.generateTodayMockData()
        mockEntries.forEach { entry ->
            addEmotionEntry(entry)
        }
    }

    fun clearAllData() {
        _emotionEntries.value = emptyMap()
        _dayEmotionSummaries.value = emptyMap()
        _selectedDate.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}