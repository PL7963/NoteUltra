package com.coolkie.noteultra.ui

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.data.NoteViewModel
import com.coolkie.noteultra.utils.LlmInferenceUtils
import com.coolkie.noteultra.utils.VectorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
@ExperimentalMaterial3Api
fun MainView(
  llmInstance: LlmInferenceUtils,
  vectorUtils: VectorUtils,
  noteViewModel: NoteViewModel
) {
  val context = LocalContext.current
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val pagerState = rememberPagerState { 2 }
  val selectedOption = remember { mutableIntStateOf(0) }
  val coroutineScope = rememberCoroutineScope()
  val scaffoldState = rememberBottomSheetScaffoldState()
  val focusManager = LocalFocusManager.current
  val userInput = remember { mutableStateOf("") }

  LaunchedEffect(pagerState.currentPage) {
    selectedOption.intValue = pagerState.currentPage
  }
  ModalNavigationDrawer(
    drawerContent = {
      HistorySheet(vectorUtils, drawerState)
    },
    drawerState = drawerState
  ) {
    Scaffold(
      modifier = Modifier
        .imePadding()
        .pointerInput(Unit) {
          detectTapGestures {
            focusManager.clearFocus()
          }
        },
      topBar = {
        CenterAlignedTopAppBar(
          modifier = Modifier
            .fillMaxWidth(),
          title = {
            SingleChoiceSegmentedButtonRow {
              SegmentedButton(
                selected = selectedOption.intValue == 0,
                onClick = {
                  coroutineScope.launch {
                    launch {
                      focusManager.clearFocus()
                    }
                    launch {
                      scaffoldState.bottomSheetState.partialExpand()
                    }
                    launch {
                      pagerState.animateScrollToPage(0)
                    }

                  }
                },
                shape = RoundedCornerShape(100.dp, 0.dp, 0.dp, 100.dp)
              ) {
                Icon(
                  painter = painterResource(id = R.drawable.rounded_grid_view_24),
                  contentDescription = "Grid"
                )
              }
              SegmentedButton(
                selected = selectedOption.intValue == 1,
                onClick = {
                  coroutineScope.launch {
                    launch {
                      focusManager.clearFocus()
                    }
                    launch {
                      scaffoldState.bottomSheetState.partialExpand()
                    }
                    launch {
                      pagerState.animateScrollToPage(1)
                    }
                  }
                },
                shape = RoundedCornerShape(0.dp, 100.dp, 100.dp, 0.dp)
              ) {
                Icon(
                  painter = painterResource(R.drawable.rounded_article_24),
                  contentDescription = "Grid"
                )
              }
            }
          },
          navigationIcon = {
            IconButton(
              onClick = {
                coroutineScope.launch {
                  launch {
                    focusManager.clearFocus()
                  }
                  launch {
                    scaffoldState.bottomSheetState.partialExpand()
                  }
                  launch {
                    drawerState.open()
                  }
                }
              }
            ) {
              Icon(
                painter = painterResource(id = R.drawable.rounded_menu_24),
                contentDescription = "Menu"
              )
            }
          },
          actions = {
            IconButton(
              onClick = {
                focusManager.clearFocus()
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
              }
            ) {
              Icon(
                painter = painterResource(id = R.drawable.rounded_settings_24),
                contentDescription = "Settings"
              )
            }
          }
        )
      },
      bottomBar = {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(56.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .onFocusChanged { focusState ->
              if (focusState.isFocused) {
                coroutineScope.launch {
                  scaffoldState.bottomSheetState.expand()
                }
              }
            }
        ) {
          TextField(
            value = userInput.value,
            onValueChange = { userInput.value = it },
            placeholder = { Text("Enter the question") },
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp)
              .padding(horizontal = 16.dp)
              .clip(RoundedCornerShape(28.dp)),
            maxLines = 4,
            trailingIcon = {
              IconButton(
                onClick = {
                  if (userInput.value.isNotEmpty()) {
                    userQueryList.add(userInput.value)
                    coroutineScope.launch {
                      launch {
                        focusManager.clearFocus()
                      }
                      launch {
                        scaffoldState.bottomSheetState.expand()
                      }
                      launch {
                        llmResponseList.add(
                          withContext(Dispatchers.IO) { llmInstance.answerUserQuestion() }
                        )
                      }
                      userInput.value = ""
                    }
                  }
                },
                modifier = Modifier
                  .padding(end = 4.dp)
              ) {
                Icon(
                  painter = painterResource
                    (id = R.drawable.rounded_send_24),
                  contentDescription = "Send"
                )
              }
            }
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .padding(innerPadding)
      ) {
        BottomSheetScaffold(
          scaffoldState = scaffoldState,
          sheetPeekHeight = 40.dp,
          sheetContent = { Chat(noteViewModel) }
        ) {
          HorizontalPager(
            state = pagerState,
            modifier = Modifier
              .fillMaxSize()
          ) { page ->
            when (page) {
              0 -> ListView(noteViewModel)
              1 -> TimeView(vectorUtils)
            }
          }
        }
      }
    }
  }
}