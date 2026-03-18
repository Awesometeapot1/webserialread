package com.example.webserialread.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webserialread.data.preferences.ReaderBackground
import com.example.webserialread.data.preferences.ReaderFont
import com.example.webserialread.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Text Size
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Text Size",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("A", fontSize = 12.sp, modifier = Modifier.width(24.dp))
                    Slider(
                        value = settings.textSizeSp,
                        onValueChange = { vm.setTextSize(it) },
                        valueRange = 12f..28f,
                        steps = 7,
                        modifier = Modifier.weight(1f)
                    )
                    Text("A", fontSize = 22.sp, modifier = Modifier.width(32.dp))
                }
                Text(
                    "${settings.textSizeSp.toInt()}sp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            HorizontalDivider()

            // Font
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Font",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReaderFont.entries.forEach { font ->
                        val selected = settings.font == font
                        FilterChip(
                            selected = selected,
                            onClick = { vm.setFont(font) },
                            label = {
                                Text(
                                    font.label,
                                    fontFamily = when (font) {
                                        ReaderFont.SERIF -> FontFamily.Serif
                                        ReaderFont.SANS  -> FontFamily.SansSerif
                                        ReaderFont.MONO  -> FontFamily.Monospace
                                    }
                                )
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Background
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Background",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ReaderBackground.entries.forEach { bg ->
                        val selected = settings.background == bg
                        val bgColor = Color(android.graphics.Color.parseColor(bg.bgHex))
                        val textColor = Color(android.graphics.Color.parseColor(bg.textHex))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .then(
                                        if (selected) Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        ) else Modifier.border(1.dp, Color.Gray, CircleShape)
                                    )
                                    .clickable { vm.setBackground(bg) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A", color = textColor, fontSize = 18.sp)
                            }
                            Text(
                                bg.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Preview
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Preview",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                val bgColor = Color(android.graphics.Color.parseColor(settings.background.bgHex))
                val textColor = Color(android.graphics.Color.parseColor(settings.background.textHex))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "The quick brown fox jumps over the lazy dog. " +
                               "This is how your chapters will look in the reader.",
                        color = textColor,
                        fontSize = settings.textSizeSp.sp,
                        fontFamily = when (settings.font) {
                            ReaderFont.SERIF -> FontFamily.Serif
                            ReaderFont.SANS  -> FontFamily.SansSerif
                            ReaderFont.MONO  -> FontFamily.Monospace
                        },
                        lineHeight = (settings.textSizeSp * 1.6f).sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
