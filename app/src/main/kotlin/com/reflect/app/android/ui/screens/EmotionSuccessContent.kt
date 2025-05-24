// File: app/src/main/kotlin/com/reflect/app/android/ui/screens/EmotionSuccessScreen.kt
package com.reflect.app.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import com.reflect.app.android.R
import com.reflect.app.android.ui.components.EmotionButton
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.ml.Emotion
import com.reflect.app.models.ContextTag
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun EmotionSuccessContent(
    dominantEmotion: Emotion,
    emotionScores: Map<Emotion, Float>,
    capturedBitmap: Bitmap?,
    onSaveEntry: (selectedTags: List<ContextTag>, note: String) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTags by remember { mutableStateOf(setOf<ContextTag>()) }
    var moodNote by remember { mutableStateOf("") }
    var showAllTags by remember { mutableStateOf(false) }
    
    val currentTime = remember {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with detected emotion
        Text(
            text = "Mood Triggered",
            style = MaterialTheme.typography.headlineSmall,
            color = EmotionTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Emotion result with emoji
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = getEmotionEmoji(dominantEmotion),
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = dominantEmotion.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = getEmotionColor(dominantEmotion)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Captured image preview
        capturedBitmap?.let { bitmap ->
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmotionTheme.colors.backgroundSecondary)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured emotion",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Context tags section
        Text(
            text = "Add Context Tags:",
            style = MaterialTheme.typography.titleMedium,
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tag selection
        val visibleTags = if (showAllTags) ContextTag.values().toList() 
                         else ContextTag.values().take(4).toList()
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(visibleTags) { tag ->
                ContextTagChip(
                    tag = tag,
                    isSelected = selectedTags.contains(tag),
                    onToggle = { 
                        selectedTags = if (selectedTags.contains(tag)) {
                            selectedTags - tag
                        } else {
                            selectedTags + tag
                        }
                    }
                )
            }
            
            if (!showAllTags && ContextTag.values().size > 4) {
                item {
                    IconButton(
                        onClick = { showAllTags = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                EmotionTheme.colors.backgroundSecondary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "More tags",
                            tint = EmotionTheme.colors.interactive
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Time display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Time of Scan",
                style = MaterialTheme.typography.bodyLarge,
                color = EmotionTheme.colors.textSecondary
            )
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = EmotionTheme.colors.textPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mood note section
        Text(
            text = "Mood Note",
            style = MaterialTheme.typography.titleMedium,
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = moodNote,
            onValueChange = { moodNote = it },
            placeholder = { 
                Text(
                    "I enjoyed a good gym session today and also took my dog for a walk in the park",
                    color = EmotionTheme.colors.textSecondary.copy(alpha = 0.7f)
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmotionTheme.colors.interactive,
                unfocusedBorderColor = EmotionTheme.colors.textSecondary.copy(alpha = 0.3f),
                focusedTextColor = EmotionTheme.colors.textPrimary,
                unfocusedTextColor = EmotionTheme.colors.textPrimary,
                cursorColor = EmotionTheme.colors.interactive,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            maxLines = 4
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmotionButton(
                text = "Save Entry",
                onClick = { 
                    onSaveEntry(selectedTags.toList(), moodNote)
                },
                modifier = Modifier.weight(1f),
                contentColor = EmotionTheme.colors.textPrimary
            )
            
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EmotionTheme.colors.interactive
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    EmotionTheme.colors.interactive
                )
            ) {
                Text(
                    text = "Restart",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ContextTagChip(
    tag: ContextTag,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        EmotionTheme.colors.interactive
    } else {
        EmotionTheme.colors.backgroundSecondary
    }
    
    val textColor = if (isSelected) {
        Color.White
    } else {
        EmotionTheme.colors.textPrimary
    }
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(
                if (!isSelected) {
                    Modifier.border(
                        1.dp,
                        EmotionTheme.colors.textSecondary.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    )
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = tag.icon,
            fontSize = 16.sp
        )
        Text(
            text = tag.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
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
        Emotion.SADNESS -> Color(0xFF1E2761)
        Emotion.ANGER -> Color(0xFFFF4444)
        Emotion.NEUTRAL -> Color(0xFF888888)
    }
}