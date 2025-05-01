// File: ui/screens/BottomNavigationBar.kt
package com.reflect.app.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.reflect.app.android.R
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.android.ui.theme.Typography

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedRoute: String = "Home",
    onTabSelected: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EmotionTheme.colors.background)
    ) {
//        Spacer(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(1.dp)
//                .background(EmotionTheme.colors.textSecondary.copy(alpha = 0.1f))
//        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = R.drawable.ic_stats,
                label = "Stats",
                isSelected = selectedRoute == "Stats",
                onClick = { onTabSelected("Stats") }
            )

            NavItem(
                icon = R.drawable.ic_calendar,
                label = "Calendar",
                isSelected = selectedRoute == "Calendar",
                onClick = { onTabSelected("Calendar") }
            )

            NavItem(
                icon = R.drawable.ic_home,
                label = "Home",
                isSelected = selectedRoute == "Home",
                onClick = { onTabSelected("Home") }
            )

            NavItem(
                icon = R.drawable.ic_breathing,
                label = "Breathing",
                isSelected = selectedRoute == "Breathing",
                onClick = { onTabSelected("Breathing") }
            )

            NavItem(
                icon = R.drawable.ic_settings,
                label = "Settings",
                isSelected = selectedRoute == "Settings",
                onClick = { onTabSelected("Settings") }
            )
        }

        // Space for the bottom system navigation on some devices
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun NavItem(
    icon: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.height(20.dp),
            tint = if (isSelected) EmotionTheme.colors.interactive else EmotionTheme.colors.textSecondary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = Typography.bodySmall,
            color = if (isSelected) EmotionTheme.colors.interactive else EmotionTheme.colors.textSecondary,

        )
    }
}