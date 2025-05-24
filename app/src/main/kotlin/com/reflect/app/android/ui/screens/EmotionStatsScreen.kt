// File: app/src/main/kotlin/com/reflect/app/android/ui/screens/EmotionStatsScreen.kt
package com.reflect.app.android.ui.screens

// Add this to your MainScreen.kt imports:
// import com.reflect.app.android.ui.screens.EmotionStatsScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reflect.app.android.R
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.android.ui.theme.Typography
import kotlin.math.*

@Composable
fun EmotionStatsScreen() {
    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Overview", "Trends", "Patterns", "Insights")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmotionTheme.colors.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Statistics",
            style = Typography.displayMedium.copy(fontSize = 32.sp),
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Tab Row
        TabSection(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Content based on selected tab
        when (selectedTab) {
            "Overview" -> OverviewContent()
            "Trends" -> TrendsContent()
            "Patterns" -> PatternsContent()
            "Insights" -> InsightsContent()
        }
    }
}

@Composable
private fun TabSection(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(tabs) { tab ->
            TabButton(
                text = tab,
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) EmotionTheme.colors.interactive
                else EmotionTheme.colors.backgroundSecondary
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else EmotionTheme.colors.textSecondary,
            style = Typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun OverviewContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Emotional Balance Score
            EmotionalBalanceCard()
        }
        
        item {
            // Emotional Distribution
            EmotionalDistributionCard()
        }
        
        item {
            // Recent Changes
            RecentChangesCard()
        }
    }
}

@Composable
private fun EmotionalBalanceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = EmotionTheme.colors.backgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emotional Balance score",
                    style = Typography.titleMedium,
                    color = EmotionTheme.colors.textPrimary
                )
                
                Text(
                    text = "Week",
                    style = Typography.bodyMedium,
                    color = EmotionTheme.colors.textSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Circular Progress
            CircularProgressIndicator(
                score = 78,
                maxScore = 100
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Your Balance is good.",
                style = Typography.bodyLarge,
                color = EmotionTheme.colors.textPrimary
            )
            
            Text(
                text = "EBS increased by 7% from last week",
                style = Typography.bodyMedium,
                color = EmotionTheme.colors.interactive
            )
        }
    }
}

