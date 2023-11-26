package view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.models.core.GameConfig
import kotlin.random.Random
import kotlin.random.nextInt

@Composable
fun GridField(
    modifier: Modifier = Modifier,
    gameConfig: GameConfig,
    cells: MutableMap<Int, Color>
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(gameConfig.width),
    ) {
        items(gameConfig.height * gameConfig.width) { index ->
            val random = Random(index).nextInt(220..230)
            val color = cells[index] ?: Color(
                random,
                random,
                random
            )
            Box(
                modifier = Modifier.width(10.dp).aspectRatio(1f).background(color)
            )
        }
    }
}