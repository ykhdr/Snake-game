package view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import view.res.GameIcon


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Stats(
    modifier: Modifier = Modifier,
    fieldHeight: Int,
    fieldWidth: Int,
    foodStatic: Int,
    stateDelayMs: Int
) {
    val statItems = listOf(
        StatItem(GameIcon.HEIGHT, "Высота поля", fieldHeight.toString()),
        StatItem(GameIcon.WIDTH, "Ширина поля", fieldWidth.toString()),
        StatItem(GameIcon.FOOD, "Статическая еда", foodStatic.toString()),
        StatItem(GameIcon.DELAY, "Задержка состояния", "$stateDelayMs мс")
    )

    val contentPadding = 2.dp
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Текущая игра", textAlign = TextAlign.Center)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(115.dp),
            contentPadding = PaddingValues(0.dp, 4.dp, 0.dp, 4.dp),
            content = {
                items(statItems) { item ->
                    StatChip(item.icon, item.label, item.value, Modifier.padding(contentPadding))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StatChip(icon: GameIcon, label: String, value: String, modifier: Modifier) {
    Chip(
        onClick = { /* Do something! */ },
        border = BorderStroke(ChipDefaults.OutlinedBorderSize, Color.LightGray),
        shape = RoundedCornerShape(12.dp),
        colors = ChipDefaults.chipColors(backgroundColor = Color.White),
        leadingIcon = {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = icon.painter(),
                contentDescription = label,
                tint = MaterialTheme.colors.primary
                )
        },
        modifier = modifier.sizeIn(minWidth = 80.dp, minHeight = 48.dp)
    ) {
        Column {
            Text(label, softWrap = true)
            Text(value, softWrap = true)
        }
    }
}

data class StatItem(val icon: GameIcon, val label: String, val value: String)
