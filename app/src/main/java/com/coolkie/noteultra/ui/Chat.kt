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

val userQueryList = mutableStateListOf<String>()
val llmResponseList = mutableStateListOf<String>()

@Composable
fun Chat() {
  val listState = rememberLazyListState()
  val chatMessages = mutableListOf<Any>()
  val maxSize = maxOf(userQueryList.size, llmResponseList.size)

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyColumn(
      state = listState,
      contentPadding = PaddingValues(12.dp)
    ) {
      for (index in 0 until maxSize) {
        if (index < userQueryList.size) {
          chatMessages.add(userQueryList[index])
        }
        if (index < llmResponseList.size) {
          chatMessages.add(llmResponseList[index])
        }
      }

      itemsIndexed(chatMessages) { _, item ->
        when (item) {
          is String -> {
            if (userQueryList.contains(item)) {
              UserQuery(item)
            } else {
              LlmResponse(item)
            }
          }
        }
      }
    }
  }
}

@Composable
fun UserQuery(query: String) {
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

@Composable
fun LlmResponse(llmResponse: String) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
  ) {
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
          onClick = { /* todo */ },
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