package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BentoPurplePrimary,
    onPrimary = BentoPurpleOnPrimary,
    primaryContainer = BentoDarkPurpleContainer,
    onPrimaryContainer = BentoPurpleContainer,
    secondary = BentoPinkContainer,
    onSecondary = BentoOnPinkContainer,
    secondaryContainer = BentoDarkPinkContainer,
    onSecondaryContainer = BentoPinkContainer,
    tertiary = BentoBlueContainer,
    background = BentoDarkBackground,
    onBackground = Color(0xFFE6E0E9),
    surface = BentoDarkSurface,
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = BentoDarkCardBg,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPurplePrimary,
    onPrimary = BentoPurpleOnPrimary,
    primaryContainer = BentoPurpleContainer,
    onPrimaryContainer = BentoOnPurpleContainer,
    secondary = BentoPinkContainer,
    onSecondary = BentoOnPinkContainer,
    secondaryContainer = BentoBlueContainer,
    onSecondaryContainer = BentoOnBlueContainer,
    tertiary = BentoGreenContainer,
    background = BentoBackground,
    onBackground = BentoTextPrimary,
    surface = BentoSurface,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoCardBg,
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoBorder
)

@Composable
fun AnimeTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> darkTheme
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) DarkColorScheme else LightColorScheme
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
