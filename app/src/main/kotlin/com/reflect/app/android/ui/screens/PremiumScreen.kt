// File: ui/screens/PremiumScreen.kt
package com.reflect.app.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflect.app.android.ui.components.CheckedItem
import com.reflect.app.android.ui.components.EmotionButton
import com.reflect.app.android.ui.components.SelectablePlanOption
import com.reflect.app.android.ui.theme.EmotionTheme

@Composable
fun PremiumScreen(
    onCloseClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf("yearly") } // yearly or monthly
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmotionTheme.colors.background)
    ) {
        // Close button
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = EmotionTheme.colors.textPrimary
            )
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Title
            Text(
                text = "Unlock Premium",
                style = MaterialTheme.typography.displayMedium,
                color = EmotionTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Enjoy these benefits when you upgrade to the premium plan",
                style = MaterialTheme.typography.bodyLarge,
                color = EmotionTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Benefits
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CheckedItem(text = "Offline access")
                CheckedItem(text = "No annoying ads")
                CheckedItem(text = "Unlimited content access")
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Free trial notice
            Text(
                text = "Enable 7-day free trial",
                style = MaterialTheme.typography.titleMedium,
                color = EmotionTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Subscription plans
            SelectablePlanOption(
                title = "Yearly",
                price = "$5.78",
                period = "year",
                isSelected = selectedPlan == "yearly",
                onClick = { selectedPlan = "yearly" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SelectablePlanOption(
                title = "Monthly",
                price = "$599.8",
                period = "month",
                isSelected = selectedPlan == "monthly",
                onClick = { selectedPlan = "monthly" }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Continue button
            EmotionButton(
                text = "Continue",
                onClick = onContinueClick,
                modifier = Modifier.fillMaxWidth()
            )

            // Bottom space for navigation
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom navigation
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
