package com.coolkie.noteultra

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.utils.LlmInferenceUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
@ExperimentalMaterial3Api
fun MainView(llmInstance: LlmInferenceUtils) {
  val context = LocalContext.current
  val pagerState = rememberPagerState { 2 }
  val selectedOption = remember { mutableIntStateOf(0) }
  val coroutineScope = rememberCoroutineScope()
  val scaffoldState = rememberBottomSheetScaffoldState()
  val userInput = remember { mutableStateOf("") }

  Surface(
    color = MaterialTheme.colorScheme.background
  ) {
    Column {
      CenterAlignedTopAppBar(
        modifier = Modifier
          .fillMaxWidth(),
        title = {
          SingleChoiceSegmentedButtonRow {
            SegmentedButton(
              selected = selectedOption.intValue == 0,
              onClick = {
                selectedOption.intValue = 0
                coroutineScope.launch {
                  pagerState.animateScrollToPage(0)
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
                selectedOption.intValue = 1
                coroutineScope.launch {
                  pagerState.animateScrollToPage(1)
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
            onClick = { /*TODO*/ }
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
      Box(
        modifier = Modifier
          .weight(1f)
      ) {
        BottomSheetScaffold(
          scaffoldState = scaffoldState,
          sheetPeekHeight = 48.dp,
          sheetContent = { Chat() }
        ) {
          HorizontalPager(
            state = pagerState,
            modifier = Modifier
              .fillMaxSize()
          ) { page ->
            when (page) {
              0 -> ListView()
              1 -> TimeView()
            }
          }
        }
      }
      LaunchedEffect(pagerState.currentPage) {
        selectedOption.intValue = pagerState.currentPage
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .background(MaterialTheme.colorScheme.surfaceContainerLow)
      ) {
        TextField(
          value = userInput.value,
          onValueChange = { userInput.value = it },
          placeholder = { Text("Enter the question") },
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(100.dp)),
          trailingIcon = {
            IconButton(
              onClick = {
                itemsList.add(userInput.value)
                userInput.value = ""
                llmResponse.add(llmInstance.answerUserQuestion())
              },
              modifier = Modifier
                .padding(end = 4.dp)
                .size(48.dp)
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
  }
}