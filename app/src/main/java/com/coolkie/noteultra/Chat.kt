package com.coolkie.noteultra

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val itemsList = mutableStateListOf<String>()
val llmResponse = mutableStateListOf<String>()

@Composable
fun Chat() {
  val listState = rememberLazyListState()

  LaunchedEffect(Unit) {
    var index = 0
    while (true) {
      delay(1000)
      val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
      val isAtBottom = lastVisibleIndex == itemsList.size - 1
      if (isAtBottom) {
        listState.animateScrollToItem(
          itemsList.size - 1
        )
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyColumn(
      state = listState,
      contentPadding = PaddingValues(12.dp)
    ) {
      items(itemsList) { item ->
        UserQuery(item)
      }
      items(llmResponse) { item ->
        LlmResponse(item)
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