@Composable
private fun CircularProgressIndicator(
    score: Int,
    maxScore: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score.toFloat() / maxScore.toFloat(),
        animationSpec = tween(durationMillis = 2000)
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(160.dp)
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.width - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background circle
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            drawArc(
                color = Color(0xFF64B4F6),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = score.toString(),
                style = Typography.displayMedium.copy(fontSize = 48.sp),
                color = EmotionTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "out of $maxScore",
                style = Typography.bodyMedium,
                color = EmotionTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun EmotionalDistributionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = EmotionTheme.colors.backgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emotional Distribution",
                    style = Typography.titleMedium,
                    color = EmotionTheme.colors.textPrimary
                )
                
                Text(
                    text = "Comparison",
                    style = Typography.bodyMedium,
                    color = EmotionTheme.colors.textSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val emotions = listOf(
                EmotionData("Joy", 50, 25, Color(0xFFFFE45C)),
                EmotionData("Sadness", 25, -12, Color(0xFF1E2761)),
                EmotionData("Anger", 12, 1, Color(0xFFFF8347)),
                EmotionData("Stress", 13, -2, Color(0xFFFFD980))
            )
            
            emotions.forEach { emotion ->
                EmotionProgressBar(emotion)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EmotionProgressBar(emotion: EmotionData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emotion.name,
            style = Typography.bodyMedium,
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier.width(80.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(
                    Color.Gray.copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp)
                )
        ) {
            val animatedProgress by animateFloatAsState(
                targetValue = emotion.percentage / 100f,
                animationSpec = tween(durationMillis = 1500)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        emotion.color,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "${emotion.percentage}%",
            style = Typography.bodyMedium,
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier.width(40.dp)
        )
        
        Text(
            text = "(${if (emotion.change >= 0) "+" else ""}${emotion.change}%)",
            style = Typography.bodySmall,
            color = if (emotion.change >= 0) Color.Green else Color.Red,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun RecentChangesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = EmotionTheme.colors.backgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Recent Changes",
                style = Typography.titleMedium,
                color = EmotionTheme.colors.textPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChangeCard(
                    title = "Joy +7%",
                    subtitle = "from last week",
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                ChangeCard(
                    title = "Stress -15%",
                    subtitle = "from last month",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChangeCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = EmotionTheme.colors.background
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = Typography.bodyMedium,
                color = EmotionTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = Typography.bodySmall,
                color = EmotionTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun TrendsContent() {
    Column {
        // Time period selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DropdownSelector(
                label = "Week",
                modifier = Modifier.weight(1f)
            )
            DropdownSelector(
                label = "All Emotions",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chart placeholder - you would implement actual charting here
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(
                containerColor = EmotionTheme.colors.backgroundSecondary
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Emotion Trends Chart\n(Week View)",
                    style = Typography.bodyLarge,
                    color = EmotionTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Key Observations
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = EmotionTheme.colors.backgroundSecondary
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Key Observations",
                    style = Typography.titleMedium,
                    color = EmotionTheme.colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(0xFFFFE45C),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Joy peaked on Saturday and lowest on Wednesday",
                        style = Typography.bodyMedium,
                        color = EmotionTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PatternsContent() {
    var selectedView by remember { mutableStateOf("By Emotions") }
    
    Column {
        // Toggle buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToggleButton(
                text = "By Emotions",
                isSelected = selectedView == "By Emotions",
                onClick = { selectedView = "By Emotions" },
                modifier = Modifier.weight(1f)
            )
            ToggleButton(
                text = "By Triggers",
                isSelected = selectedView == "By Triggers",
                onClick = { selectedView = "By Triggers" },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Emotion selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Joy", "Anger", "Stress", "Sadness")) { emotion ->
                val isSelected = emotion == "Joy"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) EmotionTheme.colors.textPrimary
                            else EmotionTheme.colors.backgroundSecondary
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = emotion,
                        color = if (isSelected) EmotionTheme.colors.background else EmotionTheme.colors.textSecondary,
                        style = Typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Bubble chart
        BubbleChart()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Most Common Joy triggers this week",
            style = Typography.bodyMedium,
            color = EmotionTheme.colors.textSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BubbleChart() {
    val triggers = listOf(
        TriggerData("Gym", 49, 200.dp),
        TriggerData("Family", 32, 150.dp),
        TriggerData("TV", 19, 100.dp)
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        triggers.forEachIndexed { index, trigger ->
            val offset = when (index) {
                0 -> Offset(-50f, -20f)
                1 -> Offset(80f, 30f)
                2 -> Offset(-30f, 80f)
                else -> Offset.Zero
            }
            
            BubbleItem(
                trigger = trigger,
                modifier = Modifier.offset(
                    x = offset.x.dp,
                    y = offset.y.dp
                )
            )
        }
    }
}

@Composable
private fun BubbleItem(
    trigger: TriggerData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(trigger.size)
            .border(
                2.dp,
                Color(0xFFFFE45C),
                CircleShape
            )
            .background(
                Color.Transparent,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(
                    id = when (trigger.name) {
                        "Gym" -> R.drawable.ic_settings // Replace with gym icon
                        "Family" -> R.drawable.ic_home // Replace with family icon
                        else -> R.drawable.ic_favorite_24px // Replace with TV icon
                    }
                ),
                contentDescription = trigger.name,
                tint = EmotionTheme.colors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = trigger.name,
                style = Typography.bodyMedium,
                color = EmotionTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${trigger.percentage}%",
                style = Typography.bodySmall,
                color = EmotionTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun InsightsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Emotional Coach Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = EmotionTheme.colors.backgroundSecondary
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Your Emotional Coach",
                            style = Typography.titleMedium,
                            color = EmotionTheme.colors.textPrimary
                        )
                        Text(
                            text = "Week",
                            style = Typography.bodyMedium,
                            color = EmotionTheme.colors.textSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_favorite_24px), // Replace with brain/AI icon
                            contentDescription = "AI Coach",
                            modifier = Modifier.size(48.dp),
                            tint = EmotionTheme.colors.textPrimary
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Hello Emma",
                                style = Typography.titleMedium,
                                color = EmotionTheme.colors.textPrimary
                            )
                            Text(
                                text = "Here are some insights I've gathered from your emotional patterns this week",
                                style = Typography.bodyMedium,
                                color = EmotionTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
        
        item {
            WeeklySummaryCard(title = "Weekly Summary")
        }
        
        item {
            InsightCard(
                title = "Exercise Pattern",
                description = "When you exercise before 10am your joy levels are higher than usual.",
                color = Color(0xFFFFE45C)
            )
        }
        
        item {
            InsightCard(
                title = "Stress Trigger",
                description = "Work meetings after 3pm trigger your stress.",
                color = Color(0xFFFFD980)
            )
        }
        
        item {
            WeeklySummaryCard(title = "Weekly Summary")
        }
    }
}

@Composable
private fun WeeklySummaryCard(title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = EmotionTheme.colors.interactive
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = Typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your emotional balance score increased by 7% compared to las week.",
                style = Typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = EmotionTheme.colors.backgroundSecondary
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = Typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = Typography.bodyMedium,
                color = EmotionTheme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun ToggleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(25.dp))
            .background(
                if (isSelected) EmotionTheme.colors.interactive
                else EmotionTheme.colors.backgroundSecondary
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = Typography.bodyMedium,
            color = if (isSelected) Color.White else EmotionTheme.colors.textSecondary
        )
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(EmotionTheme.colors.backgroundSecondary)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = Typography.bodyMedium,
                color = EmotionTheme.colors.textPrimary
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = EmotionTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Data classes
data class EmotionData(
    val name: String,
    val percentage: Int,
    val change: Int,
    val color: Color
)

data class TriggerData(
    val name: String,
    val percentage: Int,
    val size: Dp
)