package com.example.webserialread.ui.screen

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

val coverPalette = listOf(
    Color(0xFF7C4DFF), Color(0xFF00ACC1), Color(0xFFE53935),
    Color(0xFF43A047), Color(0xFFFFB300), Color(0xFF1E88E5),
    Color(0xFF8E24AA), Color(0xFF00897B), Color(0xFF6D4C41),
    Color(0xFF546E7A), Color(0xFFD81B60), Color(0xFF3949AB)
)

fun coverColor(title: String): Color =
    coverPalette[title.hashCode().absoluteValue % coverPalette.size]
