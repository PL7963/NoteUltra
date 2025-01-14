package com.coolkie.noteultra.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import com.coolkie.noteultra.utils.VectorUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatterDate(localDate: LocalDate): String {
  val formatter = DateTimeFormatter.ofPattern("MM/dd")
  return localDate.format(formatter)
}

@Composable
fun HistorySheet(vectorUtils: VectorUtils, drawerState: DrawerState) {
  val currentDate by vectorUtils.currentDate
  val dates by vectorUtils.allDates

  ModalDrawerSheet {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(Modifier.height(12.dp))
      Text(
        stringResource(R.string.history_sheet_title),
        modifier = Modifier
          .padding(12.dp),
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        stringResource(R.string.history_sheet_recent),
        modifier = Modifier
          .padding(12.dp),
        style = MaterialTheme.typography.titleMedium
      )
      dates.take(5).forEach { date ->
        DateItem(date, currentDate, drawerState, vectorUtils)
      }
      if (dates.size > 5) {
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(
          stringResource(R.string.history_sheet_old),
          modifier = Modifier
            .padding(12.dp),
          style = MaterialTheme.typography.titleMedium
        )
        dates.drop(5).forEach { date ->
          DateItem(date, currentDate, drawerState, vectorUtils)
        }
      }
    }
  }
}

@Composable
fun DateItem(
  localDate: LocalDate,
  currentDate: LocalDate,
  drawerState: DrawerState,
  vectorUtils: VectorUtils
) {
  val coroutineScope = rememberCoroutineScope()
  val date = formatterDate(localDate)
  var text = date
  if (localDate == LocalDate.now()) {
    text = stringResource(R.string.history_sheet_today)
  }
  NavigationDrawerItem(
    label = {
      Row {
        Icon(
          painter = painterResource(R.drawable.round_fiber_manual_record_24),
          contentDescription = ""
        )
        Text(
          text,
          modifier = Modifier
            .padding(horizontal = 12.dp)
        )
      }
    },
    selected = date == formatterDate(currentDate),
    onClick = {
      vectorUtils.setCurrentDate(localDate)
      coroutineScope.launch {
        drawerState.close()
      }
    }
  )
}