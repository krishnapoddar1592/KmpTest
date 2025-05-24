// File: app/src/main/kotlin/com/reflect/app/android/ui/screens/DayInsightsScreen.kt
package com.reflect.app.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reflect.app.android.R
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.ml.Emotion
import com.reflect.app.models.ContextTag
import com.reflect.app.models.EmotionEntry
import com.reflect.app.models.getFormattedTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun DayInsightsScreen(
    date: LocalDate,
    entries: List<EmotionEntry>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val formattedDate = try {
        date.format(LocalDate.Format { byUnicodePattern("MMM d, yyyy") })
    } catch (e: Exception) {
        "${date.month.name.take(3)} ${date.dayOfMonth}, ${date.year}"
    }
    
    val averageMood = calculateAverageMood(entries)
    val dominantEmotion = calculateDominantEmotion(entries)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EmotionTheme.colors.background)
    ) {
        // Header
        DayInsightsHeader(
            dayOfWeek = dayOfWeek,
            formattedDate = formattedDate,
            averageMood = averageMood,
            dominantEmotion = dominantEmotion,
            onNavigateBack = onNavigateBack
        )
        
        // Emotion entries list
        if (entries.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(entries) { entry ->
                    EmotionEntryCard(entry = entry)
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No emotion data for this day",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmotionTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start tracking your emotions to see insights here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmotionTheme.colors.textSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DayInsightsHeader(
    dayOfWeek: String,
    formattedDate: String,
    averageMood: Emotion?,
    dominantEmotion: Emotion?,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmotionTheme.colors.background)
            .padding(16.dp)
    ) {
        // Top bar with back button and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = EmotionTheme.colors.textPrimary
                )
            }
            
            Text(
                text = "Insights",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = EmotionTheme.colors.textPrimary
            )
            
            // Average mood indicator
            if (averageMood != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Average Mood",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmotionTheme.colors.textSecondary
                    )
                    Text(
                        text = getEmotionEmoji(averageMood),
                        fontSize = 24.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Date display
        Column {
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = EmotionTheme.colors.textPrimary
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyLarge,
                color = EmotionTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun EmotionEntryCard(
    entry: EmotionEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emotion photo placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmotionTheme.colors.backgroundSecondary)
            ) {
                // For now, show a placeholder. In a real app, you'd load the actual captured image
                Image(
                    painter = painterResource(id = R.drawable.avatar), // Use your placeholder image
                    contentDescription = "Captured emotion photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Emotion details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mood triggered
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Mood Triggered",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmotionTheme.colors.textSecondary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
//                        try {
                            val emotion = Emotion.valueOf(entry.dominantEmotion.uppercase())
                            Text(
                                text = getEmotionEmoji(emotion),
                                fontSize = 16.sp
                            )
                            Text(
                                text = emotion.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = getEmotionColor(emotion)
                                )
                            )
//                        } catch (e: Exception) {
//                            Text(
//                                text = entry.dominantEmotion,
//                                style = MaterialTheme.typography.bodyMedium.copy(
//                                    fontWeight = FontWeight.SemiBold
//                                ),
//                                color = EmotionTheme.colors.textPrimary
//                            )
//                        }
                    }
                }
                
                // Context tags
                if (entry.contextTags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(entry.contextTags) { tag ->
                            ContextTagChip(tag = tag)
                        }
                    }
                }
                
                // Time of scan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Time of Scan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmotionTheme.colors.textSecondary
                    )
                    Text(
                        text = entry.getFormattedTime(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = EmotionTheme.colors.textPrimary
                    )
                }
                
                // Mood note
                if (entry.moodNote.isNotBlank()) {
                    Column {
                        Text(
                            text = "Mood Note",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = EmotionTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = entry.moodNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = EmotionTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextTagChip(
    tag: ContextTag,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                EmotionTheme.colors.backgroundSecondary,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                EmotionTheme.colors.textSecondary.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = tag.icon,
            fontSize = 12.sp
        )
        Text(
            text = tag.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = EmotionTheme.colors.textPrimary
        )
    }
}

private fun calculateAverageMood(entries: List<EmotionEntry>): Emotion? {
    if (entries.isEmpty()) return null
    
    val emotionCounts = mutableMapOf<String, Int>()
    entries.forEach { entry ->
        emotionCounts[entry.dominantEmotion] = emotionCounts.getOrDefault(entry.dominantEmotion, 0) + 1
    }
    
    val mostCommon = emotionCounts.maxByOrNull { it.value }?.key
    return try {
        mostCommon?.let { Emotion.valueOf(it.uppercase()) }
    } catch (e: Exception) {
        null
    }
}

private fun calculateDominantEmotion(entries: List<EmotionEntry>): Emotion? {
    return calculateAverageMood(entries) // For now, same as average mood
}

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