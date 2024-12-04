package com.coolkie.noteultra

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
@ExperimentalMaterial3Api
fun MainView() {
  val scaffoldState = rememberBottomSheetScaffoldState()

  Surface(
    color = MaterialTheme.colorScheme.background
  ) {
    Column {
      CenterAlignedTopAppBar(
        modifier = Modifier
          .fillMaxWidth(),
        title = {
          Box(
            modifier = Modifier
              .width(128.dp),
            contentAlignment = Alignment.Center
          ) {
            OutlinedButton(
              onClick = { /*TODO*/ },
              modifier = Modifier
                .width(64.dp)
                .offset((-31).dp)
                .clip(
                  RoundedCornerShape(100.dp, 0.dp, 0.dp, 100.dp)
                ),
              shape = RoundedCornerShape(100.dp, 0.dp, 0.dp, 100.dp)
            ) {
              Icon(
                painter = painterResource(id = R.drawable.rounded_grid_view_24),
                contentDescription = "Grid"
              )
            }
            OutlinedButton(
              onClick = { /*TODO*/ },
              modifier = Modifier
                .width(65.dp)
                .offset((32).dp)
                .clip(
                  RoundedCornerShape(0.dp, 100.dp, 100.dp, 0.dp)
                ),
              shape = RoundedCornerShape(0.dp, 100.dp, 100.dp, 0.dp)
            ) {
              Icon(
                painter = painterResource(id = R.drawable.rounded_article_24),
                contentDescription = "Article"
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
            onClick = { /*TODO*/ }
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
            state = rememberPagerState { 2 },
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
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .background(MaterialTheme.colorScheme.surfaceContainerLow)
      ) {
        TextField(
          value = "",
          onValueChange = { /*TODO*/ },
          placeholder = { Text("Enter the question") },
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(100.dp)),
          trailingIcon = {
            IconButton(
              onClick = { /*TODO*/ },
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