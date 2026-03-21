package com.palabradeldia.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.palabradeldia.domain.model.ThemeMode

// Static palettes used on Android < 12 (no Dynamic Color).
private val LightColors = lightColorScheme(
    primary          = Color(0xFF1A56A0),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD4E4FF),
    secondary        = Color(0xFF5B6B87),
    background       = Color(0xFFF8F9FF),
    surface          = Color(0xFFF8F9FF),
    surfaceVariant   = Color(0xFFE2E6F0),
    onBackground     = Color(0xFF1A1C21),
    onSurface        = Color(0xFF1A1C21),
)

private val DarkColors = darkColorScheme(
    primary          = Color(0xFFA3C4F5),
    onPrimary        = Color(0xFF00305E),
    primaryContainer = Color(0xFF1A4782),
    secondary        = Color(0xFFB4C4DC),
    background       = Color(0xFF111318),
    surface          = Color(0xFF111318),
    surfaceVariant   = Color(0xFF2A2D35),
    onBackground     = Color(0xFFE2E4EC),
    onSurface        = Color(0xFFE2E4EC),
)

private val OledColors = DarkColors.copy(
    background     = Color.Black,
    surface        = Color.Black,
    surfaceVariant = Color(0xFF161616),
)

val PalabraTypography = Typography(
    displayMedium = TextStyle(
        fontWeight    = FontWeight.Light,
        fontSize      = 42.sp,
        lineHeight    = 52.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 40.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

@Composable
fun PalabraTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK,
        ThemeMode.OLED   -> true
    }
    val isOled = themeMode == ThemeMode.OLED

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            val dynamic = if (isDark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
            if (isOled) dynamic.copy(
                background     = Color.Black,
                surface        = Color.Black,
                surfaceVariant = Color(0xFF161616)
            ) else dynamic
        }
        isOled -> OledColors
        isDark -> DarkColors
        else   -> LightColors
    }

    MaterialTheme(colorScheme = colorScheme, typography = PalabraTypography, content = content)
}
