package com.example.webserialread

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.webserialread.navigation.AppNavigation
import com.example.webserialread.ui.theme.WebserialreadTheme
import com.example.webserialread.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val settingsVm: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode   by settingsVm.themeMode.collectAsStateWithLifecycle()
            val accentColor by settingsVm.accentColor.collectAsStateWithLifecycle()

            WebserialreadTheme(themeMode = themeMode, accentColor = accentColor) {
                AppNavigation()
            }
        }
    }
}
