package view.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.models.core.GameConfig
import view.components.GridField

@Composable
fun GameView(
    config: GameConfig,
    cells: MutableState<MutableMap<Int, Color>>,
) {
    val modifier = Modifier.fillMaxSize()

    Row(modifier) {
        val generalComponentPadding = 6.dp
        val generalColumnModifier = Modifier.fillMaxHeight()
        val generalComponentsModifier = Modifier.padding(generalComponentPadding)

        val gameFieldColumnModifier = generalColumnModifier.weight(.6f).background(Color(240, 240, 240))
        Column(
            modifier = gameFieldColumnModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GridField(modifier = generalComponentsModifier, config, cells.value)
        }

    }
}

object ColorResolver {
    fun resolveFood(x: Int, y: Int): Color {
        return KELLY_COLORS_FOOD[x * y % KELLY_COLORS_FOOD.size]
    }

    fun resolveSnake(faded: Boolean, id: Int): Color {
        var color = KELLY_COLORS_SNAKES[id % KELLY_COLORS_SNAKES.size]
        val maxVal = Color.White.green
        if (faded) {
            color = Color(
                (color.red + maxVal) / 2,
                (color.green + maxVal) / 2,
                (color.blue + maxVal) / 2,
                1f,
            )
        }
        return color
    }

    private val KELLY_COLORS_SNAKES = arrayOf(
        Color.Red,
        Color.Blue,
        Color.Cyan,
        Color.Green,
    )

    private val KELLY_COLORS_FOOD = arrayOf(
        Color.Yellow,
    )
}