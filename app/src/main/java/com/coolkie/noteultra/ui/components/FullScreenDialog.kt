package com.coolkie.noteultra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDialog(
  modifier: Modifier = Modifier,
  onDismissRequest: () -> Unit,
  topAppBar: @Composable () -> Unit = {},
  content: @Composable () -> Unit
) {
  BasicAlertDialog(
    properties = DialogProperties(usePlatformDefaultWidth = false),
    onDismissRequest = { onDismissRequest() }
  ) {
    Surface(
      modifier = modifier
        .fillMaxSize(),
      shape = AlertDialogDefaults.shape
    ) {
      Column {
        topAppBar()
        content()
      }
    }
  }
}