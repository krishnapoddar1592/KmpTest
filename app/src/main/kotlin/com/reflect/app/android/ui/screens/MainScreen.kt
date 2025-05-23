// File: app/src/main/kotlin/com/reflect/app/android/ui/screens/MainScreen.kt
package com.reflect.app.android.ui.screens

import android.bluetooth.BluetoothClass.Device
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflect.app.android.ui.components.FeatureCard
import com.reflect.app.android.ui.screens.BottomNavigationBar
import com.reflect.app.android.ui.theme.EmotionAppTheme
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.android.ui.theme.EmotionTheme.currentTheme
import com.reflect.app.android.ui.theme.ThemeManager
import com.reflect.app.android.ui.theme.Typography
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflect.app.android.R
import com.reflect.app.ml.viewmodel.EmotionDetectionViewModel
import com.reflect.app.viewmodels.CalendarViewModel
import com.reflect.app.viewmodels.EnhancedEmotionDetectionViewModel
import kotlinx.datetime.LocalDate
import androidx.compose.runtime.DisposableEffect

@Composable
fun MainScreen(
    onEmotionDetectionClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onSignOut: () -> Unit,
    onToggleTheme: () -> Unit,
    currentTheme: EmotionAppTheme,
    emotionDetectionViewModel: EnhancedEmotionDetectionViewModel,
    calendarViewModel: CalendarViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Home") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var previousTab by remember { mutableStateOf("Home") }

    // Load mock data for calendar (remove in production)
    LaunchedEffect(Unit) {
        calendarViewModel.loadFullMockData()
    }

    // Handle ViewModel lifecycle based on tab changes
    LaunchedEffect(selectedTab) {
        when {
            // Coming TO Home tab from another tab
            selectedTab == "Home" && previousTab != "Home" -> {
                // Prepare for resuming (don't close detector)
                emotionDetectionViewModel.prepareForPause()
            }
            // Leaving FROM Home tab to another tab
            selectedTab != "Home" && previousTab == "Home" -> {
                // Prepare for pausing (don't close detector, just pause)
                emotionDetectionViewModel.prepareForPause()
            }
        }
        previousTab = selectedTab
    }

    // Handle final cleanup when leaving the screen entirely
    DisposableEffect(Unit) {
        onDispose {
            // Only close detector if we're actually leaving the app/screen permanently
            if (selectedTab == "Home") {
                emotionDetectionViewModel.prepareForDestroy()
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    // Reset selected date when changing tabs
                    if (it != "Calendar") {
                        selectedDate = null
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                selectedDate != null -> {
                    // Show day insights screen
                    val entries by calendarViewModel.emotionEntries.collectAsState()
                    val dayEntries = entries[selectedDate] ?: emptyList()

                    DayInsightsScreen(
                        date = selectedDate!!,
                        entries = dayEntries,
                        onNavigateBack = { selectedDate = null }
                    )
                }
                selectedTab == "Home" -> {
                    EnhancedEmotionDetectionScreen(
                        viewModel = emotionDetectionViewModel,
                        onNavigateBack = onNavigateBack
                    )
                }
                selectedTab == "Calendar" -> {
                    val dayEmotionSummaries by calendarViewModel.dayEmotionSummaries.collectAsState()

                    CalendarScreen(
                        onDayClick = { date -> selectedDate = date },
                        emotionData = dayEmotionSummaries,
                        emotionEntries = calendarViewModel.emotionEntries.value
                        // Needed for weekly view details
                    )
                }
                selectedTab == "Stats" -> EmotionStatsScreen()
                selectedTab == "Breathing" -> BreathingContent()
                selectedTab == "Settings" -> SettingsScreen(
                    currentTheme = currentTheme,
                    onToggleTheme = onToggleTheme,
                    onSignOut = onSignOut
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    onEmotionDetectionClick: () -> Unit,
    onPremiumClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Every Emotion Matters",
            style = Typography.displayMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            color = EmotionTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "A journey to self-awareness starts here.",
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Feature cards
        FeatureCard(
            title = "Detect Emotion",
            description = "Use your camera to detect your current emotion",
            iconResId = android.R.drawable.ic_menu_camera, // Replace with your icon
            onClick = onEmotionDetectionClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            title = "Premium Features",
            description = "Unlock advanced emotion tracking and insights",
            iconResId = android.R.drawable.ic_menu_compass, // Replace with your icon
            onClick = onPremiumClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            title = "Breathing Exercises",
            description = "Guided breathing exercises to help manage emotions",
            iconResId = android.R.drawable.ic_menu_recent_history, // Replace with your icon
            onClick = { /* Navigate to breathing exercises */ }
        )
    }
}

@Composable
fun CalendarContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emotion Calendar",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Text(
            text = "Your emotion history will be displayed here",
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BreathingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Breathing Exercises",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Text(
            text = "Guided breathing exercises will appear here",
            style = Typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsScreen(
    currentTheme: EmotionAppTheme,
    onToggleTheme: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = EmotionTheme.colors.textPrimary,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Theme settings section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = EmotionTheme.colors.backgroundSecondary.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmotionTheme.colors.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        color = EmotionTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onToggleTheme,
                        colors = ButtonDefaults.buttonColors(

                            containerColor = EmotionTheme.colors.backgroundSecondary
                        ),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = if (currentTheme == EmotionAppTheme.COSMIC)
                                    R.drawable.ic_cosmic_theme
                                else
                                    R.drawable.ic_serene_theme),
                                contentDescription = "Theme icon",
                                tint = EmotionTheme.colors.interactive
                            )

                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                            Text(
                                text = if (currentTheme == EmotionAppTheme.COSMIC) "Cosmic" else "Serene",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmotionTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sign out button (in red)
        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}