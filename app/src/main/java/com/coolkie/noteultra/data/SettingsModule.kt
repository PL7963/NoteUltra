package com.coolkie.noteultra.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsKeys {
    val LLM_MODEL = stringPreferencesKey("models")
    val THEME = intPreferencesKey("theme")
}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val llmModels: Flow<String> = dataStore.data.map { preferences ->
        preferences[SettingsKeys.LLM_MODEL] ?: "local_model"
    }
    val theme: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SettingsKeys.THEME] ?: 0
    }

    suspend fun setLlmModel(value: String) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.LLM_MODEL] = value
        }
    }

    suspend fun setTheme(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.THEME] = value
        }
    }
}