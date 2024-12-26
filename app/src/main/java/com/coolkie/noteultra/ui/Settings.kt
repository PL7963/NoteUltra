package com.coolkie.noteultra.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.ui.theme.NoteUltraTheme

class SettingsActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      NoteUltraTheme {
        Scaffold(
          topBar = {
            TopAppBar(
              modifier = Modifier
                .fillMaxWidth(),
              title = {},
              navigationIcon = {
                IconButton(
                  onClick = { finish() }
                ) {
                  Icon(
                    painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                    contentDescription = "Back"
                  )
                }
              }
            )
          }
        ) { innerPadding ->
          Column(
            modifier = Modifier
              .padding(innerPadding)
          ) {
            SettingCategory("App Settings")
            SwitchOption(
              "Press to note",
              "Transcribing in real-time can be performance hungry. Press to note only records voices when you hold your phone firmly\n" +
                      "*Requires pressure sensor ",
              false
            )
            SettingCategory("AI Settings")
            DialogOption(
              "LLM Models",
              "LLM Models setting define which Large Language model process your text input"
            )
            SettingCategory("Other")
            DialogOption(
              "Dark mode",
              "Change color scheme for this app"
            )
            DialogOption(
              "Clear data",
              "WARNING: Clear data will clear all the conversation you recorded. This is irreversible!"
            )
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
      .padding(horizontal = 32.dp, vertical = 12.dp),
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.primary,
    text = text
  )
}

@Composable
fun SwitchOption(title: String, subtitle: String, boolean: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable {}
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 32.dp, vertical = 12.dp)
        .weight(1f)
    ) {
      Text(
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        text = title
      )
      Text(
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        text = subtitle
      )
    }
    Switch(
      modifier = Modifier
        .padding(end = 16.dp),
      checked = boolean,
      onCheckedChange = { /*TODO*/ }
    )
  }
}

@Composable
fun DialogOption(title: String, subtitle: String) {
  Box(
    contentAlignment = Alignment.CenterStart,
    modifier = Modifier
      .fillMaxWidth()
      .clickable {}
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 32.dp, vertical = 12.dp)
    ) {
      Text(
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        text = title
      )
      Text(
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        text = subtitle
      )
    }
  }
}