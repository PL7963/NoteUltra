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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatterDate(localDate: LocalDate): String {
  val formatter = DateTimeFormatter.ofPattern("MM/dd")
  return localDate.format(formatter)
}

@Composable
fun HistorySheet(
  dates: List<LocalDate>,
  currentLocalDate: MutableState<LocalDate>,
  drawerState: DrawerState
) {
  ModalDrawerSheet {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(Modifier.height(12.dp))
      Text(
        "History",
        modifier = Modifier
          .padding(12.dp),
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        "Last 5 days",
        modifier = Modifier
          .padding(12.dp),
        style = MaterialTheme.typography.titleMedium
      )
      dates.take(5).forEach { date ->
        DateItem(date, currentLocalDate, drawerState)
      }
      if (dates.size > 5) {
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text(
          "Old",
          modifier = Modifier
            .padding(12.dp),
          style = MaterialTheme.typography.titleMedium
        )
        dates.drop(5).forEach { date ->
          DateItem(date, currentLocalDate, drawerState)
        }
      }
    }
  }
}

@Composable
fun DateItem(
  localDate: LocalDate,
  currentLocalDate: MutableState<LocalDate>,
  drawerState: DrawerState
) {
  val coroutineScope = rememberCoroutineScope()
  val date = formatterDate(localDate)
  var text = date
  if (localDate == LocalDate.now()) {
    text = "Today"
  }
  NavigationDrawerItem(
    label = {
      Row {
        Icon(
          painter = painterResource(R.drawable.round_fiber_manual_record_24),
          contentDescription = "Grid"
        )
        Text(
          text,
          modifier = Modifier
            .padding(horizontal = 12.dp)
        )
      }
    },
    selected = date == formatterDate(currentLocalDate.value),
    onClick = {
      currentLocalDate.value = localDate
      coroutineScope.launch {
        drawerState.close()
      }
    }
  )
}