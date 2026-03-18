package com.example.webserialread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webserialread.WebSerialApplication
import com.example.webserialread.data.preferences.NavPosition
import com.example.webserialread.data.preferences.ReaderBackground
import com.example.webserialread.data.preferences.ReaderFont
import com.example.webserialread.data.preferences.ReaderSettings
import com.example.webserialread.ui.theme.AccentColor
import com.example.webserialread.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = (app as WebSerialApplication).readerPreferences

    val themeMode: StateFlow<ThemeMode> = prefs.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM
    )

    val accentColor: StateFlow<AccentColor> = prefs.accentColor.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccentColor.DYNAMIC
    )

    val settings: StateFlow<ReaderSettings> = prefs.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReaderSettings()
    )

    fun setTextSize(size: Float)            = viewModelScope.launch { prefs.setTextSize(size) }
    fun setFont(font: ReaderFont)           = viewModelScope.launch { prefs.setFont(font) }
    fun setBackground(bg: ReaderBackground) = viewModelScope.launch { prefs.setBackground(bg) }
    fun setNavPosition(pos: NavPosition)    = viewModelScope.launch { prefs.setNavPosition(pos) }
    fun setThemeMode(mode: ThemeMode)      = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setAccentColor(color: AccentColor) = viewModelScope.launch { prefs.setAccentColor(color) }
}
