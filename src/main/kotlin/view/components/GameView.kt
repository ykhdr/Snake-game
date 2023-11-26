package view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import model.client.Client
import model.controllers.GameController
import model.models.core.GameAnnouncement
import model.models.core.GameConfig
import model.models.core.GamePlayer
import model.states.ClientState
import model.states.State
import java.net.InetSocketAddress

@Composable
fun GameView(
    client: Client,
    config: GameConfig,
    playersState: MutableState<Array<GamePlayer>>,
    fadedColorsEnable: MutableState<Boolean>,
    cells: MutableState<MutableMap<Int, Color>>,
    isGameRunning: MutableState<Boolean>,
    announcements: MutableState<Map<InetSocketAddress, GameAnnouncement>>
) {
    val modifier = Modifier.fillMaxSize()

        Row(modifier) {
            val generalComponentPadding = 6.dp
            val generalColumnModifier = Modifier.fillMaxHeight()
            val generalComponentsModifier = Modifier.padding(generalComponentPadding)

//                Column(modifier = generalColumnModifier.weight(.2f)) {
//                    GameInfoSection(client, config, generalComponentsModifier, playersState, expandedInfoEnable)
//                }

                val gameFieldColumnModifier = generalColumnModifier.weight(.6f).background(Color(240, 240, 240))
                Column(
                    modifier = gameFieldColumnModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GridField(modifier = generalComponentsModifier, config, cells.value)
                }

//            val gameSettingsColumnModifier = generalColumnModifier.weight(.2f)
//            Column(modifier = gameSettingsColumnModifier) {
//                GameSettingsSection(generalComponentsModifier, fadedColorsEnable, expandedInfoEnable, client)
//            }

//        }
    }

}

@Composable
private fun GameInfoSection(
    client: Client,
    config: GameConfig,
    modifier: Modifier,
    playersState: MutableState<Array<GamePlayer>>,
    expandedInfoEnable: MutableState<Boolean>
) {
    Column(modifier) {
//        Logo(modifier)
        Column(
            Modifier.fillMaxHeight(0.9f),
            verticalArrangement = Arrangement.Center
        ) {
            Rating(modifier, client.getState().getCurNodePlayer().name, playersState.value, expandedInfoEnable.value)
            Stats(modifier, config.height, config.width, config.foodStatic, config.stateDelayMs)
        }
    }
}

@Composable
private fun GameSettingsSection(
    modifier: Modifier,
    fadedColorsEnable: MutableState<Boolean>,
    expandedInfoEnable: MutableState<Boolean>,
    client: Client
) {
    LeaveButton(modifier) { (client.getLobbyController().leave()) }
//    Column(
//        Modifier.fillMaxHeight(0.9f),
//        verticalArrangement = Arrangement.Center
//    ) {
//        GameSettings(modifier = modifier) { selected, isChecked ->
//            when (selected) {
//                GameOption.EXPANDED_INFORMATION_ABOUT_PARTICIPANTS -> {
//                    expandedInfoEnable.value = isChecked
//                }
//
//                GameOption.OPPONENTS_FADED_COLORS -> {
//                    fadedColorsEnable.value = isChecked
//                }
//            }
//        }
//    }
}

//@Composable
fun observeGameState(
    client: Client,
    config: MutableState<GameConfig>,
    playersState: MutableState<Array<GamePlayer>>,
    fadedColorsEnable: MutableState<Boolean>,
    cells: MutableState<MutableMap<Int, Color>>,
    isGameRunning: MutableState<Boolean>,
    announcements: MutableState<Map<InetSocketAddress, GameAnnouncement>>
) {
//    LaunchedEffect(Unit) {
        client.setOnStateEditListener { state: ClientState ->
            isGameRunning.value = state.isGameRunning()
            announcements.value = state.getAnnouncements()

            if (isGameRunning.value){
                config.value = state.getConfig()
                val cellsTmp = mutableMapOf<Int, Color>()
                state.getSnakes().forEach { snake ->
                    snake.points.forEach { coords ->
                        cellsTmp[config.value.width * coords.y + coords.x] = ColorResolver.resolveSnake(
                            fadedColorsEnable.value && state.getCurNodePlayer().id != snake.playerId,
                            snake.playerId
                        )
                    }
                }

                state.getFoods().forEach { food ->
                    cellsTmp[config.value.width * food.y + food.x] = ColorResolver.resolveFood(food.x, food.y)
                }

                playersState.value = state.getPlayers().toTypedArray()
                cells.value.clear()
                cells.value = cellsTmp
            }
        }

//    }

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