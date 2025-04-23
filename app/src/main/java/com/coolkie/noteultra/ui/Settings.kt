package com.coolkie.noteultra.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.NoteUltraApp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.data.DarkTheme
import com.coolkie.noteultra.data.LlmMode
import com.coolkie.noteultra.data.LocalLlmConfig
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.data.NoteViewModelFactory
import com.coolkie.noteultra.data.NotesDatabase
import com.coolkie.noteultra.data.SettingsRepository
import com.coolkie.noteultra.data.dataStore
import com.coolkie.noteultra.ui.theme.NoteUltraTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class OptionType {
  RADIO,
  CHECKBOX
}

class SettingsActivity : ComponentActivity() {
  private val noteViewModel: NoteViewModel by viewModels {
    NoteViewModelFactory(NotesDatabase.getDatabase(applicationContext))
  }

  @SuppressLint("CoroutineCreationDuringComposition")
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val repository = SettingsRepository(dataStore)
    val app = application as NoteUltraApp
    val vectorUtils = app.vectorUtils

    enableEdgeToEdge()
    setContent {
      val recordingState by repository.recordingStateFlow.collectAsState(repository.recordingStateInitial())
      val recordingOnBoot by repository.recordingOnBootFlow.collectAsState(repository.recordingOnBootInitial())
      val llmMode by repository.llmModeFlow.collectAsState(repository.llmModeInitial())
      val darkTheme by repository.darkThemeFlow.collectAsState(repository.darkThemeInitial())

      NoteUltraTheme {
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
          topBar = {
            TopAppBar(
              modifier = Modifier
                .fillMaxWidth(),
              title = { Text(stringResource(R.string.settings_title)) },
              navigationIcon = {
                IconButton(
                  onClick = { finish() }
                ) {
                  Icon(
                    painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                    contentDescription = stringResource(R.string.settings_back_button)
                  )
                }
              }
            )
          }
        ) { innerPadding ->
          Column(
            modifier = Modifier
              .padding(innerPadding)
              .verticalScroll(rememberScrollState())
          ) {
            SettingCategory(stringResource(R.string.settings_category_service))

            SettingItem(
              title = stringResource(R.string.settings_item_recording_state_title),
              description = stringResource(R.string.settings_item_recrding_state_description),
              onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                  repository.setRecordingState(!recordingState)
                }
              },
              content = {
                Switch(
                  checked = recordingState,
                  onCheckedChange = null
                )
              }
            )

            AnimatedVisibility(recordingState) {
              SettingItem(
                title = stringResource(R.string.settings_item_recording_on_boot_title),
                description = stringResource(R.string.settings_item_recrding_on_boot_description),
                onClick = {
                  CoroutineScope(Dispatchers.IO).launch {
                    repository.setRecordingOnBoot(!recordingOnBoot)
                  }
                },
                content = {
                  Switch(
                    checked = recordingOnBoot,
                    onCheckedChange = null
                  )
                }
              )
            }

            SettingCategory(stringResource(R.string.settings_category_ai))

            SettingItem(
              title = stringResource(R.string.settings_item_llm_settings_title),
              description = stringResource(R.string.settings_item_llm_settings_description),
              dialogTitle = stringResource(R.string.settings_item_llm_settings_title)
            ) {
              val llmPath = remember { mutableStateOf(repository.localLlmConfigInitial().path) }
              val llmUrl = remember { mutableStateOf(repository.remoteLlmConfigInitial()) }

              LlmMode.entries.forEach { mode ->
                SettingItemOption(
                  type = OptionType.RADIO,
                  isChecked = llmMode == mode,
                  onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                      repository.setLlmMode(mode)
                    }
                  },
                  text = stringResource(mode.labelResId)
                ) { modifier ->
                  when (mode) {
                    LlmMode.LOCAL -> {
                      OutlinedTextField(
                        value = llmPath.value,
                        onValueChange = {
                          coroutineScope.launch {
                            repository.setLocalLlmConfig(
                              LocalLlmConfig(
                                path = it,
                                startTag = "<start_of_turn>",
                                endTag = "<end_of_turn>",
                                questionPrompt = "請試著用以下文本與USER交談，如果文本與USER無關請自行回答USER",
                                noteTitlePrompt = "請把USER說的句子簡化成標題，盡可能的簡短",
                                noteContentPrompt = "請把USER說的句子生成重點"
                              )
                            )
                          }

                          llmPath.value = it
                        },
                        enabled = llmMode == mode,
                        label = { Text("PATH") },
                        singleLine = true,
                        modifier = modifier
                      )
                    }

                    LlmMode.REMOTE -> {
                      OutlinedTextField(
                        value = llmUrl.value,
                        onValueChange = {
                          coroutineScope.launch {
                            repository.setRemoteLlmConfig(it)
                          }

                          llmUrl.value = it
                        },
                        enabled = llmMode == mode,
                        label = { Text("URL") },
                        singleLine = true,
                        modifier = modifier
                      )
                    }

                    LlmMode.DISABLE -> {}
                  }
                }
              }
            }

            SettingCategory(stringResource(R.string.settings_category_theme))

            SettingItem(
              title = stringResource(R.string.settings_item_dark_theme_title),
              description = stringResource(R.string.settings_item_dark_theme_description),
              dialogTitle = stringResource(R.string.settings_item_dark_theme_title)
            ) { closeDialog ->
              DarkTheme.entries.forEach { theme ->
                SettingItemOption(
                  type = OptionType.RADIO,
                  isChecked = darkTheme == theme,
                  onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                      repository.setDarkTheme(theme)
                    }

                    closeDialog()
                  },
                  text = stringResource(theme.labelResId)
                )
              }
            }

            SettingCategory(stringResource(R.string.settings_category_other))

            SettingItem(
              title = stringResource(R.string.settings_item_clear_data_title),
              description = stringResource(R.string.settings_item_clear_data_description),
              dialogTitle = stringResource(R.string.settings_item_clear_data_title),
            ) { closeDialog ->
              val isSettingsCheck = remember { mutableStateOf(false) }
              val isNotesCheck = remember { mutableStateOf(false) }
              val isTranscriptCheck = remember { mutableStateOf(false) }
              val isAnyCheck =
                isSettingsCheck.value || isNotesCheck.value || isTranscriptCheck.value

              SettingItemOption(
                type = OptionType.CHECKBOX,
                isChecked = isSettingsCheck.value,
                onClick = { isSettingsCheck.value = !isSettingsCheck.value },
                text = stringResource(R.string.settings_clear_data_setting)
              )
              SettingItemOption(
                type = OptionType.CHECKBOX,
                isChecked = isNotesCheck.value,
                onClick = { isNotesCheck.value = !isNotesCheck.value },
                text = stringResource(R.string.settings_clear_data_notes)
              )
              SettingItemOption(
                type = OptionType.CHECKBOX,
                isChecked = isTranscriptCheck.value,
                onClick = { isTranscriptCheck.value = !isTranscriptCheck.value },
                text = stringResource(R.string.settings_clear_data_transcript)
              )
              Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp, vertical = 8.dp)
              ) {
                TextButton(
                  onClick = { closeDialog() }
                ) {
                  Text(stringResource(R.string.settings_clear_data_close_button))
                }
                Button(
                  onClick = {
                    if (isSettingsCheck.value) {
                      CoroutineScope(Dispatchers.IO).launch {
                        repository.clearAll()
                      }
                    }
                    if (isNotesCheck.value) noteViewModel.deleteAllNotes()
                    if (isTranscriptCheck.value) vectorUtils.clearAll()

                    closeDialog()
                  },
                  enabled = isAnyCheck,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                  )
                ) {
                  Text(stringResource(R.string.settings_clear_data_delete_button))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SettingCategory(text: String) {
  Text(
    modifier = Modifier
      .padding(horizontal = 24.dp, vertical = 8.dp),
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.primary,
    text = text
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun SettingItem(
  title: String,
  description: String,
  crossinline onClick: () -> Unit = {},
  crossinline content: @Composable () -> Unit = {},
  dialogTitle: String? = null,
  crossinline dialogContent: @Composable (closeDialog: () -> Unit) -> Unit = {},
) {
  val isDialogDisplay = remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        isDialogDisplay.value = true
        onClick()
      }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge
        )
        Text(
          modifier = Modifier
            .padding(end = 8.dp),
          text = description,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium
        )
      }
      content()
    }
  }

  if (dialogTitle != null && isDialogDisplay.value) {
    BasicAlertDialog({
      isDialogDisplay.value = false
    }) {
      Surface(
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation
      ) {
        Column(
          horizontalAlignment = Alignment.Start,
          modifier = Modifier
            .padding(vertical = 8.dp)
        ) {
          Text(
            text = dialogTitle,
            modifier = Modifier
              .padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
          )
          dialogContent { isDialogDisplay.value = false }
        }
      }
    }
  }
}

@Composable
inline fun SettingItemOption(
  type: OptionType,
  isChecked: Boolean,
  crossinline onClick: () -> Unit,
  text: String,
  content: @Composable (modifier: Modifier) -> Unit = {}
) {
  Column(
    modifier = Modifier
      .clickable { onClick() }
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
    ) {
      when (type) {
        OptionType.RADIO -> {
          RadioButton(
            selected = isChecked,
            onClick = null,
            modifier = Modifier
              .padding(12.dp)
              .padding(start = 8.dp)
          )
        }

        OptionType.CHECKBOX -> {
          Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            modifier = Modifier
              .padding(12.dp)
              .padding(start = 8.dp)
          )
        }
      }
      Text(
        text = text,
        modifier = Modifier.padding(start = 8.dp)
      )
    }
    content(
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .padding(bottom = 8.dp)
    )
  }
}