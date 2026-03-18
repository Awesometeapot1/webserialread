package com.example.webserialread.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun WebserialreadTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.DYNAMIC,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme: ColorScheme = when {
        accentColor == AccentColor.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (isDark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        isDark -> when (accentColor) {
            AccentColor.DYNAMIC -> PurpleDarkScheme
            AccentColor.PURPLE  -> PurpleDarkScheme
            AccentColor.TEAL    -> TealDarkScheme
            AccentColor.AMBER   -> AmberDarkScheme
            AccentColor.ROSE    -> RoseDarkScheme
            AccentColor.FOREST  -> ForestDarkScheme
            AccentColor.NAVY    -> NavyDarkScheme
        }
        else -> when (accentColor) {
            AccentColor.DYNAMIC -> PurpleLightScheme
            AccentColor.PURPLE  -> PurpleLightScheme
            AccentColor.TEAL    -> TealLightScheme
            AccentColor.AMBER   -> AmberLightScheme
            AccentColor.ROSE    -> RoseLightScheme
            AccentColor.FOREST  -> ForestLightScheme
            AccentColor.NAVY    -> NavyLightScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
