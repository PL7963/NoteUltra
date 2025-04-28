package com.coolkie.noteultra.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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


@Composable
inline fun SettingItem(
  title: String,
  description: String,
  state: Boolean? = null,
  crossinline onClick: () -> Unit = {},
  crossinline content: @Composable () -> Unit = {}
) {
  val boolean = remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        boolean.value = true
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
      if (state != null) {
        Switch(
          checked = state,
          onCheckedChange = null
        )
      }
      content()
    }
  }
}


interface DialogScope {
  val closeDialog: () -> Unit
}

class DialogBuilder {
  var setup: () -> Unit = {}
  lateinit var title: String
  lateinit var onDismissRequest: DialogScope.() -> Unit
  lateinit var content: @Composable DialogScope.() -> Unit
  var dismissButton: @Composable DialogScope.() -> Unit = {}
  var confirmButton: @Composable DialogScope.() -> Unit = {}

  fun setup(block: () -> Unit) {
    setup = block
  }

  fun title(block: String) {
    title = block
  }

  fun onDismissRequest(block: DialogScope.() -> Unit) {
    onDismissRequest = block
  }

  fun content(block: @Composable DialogScope.() -> Unit) {
    content = block
  }

  fun dismissButton(block: @Composable DialogScope.() -> Unit) {
    dismissButton = block
  }

  fun confirmButton(block: @Composable DialogScope.() -> Unit) {
    confirmButton = block
  }
}

@Composable
inline fun SettingItemWithDialog(
  title: String,
  description: String,
  dialog: DialogBuilder.() -> Unit
) {
  val isVisible = remember { mutableStateOf(false) }
  val scope = object : DialogScope {
    override val closeDialog: () -> Unit = { isVisible.value = false }
  }
  val builder = DialogBuilder().apply(dialog)

  SettingItem(
    title = title,
    description = description,
    onClick = {
      builder.setup()
      isVisible.value = true
    }
  ) {
    Dialog(
      title = builder.title,
      visible = isVisible,
      onDismissRequest = { builder.onDismissRequest(scope) },
      dismissButton = { builder.dismissButton(scope) },
      confirmButton = { builder.confirmButton(scope) }
    ) {
      builder.content(scope)
    }
  }
}