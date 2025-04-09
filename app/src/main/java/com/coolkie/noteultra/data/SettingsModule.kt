package com.coolkie.noteultra.data

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.coolkie.noteultra.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class LlmMode(@StringRes val labelResId: Int) {
    LOCAL(R.string.settings_llm_source_local),
    REMOTE(R.string.settings_llm_source_remote),
    DISABLE(R.string.settings_llm_source_disable)
}

enum class DarkTheme(@StringRes val labelResId: Int) {
    ENABLE(R.string.settings_dark_theme_enable),
    DISABLE(R.string.settings_dark_theme_disable),
    SYSTEM(R.string.settings_dark_theme_system)
}

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val RECORDING_STATE = booleanPreferencesKey("recording_state")
        val RECORDING_ON_BOOT = booleanPreferencesKey("recording_on_boot")
        val LLM_MODE = stringPreferencesKey("llm_mode")
        val LLM_PATH = stringPreferencesKey("llm_path")
        val LLM_URL = stringPreferencesKey("llm_url")
        val DARK_THEME = stringPreferencesKey("dark_theme")
    }

    private fun Flow<Preferences>.catchIOException(): Flow<Preferences> {
        return this.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
    }

    private fun Preferences.toRecordingState(): Boolean {
        return this[PreferencesKeys.RECORDING_STATE] ?: true
    }

    private fun Preferences.toRecordingOnBoot(): Boolean {
        return this[PreferencesKeys.RECORDING_ON_BOOT] ?: true
    }

    private fun Preferences.toLlmMode(): LlmMode {
        return LlmMode.valueOf(this[PreferencesKeys.LLM_MODE] ?: LlmMode.DISABLE.name)
    }

    private fun Preferences.toLlmPath(): String {
        return this[PreferencesKeys.LLM_PATH] ?: "/data/local/tmp/llm/model.bin"
    }

    private fun Preferences.toLlmUrl(): String {
        return this[PreferencesKeys.LLM_URL] ?: "http://"
    }

    private fun Preferences.toDarkTheme(): DarkTheme {
        return DarkTheme.valueOf(this[PreferencesKeys.DARK_THEME] ?: DarkTheme.SYSTEM.name)
    }


    fun recordingStateInitial(): Boolean = runBlocking {
        dataStore.data.first().toRecordingState()
    }

    val recordingStateFlow: Flow<Boolean> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toRecordingState()
        }

    fun recordingOnBootInitial(): Boolean = runBlocking {
        dataStore.data.first().toRecordingOnBoot()
    }

    val recordingOnBootFlow: Flow<Boolean> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toRecordingOnBoot()
        }

    fun llmModeInitial(): LlmMode = runBlocking {
        dataStore.data.first().toLlmMode()
    }

    val llmModeFlow: Flow<LlmMode> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toLlmMode()
        }

    fun llmPathInitial(): String = runBlocking {
        dataStore.data.first().toLlmPath()
    }

    val llmPathFlow: Flow<String> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toLlmPath()
        }

    fun llmUrlInitial(): String = runBlocking {
        dataStore.data.first().toLlmUrl()
    }

    val llmUrlFlow: Flow<String> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toLlmUrl()
        }

    fun darkThemeInitial(): DarkTheme = runBlocking {
        dataStore.data.first().toDarkTheme()
    }

    val darkThemeFlow: Flow<DarkTheme> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toDarkTheme()
        }


    suspend fun setRecordingState(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECORDING_STATE] = state
        }
    }

    suspend fun setRecordingOnBoot(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECORDING_ON_BOOT] = state
        }
    }

    suspend fun setLlmMode(mode: LlmMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LLM_MODE] = mode.name
        }
    }

    suspend fun setLlmPath(path: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LLM_PATH] = path
        }
    }

    suspend fun setLlmUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LLM_URL] = url
        }
    }

    suspend fun setDarkTheme(theme: DarkTheme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME] = theme.name
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}