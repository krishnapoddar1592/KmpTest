// File: ui/components/CommonComponents.kt
package com.reflect.app.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reflect.app.android.R
import com.reflect.app.android.ui.theme.EmotionTheme
import com.reflect.app.android.ui.theme.Typography

@Composable
fun EmotionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmotionTheme.colors.interactive,
            unfocusedBorderColor = EmotionTheme.colors.textSecondary.copy(alpha = 0.5f),
            focusedTextColor = EmotionTheme.colors.textPrimary,
            unfocusedTextColor = EmotionTheme.colors.textPrimary,
            cursorColor = EmotionTheme.colors.interactive,
            focusedPlaceholderColor = EmotionTheme.colors.textSecondary,
            unfocusedPlaceholderColor = EmotionTheme.colors.textSecondary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction
        ),
        visualTransformation = when {
            isPassword && !passwordVisible -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {

                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_show else R.drawable.ic_hide
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = EmotionTheme.colors.textSecondary
                    )

                }
            }
        }
    )
}



@Composable
fun EmotionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) EmotionTheme.colors.interactive else EmotionTheme.colors.interactiveDisabled,
            contentColor = contentColor,
            disabledContainerColor = EmotionTheme.colors.interactiveDisabled,
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            style = Typography.bodyMedium
        )
    }
}
@Composable
fun EmotionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) EmotionTheme.colors.interactive else EmotionTheme.colors.interactiveDisabled,
            contentColor = contentColor,
            disabledContainerColor = EmotionTheme.colors.interactiveDisabled,
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Show loading indicator when isLoading is true
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            }

            // Show text only when not loading
            Text(
                text = text,
                style = Typography.bodyMedium,
                modifier = Modifier.alpha(if (isLoading) 0f else 1f)
            )
        }
    }
}

@Composable
fun EmotionDividerWithText(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = EmotionTheme.colors.textSecondary.copy(alpha = 0.3f)
        )
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = EmotionTheme.colors.textSecondary
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = EmotionTheme.colors.textSecondary.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun SocialButton(
    iconResId: Int,
    onClick: () -> Unit,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = EmotionTheme.colors.textPrimary
        )
    }
}

@Composable
fun CheckedItem(
    text: String,
    isChecked: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom checkbox/check mark that matches the design
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isChecked) EmotionTheme.colors.interactive else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
//                    painter = painterResource(id = R.drawable.ic_check),
                    painter = painterResource(id = R.drawable.ic_favorite_24px),
                    contentDescription = "Checked",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = EmotionTheme.colors.textPrimary
        )
    }
}

@Composable
fun SelectablePlanOption(
    title: String,
    price: String,
    period: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (isSelected) EmotionTheme.colors.backgroundSecondary
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = EmotionTheme.colors.interactive,
                    unselectedColor = EmotionTheme.colors.textSecondary
                )
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = EmotionTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$price /$period",
                style = MaterialTheme.typography.titleMedium,
                color = EmotionTheme.colors.textPrimary,
                textAlign = TextAlign.End
            )
        }
    }
}