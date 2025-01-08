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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.data.NoteViewModel

val userQueryList = mutableStateListOf<String>()
val llmResponseList = mutableStateListOf<String>()

@Composable
fun Chat(noteViewModel: NoteViewModel) {
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
          LlmResponse(it, noteViewModel)
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
fun LlmResponse(llmResponse: String, noteViewModel: NoteViewModel) {
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
              noteViewModel.addNote(
                "Note Title",
                "Material design的根本都是來自現實世界中的印刷設計，像是頁面的基線以及網格結構。這種佈局都是被設計給予不同屏幕尺寸且便於UI的開發使用，最終的目的是要做出可伸縮的應用程式。",
                System.currentTimeMillis()
              )
            },
            modifier = Modifier
              .align(Alignment.TopEnd)
          ) {
            Icon(
              painter = painterResource
                (id = R.drawable.rounded_add_24),
              contentDescription = "Add"
            )
          }
        }
      }
    }
  }
}