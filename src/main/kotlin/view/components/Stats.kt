package view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


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
        StatItem(Icons.Rounded.ThumbUp, "Высота поля", fieldHeight.toString()),
        StatItem(Icons.Rounded.ArrowForward, "Ширина поля", fieldWidth.toString()),
        StatItem(Icons.Rounded.ShoppingCart, "Статическая еда", foodStatic.toString()),
        StatItem(Icons.Rounded.Build, "Задержка состояния", "$stateDelayMs мс")
    )

    val contentPadding = 2.dp
    Column(modifier) {
        Text("Текущая игра")
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
private fun StatChip(icon: ImageVector, label: String, value: String, modifier: Modifier) {
    Chip(
        onClick = { /* Do something! */ },
        border = BorderStroke(ChipDefaults.OutlinedBorderSize, Color.LightGray),
        shape = RoundedCornerShape(12.dp),
        colors = ChipDefaults.chipColors(backgroundColor = Color.White),
        leadingIcon = {
            Icon(
                painter = painterResource(icon.name),
                contentDescription = label
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

data class StatItem(val icon: ImageVector, val label: String, val value: String)
