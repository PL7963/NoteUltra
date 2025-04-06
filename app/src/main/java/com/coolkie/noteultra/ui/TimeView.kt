package com.coolkie.noteultra.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import kotlinx.coroutines.launch

@Composable
fun TimeView() {
  val vectorUtils = LocalVectorUtils.current
  val itemsList by vectorUtils.dateAllContent
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val initial = remember { mutableStateOf(true) }
  val buttonVisible = remember { mutableStateOf(false) }
  val offsetX by animateDpAsState(if (buttonVisible.value) -12.dp else 64.dp, label = "offsetX")

  LaunchedEffect(listState.canScrollForward) {
    if (!initial.value) {
      buttonVisible.value = listState.canScrollForward
    }
  }

  LaunchedEffect(itemsList) {
    if (itemsList.isNotEmpty()) {
      val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index

      if (lastVisibleIndex == itemsList.lastIndex || initial.value) {
        listState.scrollToItem(itemsList.lastIndex)
        initial.value = false
      }
    }
  }

  Box(
    contentAlignment = Alignment.BottomEnd,
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize(),
      state = listState,
      contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 40.dp)
    ) {
      items(itemsList) { item ->
        Text(item)
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
    FloatingActionButton(
      onClick = {
        coroutineScope.launch {
          listState.animateScrollToItem(itemsList.lastIndex)
          buttonVisible.value = false
        }
      },
      shape = CircleShape,
      modifier = Modifier
        .offset(offsetX, -52.dp)
    ) {
      Icon(
        painter = painterResource(id = R.drawable.rounded_arrow_downward_24),
        contentDescription = stringResource(R.string.time_view_floating_button)
      )
    }
  }
}