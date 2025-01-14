package com.coolkie.noteultra.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.utils.LlmInferenceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val userQueryList = mutableStateListOf<String>()
val llmResponseList = mutableStateListOf<String>()

@Composable
fun Chat(noteViewModel: NoteViewModel, llmInstance: LlmInferenceUtils, isButtonEnable: MutableState<Boolean>) {
  val listState = rememberLazyListState()

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyColumn(
      state = listState,
      contentPadding = PaddingValues(12.dp)
    ) {
      itemsIndexed(userQueryList) { index, userQuery ->
        UserQuery(userQuery)
        llmResponseList.getOrNull(index)?.let {
          LlmResponse(it, noteViewModel, llmInstance, isButtonEnable)
        }
      }
    }
  }
}

@Composable
fun UserQuery(query: String) {
  SelectionContainer {
    Box(
      modifier = Modifier
        .fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.8f)
          .align(Alignment.TopEnd)
      ) {
        Text(
          text = query,
          modifier = Modifier
            .align(Alignment.TopEnd)
        )
      }
    }
  }
}

@Composable
fun LlmResponse(llmResponse: String, noteViewModel: NoteViewModel, llmInstance: LlmInferenceUtils, isButtonEnable: MutableState<Boolean>) {
  val coroutineScope = rememberCoroutineScope()

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
  ) {
    SelectionContainer {
      Column(
        modifier = Modifier
          .fillMaxWidth()
      ) {
        Text(
          modifier = Modifier
            .padding(start = 12.dp, top = 12.dp, end = 12.dp),
          text = llmResponse
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
        ) {
          IconButton(
            onClick = {
              coroutineScope.launch(Dispatchers.IO) {
                isButtonEnable.value = false
                val note = llmInstance.generateNotes(llmResponse)
                noteViewModel.addNote(
                  note[0],note[1],
                  System.currentTimeMillis()
                )
                isButtonEnable.value = true
              }
            },
            modifier = Modifier
              .align(Alignment.TopEnd),
            enabled = isButtonEnable.value
          ) {
            Icon(
              painter = painterResource
                (id = R.drawable.rounded_add_24),
              contentDescription = stringResource(R.string.chat_page_add_note_button)
            )
          }
        }
      }
    }
  }
}