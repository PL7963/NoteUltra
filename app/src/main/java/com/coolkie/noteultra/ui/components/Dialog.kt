package com.coolkie.noteultra.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.coolkie.noteultra.ui.OptionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun Dialog(
  title: String,
  visible: MutableState<Boolean>,
  crossinline onDismissRequest: () -> Unit,
  noinline dismissButton: @Composable () -> Unit = {},
  noinline confirmButton: @Composable () -> Unit = {},
  crossinline content: @Composable () -> Unit
) {
  if (visible.value) {
    BasicAlertDialog({
      onDismissRequest()
    }) {
      Surface(
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation
      ) {
        Column(
          horizontalAlignment = Alignment.Start,
          modifier = Modifier
            .padding(vertical = 8.dp)
        ) {
          Text(
            text = title,
            modifier = Modifier
              .padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
          )
          content()
          if (dismissButton != {} || confirmButton != {}) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 2.dp, bottom = 6.dp)
            ) {
              dismissButton()
              confirmButton()
            }
          }
        }
      }
    }
  }
}


@Composable
inline fun Option(
  modifier: Modifier = Modifier,
  type: OptionType,
  isChecked: Boolean,
  crossinline onClick: () -> Unit,
  text: String,
  crossinline content: @Composable () -> Unit = {}
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    when (type) {
      OptionType.RADIO -> {
        RadioButton(
          selected = isChecked,
          onClick = null,
          modifier = Modifier
            .padding(12.dp)
            .padding(start = 8.dp)
        )
      }

      OptionType.CHECKBOX -> {
        Checkbox(
          checked = isChecked,
          onCheckedChange = null,
          modifier = Modifier
            .padding(12.dp)
            .padding(start = 8.dp)
        )
      }
    }
    Text(
      text = text,
      modifier = Modifier
        .weight(1f)
        .padding(start = 8.dp)
    )
    content()
  }
}


class MoreButtonBuilder {
  lateinit var icon: Painter
  var description: String? = null
  lateinit var onClick: () -> Unit

  fun icon(block: Painter) {
    icon = block
  }

  fun description(block: String?) {
    description = block
  }

  fun onClick(block: () -> Unit) {
    onClick = block
  }
}

@Composable
inline fun OptionWithMoreButton(
  isChecked: Boolean,
  crossinline onClick: () -> Unit,
  text: String,
  button: MoreButtonBuilder.() -> Unit
) {
  val builder = MoreButtonBuilder().apply(button)
  val density = LocalDensity.current
  val optionHeight = remember { mutableStateOf(0.dp) }

  Option(
    modifier = Modifier
      .onGloballyPositioned {
        val height = it.size.height
        optionHeight.value = with(density) { height.toDp() }
      },
    type = OptionType.RADIO,
    isChecked = isChecked,
    onClick = onClick,
    text = text
  ) {
    VerticalDivider(
      modifier = Modifier
        .height(optionHeight.value - 16.dp)
    )
    Box(
      modifier = Modifier
        .padding(end = 8.dp)
        .height(optionHeight.value)
        .clickable { builder.onClick() }
    ) {
      Icon(
        modifier = Modifier
          .padding(12.dp),
        painter = builder.icon,
        contentDescription = builder.description
      )
    }
  }
}