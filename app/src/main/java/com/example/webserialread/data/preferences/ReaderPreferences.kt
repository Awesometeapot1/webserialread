package com.example.webserialread.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reader_prefs")

class ReaderPreferences(context: Context) {

    private val store = context.dataStore

    private object Keys {
        val TEXT_SIZE  = floatPreferencesKey("text_size")
        val FONT       = stringPreferencesKey("font")
        val BACKGROUND = stringPreferencesKey("background")
    }

    val settings: Flow<ReaderSettings> = store.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            ReaderSettings(
                textSizeSp = prefs[Keys.TEXT_SIZE] ?: 18f,
                font       = ReaderFont.entries.find { it.name == prefs[Keys.FONT] }       ?: ReaderFont.SERIF,
                background = ReaderBackground.entries.find { it.name == prefs[Keys.BACKGROUND] } ?: ReaderBackground.WHITE
            )
        }

    suspend fun setTextSize(size: Float)         { store.edit { it[Keys.TEXT_SIZE]  = size      } }
    suspend fun setFont(font: ReaderFont)         { store.edit { it[Keys.FONT]       = font.name } }
    suspend fun setBackground(bg: ReaderBackground) { store.edit { it[Keys.BACKGROUND] = bg.name  } }
}
