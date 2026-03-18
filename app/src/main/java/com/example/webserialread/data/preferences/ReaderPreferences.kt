package com.example.webserialread.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.webserialread.ui.theme.AccentColor
import com.example.webserialread.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reader_prefs")

class ReaderPreferences(context: Context) {

    private val store = context.dataStore

    private object Keys {
        val TEXT_SIZE    = floatPreferencesKey("text_size")
        val FONT         = stringPreferencesKey("font")
        val BACKGROUND   = stringPreferencesKey("background")
        val NAV_POSITION = stringPreferencesKey("nav_position")
        val THEME_MODE   = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
    }

    val settings: Flow<ReaderSettings> = store.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            ReaderSettings(
                textSizeSp  = prefs[Keys.TEXT_SIZE] ?: 18f,
                font        = ReaderFont.entries.find { it.name == prefs[Keys.FONT] }           ?: ReaderFont.SERIF,
                background  = ReaderBackground.entries.find { it.name == prefs[Keys.BACKGROUND] } ?: ReaderBackground.WHITE,
                navPosition = NavPosition.entries.find { it.name == prefs[Keys.NAV_POSITION] }  ?: NavPosition.BOTTOM,
            )
        }

    val themeMode: Flow<ThemeMode> = store.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> ThemeMode.entries.find { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.SYSTEM }

    val accentColor: Flow<AccentColor> = store.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> AccentColor.entries.find { it.name == prefs[Keys.ACCENT_COLOR] } ?: AccentColor.DYNAMIC }

    suspend fun setTextSize(size: Float)              { store.edit { it[Keys.TEXT_SIZE]    = size        } }
    suspend fun setFont(font: ReaderFont)              { store.edit { it[Keys.FONT]         = font.name   } }
    suspend fun setBackground(bg: ReaderBackground)   { store.edit { it[Keys.BACKGROUND]   = bg.name     } }
    suspend fun setNavPosition(pos: NavPosition)      { store.edit { it[Keys.NAV_POSITION] = pos.name    } }
    suspend fun setThemeMode(mode: ThemeMode)         { store.edit { it[Keys.THEME_MODE]   = mode.name   } }
    suspend fun setAccentColor(color: AccentColor)    { store.edit { it[Keys.ACCENT_COLOR] = color.name  } }
}
