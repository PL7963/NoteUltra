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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.utils.VectorUtils

@Composable
fun TimeView(vectorUtils: VectorUtils) {
  val itemsList by vectorUtils.dateAllContent
  val listState = rememberLazyListState()

  LaunchedEffect(itemsList) {
    val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
    if (lastVisibleItemIndex == itemsList.lastIndex) {
      listState.animateScrollToItem(
        itemsList.size - 1
      )
    }
  }

  Box(
    contentAlignment = Alignment.TopStart,
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize(),
      state = listState,
      contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 48.dp)
    ) {
      items(itemsList) { item ->
        Text(item)
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}