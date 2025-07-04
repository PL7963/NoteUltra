package com.coolkie.noteultra.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.coolkie.noteultra.NoteUltraApp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.data.DarkTheme
import com.coolkie.noteultra.data.LlmMode
import com.coolkie.noteultra.data.LocalLlmConfig
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.data.NoteViewModelFactory
import com.coolkie.noteultra.data.NotesDatabase
import com.coolkie.noteultra.data.PagerState
import com.coolkie.noteultra.data.SettingsRepository
import com.coolkie.noteultra.data.dataStore
import com.coolkie.noteultra.service.ForegroundRecordingService
import com.coolkie.noteultra.service.initiateRecording
import com.coolkie.noteultra.ui.components.FullScreenDialog
import com.coolkie.noteultra.ui.components.Option
import com.coolkie.noteultra.ui.components.OptionWithMoreButton
import com.coolkie.noteultra.ui.components.SettingCategory
import com.coolkie.noteultra.ui.components.SettingItem
import com.coolkie.noteultra.ui.components.SettingItemWithDialog
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
  private lateinit var repository: SettingsRepository
  private val requestPermissionsLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
      if (permissions.all { it.value }) {
        startService(Intent(this, ForegroundRecordingService::class.java))
      } else {
        Toast.makeText(
          this,
          getString(R.string.service_recording_toast),
          Toast.LENGTH_SHORT
        ).show()
        lifecycleScope.launch {
          repository.setRecordingState(false)
        }
      }
    }

  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  @SuppressLint("CoroutineCreationDuringComposition", "Recycle")
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val app = application as NoteUltraApp
    val vectorUtils = app.vectorUtils
    repository = SettingsRepository(dataStore)

    WindowCompat.setDecorFitsSystemWindows(window, false)
    enableEdgeToEdge()
    setContent {
      val recordingState by repository.recordingStateFlow.collectAsState(repository.recordingStateInitial())
      val llmMode by repository.llmModeFlow.collectAsState(repository.llmModeInitial())
      val darkTheme by repository.darkThemeFlow.collectAsState(repository.darkThemeInitial())
      val initialPage by repository.initialPageFlow.collectAsState(repository.initialPageInitial())

      NoteUltraTheme {
        val context = LocalContext.current
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
              description = stringResource(R.string.settings_item_recording_state_description),
              state = recordingState,
              onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                  repository.setRecordingState(!recordingState)
                  initiateRecording(
                    context,
                    repository.recordingStateInitial(),
                    requestPermissionsLauncher
                  )
                }
              }
            )

            SettingCategory(stringResource(R.string.settings_category_ai))

            SettingItemWithDialog(
              title = stringResource(R.string.settings_item_llm_settings_title),
              description = stringResource(R.string.settings_item_llm_settings_description),
              dialog = {
                val selectMode = remember { mutableStateOf(llmMode) }

                setup { selectMode.value = llmMode }
                title(stringResource(R.string.settings_item_llm_settings_title))
                content {
                  LlmMode.entries.forEach { mode ->
                    when (mode) {
                      LlmMode.LOCAL -> {
                        val isDialogShow = remember { mutableStateOf(false) }

                        OptionWithMoreButton(
                          isChecked = mode == selectMode.value,
                          onClick = { selectMode.value = mode },
                          text = stringResource(mode.labelResId),
                          button = {
                            icon(painterResource(R.drawable.rounded_settings_24))
                            description(stringResource(R.string.settings_local_llm_settings_button))
                            onClick { isDialogShow.value = true }
                          }
                        )

                        if (isDialogShow.value) {
                          val initial = repository.localLlmConfigInitial()
                          val loadOnStartup = remember { mutableStateOf(initial.loadOnStartup) }
                          val path = remember { mutableStateOf(initial.path) }
                          val startTag = remember { mutableStateOf(initial.startTag) }
                          val endTag = remember { mutableStateOf(initial.endTag) }
                          val questionPrompt = remember { mutableStateOf(initial.questionPrompt) }
                          val noteTitlePrompt = remember { mutableStateOf(initial.noteTitlePrompt) }
                          val noteContentPrompt =
                            remember { mutableStateOf(initial.noteContentPrompt) }

                          FullScreenDialog(
                            onDismissRequest = { isDialogShow.value = false },
                            topAppBar = {
                              CenterAlignedTopAppBar(
                                title = {
                                  Text(stringResource(R.string.settings_local_llm_config_title))
                                },
                                navigationIcon = {
                                  IconButton(
                                    onClick = { isDialogShow.value = false }
                                  ) {
                                    Icon(
                                      painter = painterResource(R.drawable.rounded_close_24),
                                      contentDescription = stringResource(R.string.settings_local_llm_config_close_button)
                                    )
                                  }
                                },
                                actions = {
                                  TextButton(
                                    modifier = Modifier
                                      .padding(end = 8.dp),
                                    onClick = {
                                      coroutineScope.launch {
                                        repository.setLocalLlmConfig(
                                          LocalLlmConfig(
                                            loadOnStartup = loadOnStartup.value,
                                            path = path.value,
                                            startTag = startTag.value,
                                            endTag = endTag.value,
                                            questionPrompt = questionPrompt.value,
                                            noteTitlePrompt = noteTitlePrompt.value,
                                            noteContentPrompt = noteContentPrompt.value
                                          )
                                        )
                                      }
                                      isDialogShow.value = false
                                    }
                                  ) {
                                    Text(stringResource(R.string.settings_local_llm_config_save_button))
                                  }
                                }
                              )
                            }
                          ) {
                            Column(
                              modifier = Modifier
                                .verticalScroll(rememberScrollState())
                            ) {
                              SettingItem(
                                title = stringResource(R.string.settings_item_load_on_startup_title),
                                description = stringResource(R.string.settings_item_load_on_startup_description),
                                state = loadOnStartup.value,
                                onClick = {
                                  loadOnStartup.value = !loadOnStartup.value
                                }
                              )
                              HorizontalDivider(
                                modifier = Modifier
                                  .padding(bottom = 8.dp)
                                  .fillMaxWidth()
                              )
                              OutlinedTextField(
                                modifier = Modifier
                                  .padding(vertical = 8.dp, horizontal = 24.dp)
                                  .fillMaxWidth(),
                                value = path.value,
                                onValueChange = { path.value = it },
                                label = { Text(stringResource(R.string.settings_local_llm_config_path)) },
                                singleLine = true
                              )
                              HorizontalDivider(
                                modifier = Modifier
                                  .padding(horizontal = 24.dp)
                                  .padding(top = 12.dp, bottom = 8.dp)
                                  .fillMaxWidth()
                              )
                              Row(
                                modifier = Modifier
                                  .padding(vertical = 8.dp, horizontal = 24.dp)
                                  .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                              ) {
                                OutlinedTextField(
                                  modifier = Modifier
                                    .weight(1f),
                                  value = startTag.value,
                                  onValueChange = { startTag.value = it },
                                  label = { Text(stringResource(R.string.settings_local_llm_config_start_tag)) },
                                  singleLine = true
                                )
                                OutlinedTextField(
                                  modifier = Modifier
                                    .weight(1f),
                                  value = endTag.value,
                                  onValueChange = { endTag.value = it },
                                  label = { Text(stringResource(R.string.settings_local_llm_config_end_tag)) },
                                  singleLine = true
                                )
                              }
                              HorizontalDivider(
                                modifier = Modifier
                                  .padding(horizontal = 24.dp)
                                  .padding(top = 12.dp, bottom = 8.dp)
                                  .fillMaxWidth()
                              )
                              OutlinedTextField(
                                modifier = Modifier
                                  .padding(vertical = 8.dp, horizontal = 24.dp)
                                  .fillMaxWidth(),
                                value = questionPrompt.value,
                                onValueChange = { questionPrompt.value = it },
                                label = { Text(stringResource(R.string.settings_local_llm_config_question_prompt)) }
                              )
                              OutlinedTextField(
                                modifier = Modifier
                                  .padding(vertical = 8.dp, horizontal = 24.dp)
                                  .fillMaxWidth(),
                                value = noteTitlePrompt.value,
                                onValueChange = { noteTitlePrompt.value = it },
                                label = { Text(stringResource(R.string.settings_local_llm_config_note_title_prompt)) }
                              )
                              OutlinedTextField(
                                modifier = Modifier
                                  .padding(vertical = 8.dp, horizontal = 24.dp)
                                  .fillMaxWidth(),
                                value = noteContentPrompt.value,
                                onValueChange = { noteContentPrompt.value = it },
                                label = { Text(stringResource(R.string.settings_local_llm_config_note_content_prompt)) }
                              )
                            }
                          }
                        }
                      }

                      LlmMode.REMOTE -> {
                        val isDialogShow = remember { mutableStateOf(false) }

                        OptionWithMoreButton(
                          isChecked = mode == selectMode.value,
                          onClick = { selectMode.value = mode },
                          text = stringResource(mode.labelResId),
                          button = {
                            icon(painterResource(R.drawable.rounded_settings_24))
                            description(stringResource(R.string.settings_remote_llm_settings_button))
                            onClick { isDialogShow.value = true }
                          }
                        )

                        if (isDialogShow.value) {
                          val url =
                            remember { mutableStateOf(repository.remoteLlmConfigInitial()) }

                          FullScreenDialog(
                            onDismissRequest = { isDialogShow.value = false },
                            topAppBar = {
                              CenterAlignedTopAppBar(
                                title = {
                                  Text(stringResource(R.string.settings_remote_llm_config_title))
                                },
                                navigationIcon = {
                                  IconButton(
                                    onClick = { isDialogShow.value = false }
                                  ) {
                                    Icon(
                                      painter = painterResource(R.drawable.rounded_close_24),
                                      contentDescription = stringResource(R.string.settings_remote_llm_config_close_button)
                                    )
                                  }
                                },
                                actions = {
                                  TextButton(
                                    modifier = Modifier
                                      .padding(end = 8.dp),
                                    onClick = {
                                      coroutineScope.launch {
                                        repository.setRemoteLlmConfig(url.value)
                                      }

                                      isDialogShow.value = false
                                    }
                                  ) {
                                    Text(stringResource(R.string.settings_remote_llm_config_save_button))
                                  }
                                }
                              )
                            }
                          ) {
                            OutlinedTextField(
                              modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 24.dp)
                                .fillMaxWidth(),
                              value = url.value,
                              onValueChange = { url.value = it },
                              label = { Text(stringResource(R.string.settings_remote_llm_config_url)) },
                              singleLine = true
                            )
                          }
                        }
                      }

                      LlmMode.DISABLE -> {
                        Option(
                          type = OptionType.RADIO,
                          isChecked = mode == selectMode.value,
                          onClick = { selectMode.value = mode },
                          text = stringResource(mode.labelResId)
                        )
                      }
                    }
                  }
                }
                onDismissRequest { closeDialog() }
                dismissButton {
                  TextButton(
                    onClick = { closeDialog() }
                  ) {
                    Text(stringResource(R.string.settings_llm_source_close_button))
                  }
                }
                confirmButton {
                  TextButton(
                    onClick = {
                      when (selectMode.value) {
                        LlmMode.LOCAL -> {
                          val path = repository.localLlmConfigInitial().path

                          if (path == "") {
                            Toast.makeText(
                              context,
                              getString(R.string.settings_llm_source_toast_local),
                              Toast.LENGTH_SHORT
                            ).show()

                            return@TextButton
                          }
                        }

                        LlmMode.REMOTE -> {
                          val url = repository.remoteLlmConfigInitial()

                          if (url == "") {
                            Toast.makeText(
                              context, getString(R.string.settings_llm_source_toast_remote),
                              Toast.LENGTH_SHORT
                            ).show()

                            return@TextButton
                          }
                        }

                        LlmMode.DISABLE -> {}
                      }
                      CoroutineScope(Dispatchers.IO).launch {
                        repository.setLlmMode(selectMode.value)
                      }
                      closeDialog()
                    }
                  ) {
                    Text(stringResource(R.string.settings_llm_source_confirm_button))
                  }
                }
              }
            )

            SettingCategory(stringResource(R.string.settings_category_display))

            SettingItemWithDialog(
              title = stringResource(R.string.settings_item_dark_theme_title),
              description = stringResource(R.string.settings_item_dark_theme_description),
              dialog = {
                title(stringResource(R.string.settings_item_dark_theme_title))
                content {
                  DarkTheme.entries.forEach { theme ->
                    Option(
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
                onDismissRequest { closeDialog() }
              }
            )

            SettingItemWithDialog(
              title = stringResource(R.string.settings_item_initial_page_title),
              description = stringResource(
                R.string.settings_item_initial_page_description,
                stringResource(initialPage.labelResId)
              ),
              dialog = {
                title(stringResource(R.string.settings_item_initial_page_title))
                content {
                  PagerState.entries.forEach { page ->
                    Option(
                      type = OptionType.RADIO,
                      isChecked = initialPage == page,
                      onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                          repository.setInitialPage(page)
                        }

                        closeDialog()
                      },
                      text = stringResource(page.labelResId)
                    )
                  }
                }
                onDismissRequest { closeDialog() }
              }
            )

            SettingCategory(stringResource(R.string.settings_category_other))

            SettingItemWithDialog(
              title = stringResource(R.string.settings_item_clear_data_title),
              description = stringResource(R.string.settings_item_clear_data_description),
              dialog = {
                val isSettingsCheck = remember { mutableStateOf(false) }
                val isNotesCheck = remember { mutableStateOf(false) }
                val isTranscriptCheck = remember { mutableStateOf(false) }
                val isAnyCheck =
                  isSettingsCheck.value || isNotesCheck.value || isTranscriptCheck.value

                setup {
                  isSettingsCheck.value = false
                  isNotesCheck.value = false
                  isTranscriptCheck.value = false
                }
                title(stringResource(R.string.settings_item_clear_data_title))
                content {
                  Option(
                    type = OptionType.CHECKBOX,
                    isChecked = isSettingsCheck.value,
                    onClick = { isSettingsCheck.value = !isSettingsCheck.value },
                    text = stringResource(R.string.settings_clear_data_setting)
                  )
                  Option(
                    type = OptionType.CHECKBOX,
                    isChecked = isNotesCheck.value,
                    onClick = { isNotesCheck.value = !isNotesCheck.value },
                    text = stringResource(R.string.settings_clear_data_notes)
                  )
                  Option(
                    type = OptionType.CHECKBOX,
                    isChecked = isTranscriptCheck.value,
                    onClick = { isTranscriptCheck.value = !isTranscriptCheck.value },
                    text = stringResource(R.string.settings_clear_data_transcript)
                  )
                }
                onDismissRequest {
                  closeDialog()
                }
                dismissButton {
                  TextButton(
                    onClick = {
                      closeDialog()
                    }
                  ) {
                    Text(stringResource(R.string.settings_clear_data_close_button))
                  }
                }
                confirmButton {
                  TextButton(
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
            )
          }
        }
      }
    }
  }
}