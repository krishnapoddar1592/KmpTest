// File: ui/screens/WelcomeScreen.kt
package com.reflect.app.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflect.app.android.R
import com.reflect.app.android.ui.components.EmotionButton
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.android.ui.theme.Neutral

@Composable
fun WelcomeScreen(
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmotionTheme.colors.background)
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for illustration/logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 56.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Neutral)
            ) {
                // You would replace this with your actual app logo or illustration
                // For now, using a placeholder
                Image(
                    painter = painterResource(id = R.drawable.ic_favorite_24px),
                    contentDescription = "Welcome Illustration",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // Title and subtitle
            Text(
                text = "Every Emotion Matters",
                style = MaterialTheme.typography.displayMedium,
                color = EmotionTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "A journey to self-awareness starts here.",
                style = MaterialTheme.typography.bodyLarge,
                color = EmotionTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EmotionButton(
                    text = "Register",
                    onClick = onRegisterClick,
                    modifier = Modifier.weight(1f),
                    contentColor = if (EmotionTheme.currentTheme == com.reflect.app.android.ui.theme.EmotionAppTheme.COSMIC) EmotionTheme.colors.textPrimary else EmotionTheme.colors.textPrimary,
                )

                EmotionButton(
                    text = "Sign In",
                    onClick = onSignInClick,
                    modifier = Modifier.weight(1f),
                    contentColor = if (EmotionTheme.currentTheme == com.reflect.app.android.ui.theme.EmotionAppTheme.COSMIC) EmotionTheme.colors.textPrimary else EmotionTheme.colors.textPrimary,
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for the bottom navigation
        }

        // Bottom navigation - would be a separate component in a real app
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
