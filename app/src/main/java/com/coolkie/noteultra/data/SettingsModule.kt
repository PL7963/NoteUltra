package com.coolkie.noteultra.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class LlmSource {
    LOCAL,
    REMOTE,
    DISABLED
}

enum class DarkMode {
    ENABLED,
    DISABLED,
    SYSTEM
}

data class LlmPreferences(
    val llmSource: LlmSource,
    val llmPath: String
)

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val LLM_SOURCE = stringPreferencesKey("llm_source")
        val LLM_PATH = stringPreferencesKey("llm_path")
        val DARK_MODE = stringPreferencesKey("dark_mode")
    }

    val llmSourceFlow: Flow<LlmPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            LlmPreferences(
                LlmSource.valueOf(preferences[PreferencesKeys.LLM_SOURCE] ?: LlmSource.LOCAL.name),
                preferences[PreferencesKeys.LLM_PATH] ?: ""
            )
        }

    val darkModeFlow: Flow<DarkMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            DarkMode.valueOf(preferences[PreferencesKeys.DARK_MODE] ?: DarkMode.SYSTEM.name)
        }

    suspend fun llmPreferences(preferences: LlmPreferences) {
        dataStore.edit { preferencesMap ->
            preferencesMap[PreferencesKeys.LLM_SOURCE] = preferences.llmSource.name
            preferencesMap[PreferencesKeys.LLM_PATH] = preferences.llmPath
        }
    }

    suspend fun darkMode(mode: DarkMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = mode.name
        }
    }
}