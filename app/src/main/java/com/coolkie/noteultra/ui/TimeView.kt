package com.coolkie.noteultra.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TimeView() {
  val itemsList = remember { mutableStateListOf<Int>() }
  val listState = rememberLazyListState()

  LaunchedEffect(Unit) {
    var index = 0
    while (true) {
      delay(1000)
      val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
      val isAtBottom = lastVisibleIndex == itemsList.size - 1
      itemsList.add(index++)
      if (isAtBottom) {
        listState.animateScrollToItem(
          itemsList.size - 1
        )
      }
    }
  }

  Box(
    contentAlignment = Alignment.TopStart,
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyColumn(
      state = listState,
      contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 48.dp)
    ) {
      items(itemsList) { index ->
        ProduceDialogue("A", "${index + 1}, Did you eat cat today?")
        ProduceDialogue("B", "${index + 1}, No, I eat dog")
        ProduceDialogue(
          "C",
          "${index + 1}, Material design的根本都是來自現實世界中的印刷設計，像是頁面的基線以及網格結構。這種佈局都是被設計給予不同屏幕尺寸且便於UI的開發使用，最終的目的是要做出可伸縮的應用程式。"
        )
      }
    }
  }
}

@Composable
fun ProduceDialogue(speaker: String, content: String) {
  Text(
    text = "$speaker: $content"
  )
  Spacer(modifier = Modifier.height(8.dp))
}