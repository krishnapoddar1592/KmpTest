package com.reflect.app.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.reflect.app.android.R

// Create FontFamilies for Cabinet Grotesk and General Sans
// Note: You would need to add these fonts to your resources
val CabinetGrotesk = FontFamily(
    Font(R.font.cabinet_grotesk_bold, FontWeight.Bold)
    // Add more weights as needed
)

val GeneralSans = FontFamily(
    Font(R.font.cabinet_grotesk_bold, FontWeight.SemiBold),
    // Add more weights as needed
)

// Typography system based on the Figma design
val Typography = Typography(
    // Bold Heading - Cabinet Grotesk Bold 72sp
    displayLarge = TextStyle(
        fontFamily = CabinetGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp,
        lineHeight = 97.sp, // 135.14% of 72sp
    ),

    // Heading1 - Cabinet Grotesk Bold 48sp
    displayMedium = TextStyle(
        fontFamily = CabinetGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 64.sp, // 135.14% of 48sp
    ),

    // H1 - General Sans Semibold 32sp
    headlineLarge = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),

    // Semibold14 - General Sans Semibold 14sp
    bodyMedium = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),

    // Use this for regular body text
    bodyLarge = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),

    // Title for buttons, labels, etc.
    titleMedium = TextStyle(
        fontFamily = GeneralSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = CabinetGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 24.sp,
    ),
)