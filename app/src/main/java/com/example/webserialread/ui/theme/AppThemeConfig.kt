package com.example.webserialread.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String) {
    SYSTEM("Follow system"),
    LIGHT("Light"),
    DARK("Dark"),
}

enum class AccentColor(val label: String) {
    DYNAMIC("Dynamic"),   // Android 12+ wallpaper-based
    PURPLE("Purple"),
    TEAL("Teal"),
    AMBER("Amber"),
    ROSE("Rose"),
    FOREST("Forest"),
    NAVY("Navy"),
}

// ── Light schemes ─────────────────────────────────────────────────────────────

val PurpleLightScheme = lightColorScheme(
    primary         = Color(0xFF6650A4),
    onPrimary       = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary       = Color(0xFF625B71),
    tertiary        = Color(0xFF7D5260),
)

val TealLightScheme = lightColorScheme(
    primary         = Color(0xFF006A60),
    onPrimary       = Color.White,
    primaryContainer = Color(0xFF9DF2E4),
    onPrimaryContainer = Color(0xFF00201C),
    secondary       = Color(0xFF4A6360),
    tertiary        = Color(0xFF4E6479),
)

val AmberLightScheme = lightColorScheme(
    primary         = Color(0xFF7B5200),
    onPrimary       = Color.White,
    primaryContainer = Color(0xFFFFDDB3),
    onPrimaryContainer = Color(0xFF271900),
    secondary       = Color(0xFF6C5E3F),
    tertiary        = Color(0xFF4E6645),
)

val RoseLightScheme = lightColorScheme(
    primary         = Color(0xFF9C3B6B),
    onPrimary       = Color.White,
    primaryContainer = Color(0xFFFFD8E7),
    onPrimaryContainer = Color(0xFF3C0027),
    secondary       = Color(0xFF74565F),
    tertiary        = Color(0xFF7C5635),
)

val ForestLightScheme = lightColorScheme(
    primary         = Color(0xFF326B25),
    onPrimary       = Color.White,
    primaryContainer = Color(0xFFB2F594),
    onPrimaryContainer = Color(0xFF002201),
    secondary       = Color(0xFF52634C),
    tertiary        = Color(0xFF39656B),
)

val NavyLightScheme = lightColorScheme(
    primary         = Color(0xFF0D5EA0),
    onPrimary       = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary       = Color(0xFF535F70),
    tertiary        = Color(0xFF6B5778),
)

// ── Dark schemes ──────────────────────────────────────────────────────────────

val PurpleDarkScheme = darkColorScheme(
    primary         = Color(0xFFD0BCFF),
    onPrimary       = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary       = Color(0xFFCCC2DC),
    tertiary        = Color(0xFFEFB8C8),
)

val TealDarkScheme = darkColorScheme(
    primary         = Color(0xFF81D5C9),
    onPrimary       = Color(0xFF00201C),
    primaryContainer = Color(0xFF004E47),
    onPrimaryContainer = Color(0xFF9DF2E4),
    secondary       = Color(0xFFB0CCCA),
    tertiary        = Color(0xFFABC8DC),
)

val AmberDarkScheme = darkColorScheme(
    primary         = Color(0xFFFDB96B),
    onPrimary       = Color(0xFF411E00),
    primaryContainer = Color(0xFF5C3A00),
    onPrimaryContainer = Color(0xFFFFDDB3),
    secondary       = Color(0xFFD8C3A0),
    tertiary        = Color(0xFFB5CCAA),
)

val RoseDarkScheme = darkColorScheme(
    primary         = Color(0xFFFFB0CD),
    onPrimary       = Color(0xFF5C103E),
    primaryContainer = Color(0xFF792F58),
    onPrimaryContainer = Color(0xFFFFD8E7),
    secondary       = Color(0xFFE4BAC4),
    tertiary        = Color(0xFFEFBD96),
)

val ForestDarkScheme = darkColorScheme(
    primary         = Color(0xFF98D97C),
    onPrimary       = Color(0xFF0A3900),
    primaryContainer = Color(0xFF1C5111),
    onPrimaryContainer = Color(0xFFB2F594),
    secondary       = Color(0xFFB7CCB0),
    tertiary        = Color(0xFFA0CFD4),
)

val NavyDarkScheme = darkColorScheme(
    primary         = Color(0xFF9ECAFF),
    onPrimary       = Color(0xFF003060),
    primaryContainer = Color(0xFF064880),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary       = Color(0xFFBAC8DA),
    tertiary        = Color(0xFFCDBEDB),
)
