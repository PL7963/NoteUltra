package com.coolkie.noteultra

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ListView() {
  Box(
    contentAlignment = Alignment.TopStart,
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 48.dp)
    ) {
      items(30) { index ->
        ProduceCard(
          "This is a title",
          "${index + 1}, Material design的根本都是來自現實世界中的印刷設計，像是頁面的基線以及網格結構。這種佈局都是被設計給予不同屏幕尺寸且便於UI的開發使用，最終的目的是要做出可伸縮的應用程式。"
        )
      }
    }
  }
}

@Composable
fun ProduceCard(title: String, content: String) {
  val showDialog = remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .padding(3.dp)
      .aspectRatio(1f)
      .clickable{
        showDialog.value = true
      }
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = content,
        style = MaterialTheme.typography.titleMedium
      )
    }
  }

  if (showDialog.value) {
    AlertDialog(
      onDismissRequest = { showDialog.value = false },
      title = { Text(title) },
      text = {
        Column(
          modifier = Modifier
            .verticalScroll(rememberScrollState())
        ) {
          Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showDialog.value = false }
        ) {
          Text("close")
        }
      }
    )
  }
}