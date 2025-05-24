// File: app/src/main/kotlin/com/reflect/app/android/ui/screens/EnhancedCalendarScreen.kt
package com.reflect.app.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.ml.Emotion
import com.reflect.app.models.DayEmotionSummary
import com.reflect.app.models.EmotionEntry
import com.reflect.app.models.getFormattedTime
import kotlinx.datetime.*

@Composable
fun CalendarScreen(
    onDayClick: (LocalDate) -> Unit,
    emotionData: Map<LocalDate, DayEmotionSummary> = emptyMap(),
    emotionEntries: Map<LocalDate, List<EmotionEntry>> = emptyMap(),
    modifier: Modifier = Modifier
) {
    var currentDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
    var selectedViewType by remember { mutableStateOf(ViewType.MONTHLY) }
    var selectedEmotionFilter by remember { mutableStateOf<Emotion?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EmotionTheme.colors.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Your Emotional Timeline",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = EmotionTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // View type toggle
        ViewTypeToggle(
            selectedType = selectedViewType,
            onTypeSelected = { selectedViewType = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Date navigation
        DateNavigation(
            currentDate = currentDate,
            viewType = selectedViewType,
            onPreviousClick = {
                currentDate = when (selectedViewType) {
                    ViewType.WEEKLY -> currentDate.minus(7, DateTimeUnit.DAY)
                    ViewType.MONTHLY -> {
                        val currentMonth = YearMonth(currentDate.year, currentDate.month)
                        val previousMonth = currentMonth.minusMonths(1)
                        previousMonth.atDay(1)
                    }
                }
            },
            onNextClick = {
                currentDate = when (selectedViewType) {
                    ViewType.WEEKLY -> currentDate.plus(7, DateTimeUnit.DAY)
                    ViewType.MONTHLY -> {
                        val currentMonth = YearMonth(currentDate.year, currentDate.month)
                        val nextMonth = currentMonth.plusMonths(1)
                        nextMonth.atDay(1)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Emotion filters
        EmotionFilterRow(
            selectedEmotion = selectedEmotionFilter,
            onEmotionSelected = { emotion ->
                selectedEmotionFilter = if (selectedEmotionFilter == emotion) null else emotion
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Calendar content based on view type
        when (selectedViewType) {
            ViewType.WEEKLY -> {
                WeeklyCalendarView(
                    currentDate = currentDate,
                    emotionData = emotionData,
                    emotionEntries = emotionEntries,
                    selectedEmotionFilter = selectedEmotionFilter,
                    onDayClick = onDayClick
                )
            }
            ViewType.MONTHLY -> {
                MonthlyCalendarGrid(
                    currentDate = currentDate,
                    emotionData = emotionData,
                    selectedEmotionFilter = selectedEmotionFilter,
                    onDayClick = onDayClick
                )
            }
        }
    }
}

@Composable
private fun WeeklyCalendarView(
    currentDate: LocalDate,
    emotionData: Map<LocalDate, DayEmotionSummary>,
    emotionEntries: Map<LocalDate, List<EmotionEntry>>,
    selectedEmotionFilter: Emotion?,
    onDayClick: (LocalDate) -> Unit
) {
    // Get the week containing the current date
    val startOfWeek = getStartOfWeek(currentDate)
    val weekDays = (0..6).map { startOfWeek.plus(it, DateTimeUnit.DAY) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Week days header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = EmotionTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly view cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(weekDays) { date ->
                WeeklyDayCard(
                    date = date,
                    emotionSummary = emotionData[date],
                    emotionEntries = emotionEntries[date] ?: emptyList(),
                    isFiltered = selectedEmotionFilter?.let { filter ->
                        emotionData[date]?.dominantEmotion != filter.name
                    } ?: false,
                    isToday = date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    onClick = { onDayClick(date) }
                )
            }
        }
    }
}

@Composable
private fun WeeklyDayCard(
    date: LocalDate,
    emotionSummary: DayEmotionSummary?,
    emotionEntries: List<EmotionEntry>,
    isFiltered: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isFiltered -> EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.3f)
        emotionSummary == null -> EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f)
        else -> EmotionTheme.colors.backgroundSecondary
    }

    val borderColor = if (isToday) EmotionTheme.colors.interactive else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isFiltered) { onClick() }
            .then(
                if (isToday) {
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp))
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emotionSummary != null) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isToday) EmotionTheme.colors.interactive else EmotionTheme.colors.textPrimary
                )
                Text(
                    text = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = EmotionTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Emotion info
            if (emotionSummary != null && !isFiltered) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Dominant emotion
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
//                        try {
                            val emotion = Emotion.valueOf(emotionSummary.dominantEmotion.uppercase())
                            Text(
                                text = getEmotionEmoji(emotion),
                                fontSize = 20.sp
                            )
                            Text(
                                text = emotion.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = getEmotionColor(emotion)
                            )
//                        } catch (e: Exception) {
//                            Text(
//                                text = emotionSummary.dominantEmotion,
//                                style = MaterialTheme.typography.titleMedium,
//                                color = EmotionTheme.colors.textPrimary
//                            )
//                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Entry count and time info
                    Text(
                        text = "${emotionSummary.entryCount} ${if (emotionSummary.entryCount == 1) "entry" else "entries"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmotionTheme.colors.textSecondary
                    )

                    // Show first entry time if available
                    if (emotionEntries.isNotEmpty()) {
                        Text(
                            text = "First: ${emotionEntries.first().getFormattedTime()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmotionTheme.colors.textSecondary
                        )
                    }
                }

                // Emotion dots for multiple entries
                if (emotionEntries.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.width(80.dp)
                    ) {
                        items(emotionEntries.take(5)) { entry ->
//                            try {
                                val emotion = Emotion.valueOf(entry.dominantEmotion.uppercase())
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            getEmotionColor(emotion),
                                            CircleShape
                                        )
                                )
//                            } catch (e: Exception) {
//                                Box(
//                                    modifier = Modifier
//                                        .size(8.dp)
//                                        .background(
//                                            EmotionTheme.colors.textSecondary,
//                                            CircleShape
//                                        )
//                                )
//                            }
                        }
                        if (emotionEntries.size > 5) {
                            item {
                                Text(
                                    text = "+${emotionEntries.size - 5}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmotionTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            } else if (!isFiltered) {
                // No emotion data
                Text(
                    text = "No data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmotionTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Filtered out
                Text(
                    text = "Filtered",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmotionTheme.colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DateNavigation(
    currentDate: LocalDate,
    viewType: ViewType,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val displayText = when (viewType) {
        ViewType.WEEKLY -> {
            val startOfWeek = getStartOfWeek(currentDate)
            val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
            if (startOfWeek.month == endOfWeek.month) {
                "${startOfWeek.month.name.take(3)} ${startOfWeek.dayOfMonth}-${endOfWeek.dayOfMonth}"
            } else {
                "${startOfWeek.month.name.take(3)} ${startOfWeek.dayOfMonth} - ${endOfWeek.month.name.take(3)} ${endOfWeek.dayOfMonth}"
            }
        }
        ViewType.MONTHLY -> {
            currentDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous ${viewType.displayName.lowercase()}",
                tint = EmotionTheme.colors.textPrimary
            )
        }

        Text(
            text = displayText,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier
                .background(
                    EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 24.dp, vertical = 8.dp)
        )

        IconButton(
            onClick = onNextClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next ${viewType.displayName.lowercase()}",
                tint = EmotionTheme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun MonthlyCalendarGrid(
    currentDate: LocalDate,
    emotionData: Map<LocalDate, DayEmotionSummary>,
    selectedEmotionFilter: Emotion?,
    onDayClick: (LocalDate) -> Unit
) {
    val currentMonth = YearMonth(currentDate.year, currentDate.month)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = EmotionTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar grid (keep existing implementation)
        val daysInMonth = getDaysInMonth(currentMonth)

        // Create rows of 7 days each
        val weeks = daysInMonth.chunked(7)

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { date ->
                        Box(modifier = Modifier.weight(1f)) {
                            CalendarDay(
                                date = date,
                                emotionSummary = if (date != null) emotionData[date] else null,
                                isFiltered = selectedEmotionFilter?.let { filter ->
                                    if (date != null) {
                                        emotionData[date]?.dominantEmotion != filter.name
                                    } else false
                                } ?: false,
                                onClick = { if (date != null) onDayClick(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
private fun getStartOfWeek(date: LocalDate): LocalDate {
    val dayOfWeek = date.dayOfWeek.ordinal // 0 = Monday, 6 = Sunday
    val daysToSubtract = (dayOfWeek + 1) % 7 // Adjust so Sunday = 0
    return date.minus(daysToSubtract, DateTimeUnit.DAY)
}

//private enum class ViewType(val displayName: String) {
//    WEEKLY("Week"),
//    MONTHLY("Monthly")
//}

// Keep existing helper functions for compatibility
private fun getEmotionEmoji(emotion: Emotion): String {
    return when (emotion) {
        Emotion.JOY -> "😊"
        Emotion.SADNESS -> "😢"
        Emotion.ANGER -> "😠"
        Emotion.NEUTRAL -> "😐"
    }
}

private fun getEmotionColor(emotion: Emotion): Color {
    return when (emotion) {
        Emotion.JOY -> Color(0xFFFFD900)
        Emotion.SADNESS -> Color(0xFF8B5CF6)
        Emotion.ANGER -> Color(0xFFFF4444)
        Emotion.NEUTRAL -> Color(0xFFFF8C00)
    }
}

@Composable
private fun ViewTypeToggle(
    selectedType: ViewType,
    onTypeSelected: (ViewType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.3f),
                RoundedCornerShape(25.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ViewType.values().forEach { type ->
            val isSelected = selectedType == type
            val backgroundColor = if (isSelected) {
                EmotionTheme.colors.interactive
            } else {
                Color.Transparent
            }
            val textColor = if (isSelected) {
                Color.White
            } else {
                EmotionTheme.colors.textSecondary
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(21.dp))
                    .background(backgroundColor)
                    .clickable { onTypeSelected(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun MonthNavigation(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
                .size(40.dp)
                .background(
                    EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = EmotionTheme.colors.textPrimary
            )
        }

        Text(
            text = currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier
                .background(
                    EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 24.dp, vertical = 8.dp)
        )

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .size(40.dp)
                .background(
                    EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = EmotionTheme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun EmotionFilterRow(
    selectedEmotion: Emotion?,
    onEmotionSelected: (Emotion) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(listOf(Emotion.JOY, Emotion.ANGER, Emotion.NEUTRAL, Emotion.SADNESS)) { emotion ->
            EmotionFilterChip(
                emotion = emotion,
                isSelected = selectedEmotion == emotion,
                onClick = { onEmotionSelected(emotion) }
            )
        }
    }
}

@Composable
private fun EmotionFilterChip(
    emotion: Emotion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        getEmotionColor(emotion)
    } else {
        EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f)
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        EmotionTheme.colors.textSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emotion.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = textColor
        )
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    emotionData: Map<LocalDate, DayEmotionSummary>,
    selectedEmotionFilter: Emotion?,
    onDayClick: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = EmotionTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar days
        val daysInMonth = getDaysInMonth(currentMonth)
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(daysInMonth) { date ->
                CalendarDay(
                    date = date,
                    emotionSummary = emotionData[date],
                    isFiltered = selectedEmotionFilter?.let { filter ->
                        emotionData[date]?.dominantEmotion != filter.name
                    } ?: false,
                    onClick = {
                        if (date != null) {
                            onDayClick(date)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    emotionSummary: DayEmotionSummary?,
    isFiltered: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        date == null -> Color.Transparent
        emotionSummary == null -> EmotionTheme.colors.textSecondary.copy(alpha = 0.2f)
        isFiltered -> EmotionTheme.colors.textSecondary.copy(alpha = 0.1f)
        else -> {
            try {
                getEmotionColor(Emotion.valueOf(emotionSummary.dominantEmotion.uppercase()))
            } catch (e: Exception) {
                EmotionTheme.colors.textSecondary.copy(alpha = 0.2f)
            }
        }
    }

    val textColor = when {
        date == null -> Color.Transparent
        emotionSummary == null -> EmotionTheme.colors.textSecondary
        isFiltered -> EmotionTheme.colors.textSecondary.copy(alpha = 0.3f)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (date != null && !isFiltered) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (emotionSummary != null) FontWeight.Bold else FontWeight.Normal
                ),
                color = textColor
            )
        }
    }
}

private fun getDaysInMonth(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal
    val daysInMonth = yearMonth.lengthOfMonth()

    val days = mutableListOf<LocalDate?>()

    // Add empty cells for days before the first day of the month
    repeat(firstDayOfWeek) {
        days.add(null)
    }

    // Add all days of the month
    for (day in 1..daysInMonth) {
        days.add(yearMonth.atDay(day))
    }

    // Add empty cells to complete the grid (42 cells total for 6 weeks)
    while (days.size < 42) {
        days.add(null)
    }

    return days
}



private enum class ViewType(val displayName: String) {
    WEEKLY("Week"),
    MONTHLY("Monthly")
}

// Helper data class for YearMonth (since kotlinx-datetime doesn't have it)
data class YearMonth(val year: Int, val month: Month) {
    fun atDay(day: Int): LocalDate = LocalDate(year, month, day)

    fun lengthOfMonth(): Int {
        return when (month) {
            Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            else -> 31
        }
    }

    fun minusMonths(months: Long): YearMonth {
        var newYear = year
        var newMonth = month.ordinal

        newMonth -= months.toInt()
        while (newMonth < 0) {
            newMonth += 12
            newYear--
        }

        return YearMonth(newYear, Month.values()[newMonth])
    }

    fun plusMonths(months: Long): YearMonth {
        var newYear = year
        var newMonth = month.ordinal

        newMonth += months.toInt()
        while (newMonth >= 12) {
            newMonth -= 12
            newYear++
        }

        return YearMonth(newYear, Month.values()[newMonth])
    }

    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}