package com.coolkie.noteultra.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.data.Note

@Composable
fun ListView() {
  val noteViewModel = LocalNoteViewModel.current
  val notes = noteViewModel.noteList.collectAsState().value

  Box(
    contentAlignment = Alignment.TopStart,
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 48.dp)
    ) {
      items(notes) { note ->
        ProduceCard(note)
      }
    }
  }
}

@Composable
fun ProduceCard(note: Note) {
  val noteViewModel = LocalNoteViewModel.current
  val showDialog = remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .padding(3.dp)
      .aspectRatio(1f)
      .clickable {
        showDialog.value = true
      }
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
    ) {
      Text(
        text = note.title,
        style = MaterialTheme.typography.titleLarge
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = note.content,
        style = MaterialTheme.typography.titleMedium
      )
    }
  }

  if (showDialog.value) {
    AlertDialog(
      onDismissRequest = {},
      title = { Text(note.title) },
      text = {
        SelectionContainer {
          Text(
            text = note.content,
            style = MaterialTheme.typography.bodyLarge
          )
        }
      },
      dismissButton = {
        IconButton(
          onClick = {
            showDialog.value = false
            noteViewModel.deleteNote(note)
          }
        ) {
          Icon(
            painter = painterResource(id = R.drawable.rounded_delete_24),
            contentDescription = stringResource(R.string.list_view_delete_note_button)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showDialog.value = false }
        ) {
          Text(stringResource(R.string.list_view_close_note_button))
        }
      }
    )
  }
}