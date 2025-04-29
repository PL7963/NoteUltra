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

enum class PagerState(@StringRes val labelResId: Int){
    NOTES(R.string.settings_pager_state_notes),
    TRANSCRIPT(R.string.settings_pager_state_transcript)
}

data class LocalLlmConfig(
    val path: String,
    val startTag: String,
    val endTag: String,
    val questionPrompt: String,
    val noteTitlePrompt: String,
    val noteContentPrompt: String
)

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val RECORDING_STATE = booleanPreferencesKey("recording_state")
        val LLM_MODE = stringPreferencesKey("llm_mode")
        val DARK_THEME = stringPreferencesKey("dark_theme")
        val INITIAL_PAGE = stringPreferencesKey("initial_page")

        object LocalLLM {
            val PATH = stringPreferencesKey("local_llm_path")
            val START_TAG = stringPreferencesKey("local_llm_start_tag")
            val END_TAG = stringPreferencesKey("local_llm_end_tag")
            val QUESTION_PROMPT = stringPreferencesKey("local_llm_question_prompt")
            val NOTE_TITLE_PROMPT = stringPreferencesKey("local_llm_note_title_prompt")
            val NOTE_CONTENT_PROMPT = stringPreferencesKey("local_llm_note_content_prompt")
        }

        object RemoteLLM {
            val URL = stringPreferencesKey("remote_llm_url")
        }
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

    private fun Preferences.toLlmMode(): LlmMode {
        return LlmMode.valueOf(this[PreferencesKeys.LLM_MODE] ?: LlmMode.DISABLE.name)
    }

    private fun Preferences.toDarkTheme(): DarkTheme {
        return DarkTheme.valueOf(this[PreferencesKeys.DARK_THEME] ?: DarkTheme.SYSTEM.name)
    }

    private fun Preferences.toInitialPage(): PagerState {
        return PagerState.valueOf(this[PreferencesKeys.INITIAL_PAGE] ?: PagerState.NOTES.name)
    }

    private fun Preferences.toLocalLlmConfig(): LocalLlmConfig {
        return LocalLlmConfig(
            path = this[PreferencesKeys.LocalLLM.PATH]
                ?: "/data/local/tmp/llm/model.bin",
            startTag = this[PreferencesKeys.LocalLLM.START_TAG]
                ?: "<start_of_turn>",
            endTag = this[PreferencesKeys.LocalLLM.END_TAG]
                ?: "<end_of_turn>",
            questionPrompt = this[PreferencesKeys.LocalLLM.QUESTION_PROMPT]
                ?: "請試著用以下文本與USER交談，如果文本與USER無關請自行回答USER",
            noteTitlePrompt = this[PreferencesKeys.LocalLLM.NOTE_TITLE_PROMPT]
                ?: "請把USER說的句子簡化成標題，盡可能的簡短",
            noteContentPrompt = this[PreferencesKeys.LocalLLM.NOTE_CONTENT_PROMPT]
                ?: "請把USER說的句子生成重點"
        )
    }

    private fun Preferences.toRemoteLolConfig(): String {
        return this[PreferencesKeys.RemoteLLM.URL] ?: ""
    }

    fun recordingStateInitial(): Boolean = runBlocking {
        dataStore.data.first().toRecordingState()
    }

    val recordingStateFlow: Flow<Boolean> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toRecordingState()
        }

    fun llmModeInitial(): LlmMode = runBlocking {
        dataStore.data.first().toLlmMode()
    }

    val llmModeFlow: Flow<LlmMode> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toLlmMode()
        }

    fun darkThemeInitial(): DarkTheme = runBlocking {
        dataStore.data.first().toDarkTheme()
    }

    val darkThemeFlow: Flow<DarkTheme> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toDarkTheme()
        }

    fun initialPageInitial(): PagerState = runBlocking {
        dataStore.data.first().toInitialPage()
    }

    val initialPageFlow: Flow<PagerState> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toInitialPage()
        }

    fun localLlmConfigInitial(): LocalLlmConfig = runBlocking {
        dataStore.data.first().toLocalLlmConfig()
    }

    val localLlmConfigFlow: Flow<LocalLlmConfig> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toLocalLlmConfig()
        }

    fun remoteLlmConfigInitial(): String = runBlocking {
        dataStore.data.first().toRemoteLolConfig()
    }

    val remoteLlmConfigFlow: Flow<String> = dataStore.data
        .catchIOException()
        .map { preferences ->
            preferences.toRemoteLolConfig()
        }


    suspend fun setRecordingState(state: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECORDING_STATE] = state
        }
    }

    suspend fun setLlmMode(mode: LlmMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LLM_MODE] = mode.name
        }
    }

    suspend fun setLocalLlmConfig(
        data: LocalLlmConfig
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LocalLLM.PATH] = data.path
            preferences[PreferencesKeys.LocalLLM.START_TAG] = data.startTag
            preferences[PreferencesKeys.LocalLLM.END_TAG] = data.endTag
            preferences[PreferencesKeys.LocalLLM.QUESTION_PROMPT] = data.questionPrompt
            preferences[PreferencesKeys.LocalLLM.NOTE_TITLE_PROMPT] = data.noteTitlePrompt
            preferences[PreferencesKeys.LocalLLM.NOTE_CONTENT_PROMPT] = data.noteContentPrompt
        }
    }

    suspend fun setRemoteLlmConfig(url: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.RemoteLLM.URL] = url
        }
    }

    suspend fun setDarkTheme(theme: DarkTheme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME] = theme.name
        }
    }

    suspend fun setInitialPage(page: PagerState) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.INITIAL_PAGE] = page.name
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}