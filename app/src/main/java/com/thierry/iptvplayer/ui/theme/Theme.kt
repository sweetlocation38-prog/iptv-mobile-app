package com.thierry.iptvplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Palette "industriel contemporain" : charbon, béton, acier, une touche ambre.
object IndustrialColors {
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1C1C1C)
    val SurfaceVariant = Color(0xFF2A2A2A)
    val Steel = Color(0xFF7C8592)
    val Accent = Color(0xFFE8590C) // ambre/orange industriel
    val TextPrimary = Color(0xFFEDEDED)
    val TextSecondary = Color(0xFF9C9C9C)
}

private val IndustrialDarkScheme = darkColorScheme(
    background = IndustrialColors.Background,
    surface = IndustrialColors.Surface,
    surfaceVariant = IndustrialColors.SurfaceVariant,
    primary = IndustrialColors.Accent,
    secondary = IndustrialColors.Steel,
    onBackground = IndustrialColors.TextPrimary,
    onSurface = IndustrialColors.TextPrimary,
    onPrimary = Color.Black
)

val LabelStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 1.2.sp
)

@Composable
fun IPTVPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IndustrialDarkScheme,
        content = content
    )
}
