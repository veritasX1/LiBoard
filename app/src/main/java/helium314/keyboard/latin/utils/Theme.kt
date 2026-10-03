// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helium314.keyboard.latin.R

@Composable
fun Theme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val material3 = Typography()
    // LiBoard: iOS system colours (Apple HIG) instead of Material You
    val colorScheme = if (dark) darkColorScheme(
        primary = Color(0xFF0A84FF),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF0A84FF),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFF0A84FF),
        secondaryContainer = Color(0xFF3A3A3C),
        onSecondaryContainer = Color.White,
        tertiary = Color(0xFF30D158),
        background = Color.Black,
        onBackground = Color.White,
        surface = Color.Black,
        onSurface = Color.White,
        onSurfaceVariant = Color(0xFF8E8E93),
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF1C1C1E),
        surfaceContainer = Color.Black,
        surfaceContainerHigh = Color(0xFF1C1C1E),
        surfaceContainerHighest = Color(0xFF2C2C2E),
        surfaceVariant = Color(0xFF2C2C2E),
        outline = Color(0xFF545458),
        outlineVariant = Color(0xFF38383A),
        error = Color(0xFFFF453A),
    ) else lightColorScheme(
        primary = Color(0xFF007AFF),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF007AFF),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFF007AFF),
        secondaryContainer = Color(0xFFE5E5EA),
        onSecondaryContainer = Color.Black,
        tertiary = Color(0xFF34C759),
        background = Color(0xFFF2F2F7),
        onBackground = Color.Black,
        surface = Color(0xFFF2F2F7),
        onSurface = Color.Black,
        onSurfaceVariant = Color(0xFF8A8A8E),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color.White,
        surfaceContainer = Color(0xFFF2F2F7),
        surfaceContainerHigh = Color.White,
        surfaceContainerHighest = Color(0xFFE5E5EA),
        surfaceVariant = Color(0xFFE5E5EA),
        outline = Color(0xFFC6C6C8),
        outlineVariant = Color(0xFFC6C6C8),
        error = Color(0xFFFF3B30),
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            // iOS text styles: Large Title 34, Title 3 20, Headline 17 semibold, Body 17, Footnote 13
            headlineMedium = material3.headlineMedium.copy(fontSize = 34.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold),
            titleLarge = material3.titleLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
            titleMedium = material3.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
            titleSmall = material3.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            bodyLarge = material3.bodyLarge.copy(fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
            bodyMedium = material3.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
            labelLarge = material3.labelLarge.copy(fontSize = 17.sp, letterSpacing = 0.sp),
        ),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(14.dp),
            extraLarge = RoundedCornerShape(14.dp),
        ),
        content = content
    )
}

const val previewDark = true
