package view.views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.client.Client
import model.dto.messages.Announcement
import model.models.core.*
import model.states.ClientState
import view.components.*
import view.models.JoinConfig
import java.util.*

@Preview
@Composable
fun MenuView(client: Client) = Surface(
    color = MaterialTheme.colors.surface,
    modifier = Modifier
        .fillMaxSize()
) {
    val openGameStartDialog = remember { mutableStateOf(false) }
    val joinGameConfig =
        remember { mutableStateOf(JoinConfig(mutableStateOf(false), Optional.empty(), Optional.empty())) }

    val viewGameConfig =
        remember { mutableStateOf(JoinConfig(mutableStateOf(false), Optional.empty(), Optional.empty())) }


    val isGameRunning = remember { mutableStateOf(false) }
    val announcements = remember { mutableStateOf(listOf<Announcement>()) }
    val config = remember { mutableStateOf(GameConfig()) }

    val curNodePlayer = remember { mutableStateOf(arrayOf<GamePlayer>()) }
    val playersState = remember { mutableStateOf(arrayOf<GamePlayer>()) }
    val fadedColorsEnable = remember { mutableStateOf(true) }
    val cells = remember { mutableStateOf(mutableMapOf<Int, Color>()) }
    val expandedInfoEnable = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        client.setOnStateEditListener { state: ClientState ->
            isGameRunning.value = state.isGameRunning()
            announcements.value = state.getAnnouncements()

            if (isGameRunning.value) {
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
                if (state.getPlayers().isNotEmpty()) {
                    curNodePlayer.value = arrayOf(state.getCurNodePlayer())
                }
                cells.value = cellsTmp
            }
        }
    }

    if (viewGameConfig.value.openDialog.value){
        JoinGameDialog(
            viewGameConfig.value,
            client.getLobbyController()::view
        )
    }


    if (joinGameConfig.value.openDialog.value){
        JoinGameDialog(
            joinGameConfig.value,
            client.getLobbyController()::join
        )
    }

    if (openGameStartDialog.value) {
        GameStartDialog(
            openGameStartDialog,
            client.getLobbyController()::createGame
        )
    }

    Row {

        Column(
            modifier = Modifier.weight(8f),

            ) {
            if (isGameRunning.value) {
                GameView(config.value, cells)
            }
        }

        Column(
            modifier = Modifier.weight(2f)
        ) {
            Row(
                modifier = Modifier
                    .weight(1.5f)
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                GameStartButton(
                    modifier = Modifier.weight(1f),
                    onClick = { openGameStartDialog.value = true },
                    isEnabled = !isGameRunning.value
                )

                LeaveButton(
                    modifier = Modifier.weight(1f),
                    onClick = { client.getLobbyController().leave() },
                    isEnabled = isGameRunning.value
                )

            }


            Box(
                modifier = Modifier.weight(2.5f)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    border = BorderStroke(1.dp, color = Color.LightGray),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(15.dp),
                ) {
                    if (isGameRunning.value) {
                        Stats(
                            modifier = Modifier,
                            config.value.height,
                            config.value.width,
                            config.value.foodStatic,
                            config.value.stateDelayMs
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.weight(4f)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    border = BorderStroke(1.dp, color = Color.LightGray),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(15.dp),
                ) {
                    if (isGameRunning.value && curNodePlayer.value.isNotEmpty()) {
                        Rating(
                            modifier = Modifier,
                            curNodePlayer.value[0].name,
                            playersState.value,
                            expandedInfoEnable.value
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.weight(7f)
            ) {
                GameAnnouncementsList(
                    joinGameConfig.value,
                    viewGameConfig.value,
                    Modifier,
                    announcements.value,
                )
            }

        }

    }


}


