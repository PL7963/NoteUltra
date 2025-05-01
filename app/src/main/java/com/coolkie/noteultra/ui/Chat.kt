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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Chat(
  userQueryList: MutableList<String>,
  llmResponseList: MutableList<String>,
  isButtonEnable: MutableState<Boolean>
) {
  val listState = rememberLazyListState()

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    LazyColumn(
      state = listState,
      contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
      item {
        UserQuery("Note Ultra的特點是什麼？")
        LlmResponse("NoteUltra 是一款結合語音辨識與大型語言模型的智慧記事應用程式，專為無感記錄與自然搜尋而設計。它能自動辨識對話內容並轉為語意向量儲存，用戶只需用自然語言提問，就能快速搜尋並獲得相關對話資訊。支援純地端與混合雲運算，兼顧效能與隱私安全。介面遵循 Material 3 設計，支援無障礙操作，簡單易用。特別適合學生、職場人士與重視資訊隱私的使用者，是日常記錄與回顧的重要輔助工具。", isButtonEnable)
      }

      itemsIndexed(userQueryList) { index, userQuery ->
        UserQuery(userQuery)
        llmResponseList.getOrNull(index)?.let {
          LlmResponse(it, isButtonEnable)
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
fun LlmResponse(
  llmResponse: String,
  isButtonEnable: MutableState<Boolean>
) {
  val noteViewModel = LocalNoteViewModel.current
  val llmInstance = LocalLlmInstance.current
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
                  note[0], note[1],
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