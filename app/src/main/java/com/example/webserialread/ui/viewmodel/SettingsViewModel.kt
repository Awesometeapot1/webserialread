package com.example.webserialread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webserialread.WebSerialApplication
import com.example.webserialread.data.preferences.ReaderBackground
import com.example.webserialread.data.preferences.ReaderFont
import com.example.webserialread.data.preferences.ReaderSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = (app as WebSerialApplication).readerPreferences

    val settings: StateFlow<ReaderSettings> = prefs.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReaderSettings()
    )

    fun setTextSize(size: Float) = viewModelScope.launch { prefs.setTextSize(size) }
    fun setFont(font: ReaderFont) = viewModelScope.launch { prefs.setFont(font) }
    fun setBackground(bg: ReaderBackground) = viewModelScope.launch { prefs.setBackground(bg) }
}
