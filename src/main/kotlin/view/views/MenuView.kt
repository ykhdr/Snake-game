package view.views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import model.client.Client
import model.models.core.Direction
import model.models.core.GameAnnouncement
import model.models.core.NodeRole
import view.components.GameStartButton
import view.components.GameStartDialog
import view.components.LeaveButton
import java.net.InetSocketAddress

@Preview
@Composable
fun MenuView(client: Client) = Surface(
    color = MaterialTheme.colors.surface,
    modifier = Modifier
        .fillMaxSize()
) {
    val openDialog = remember { mutableStateOf(false) }
    val isGameRunning = remember { mutableStateOf(false) }
    val announcements = remember { mutableStateOf(mapOf<InetSocketAddress, GameAnnouncement>()) }

    LaunchedEffect(Unit) {
        client.setOnStateEditListener { state ->
            isGameRunning.value = state.isGameRunning()
            announcements.value = state.getAnnouncements()
        }
    }

    if (openDialog.value) {
        GameStartDialog(
            openDialog,
            client.getLobbyController()::createGame
        )
    }

    Row(
//        horizontalArrangement = Arrangement.spacedBy(5.dp),
//        modifier = Modifier.padding(5.dp)
    ) {

        Column(
            modifier = Modifier.weight(8f)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                backgroundColor = if (isGameRunning.value) Color.Cyan else Color.Yellow
            ) {
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
                    onClick = { openDialog.value = true }
                )

                LeaveButton(
                    modifier = Modifier.weight(1f),
                    onClick = { client.getLobbyController().leave() }
                )

            }


            Box(
                modifier = Modifier.weight(2.5f)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = Color.Yellow
                ) {

                }
            }

            Box(
                modifier = Modifier.weight(4f)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = Color.Transparent
                ) {

                }
            }

            Box(
                modifier = Modifier.weight(7f)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = Color.White
                ) {
                    Lobbies(announcements = announcements.value)
                }
            }

        }


//        Column(
//            verticalArrangement = Arrangement.spacedBy(1.dp),
//            modifier = Modifier.weight(2F)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1F)
//                    .padding(5.dp),
//                horizontalArrangement = Arrangement.Center,
//            ) {
//
//                Box(Modifier.fillMaxSize()) {
//                    OutlinedButton(
//                        onClick = { client.getLobbyController().createGame("a", 10, 10, 1, 1000) },
//                        border = BorderStroke(width = 1.dp, color = Color.Gray),
//                        modifier = Modifier.align(Alignment.Center)
//                    ) {
//                        Text(
//                            textAlign = TextAlign.Center,
//                            text = "Leave Game"
//                        )
//                    }
//                }
//                Card(){
//
//                }

//                LeaveButton(
//                    Modifier
//                        .fillMaxSize()
//                        .weight(2F)
//                        .padding(5.dp)
//                ) {
//
//                }
//
//                NewGameButton(
//                    Modifier
//                        .fillMaxSize()
//                        .weight(2F)
//                        .padding(5.dp)
//                ) {
//                }
//            }
//
//            Box(modifier = Modifier.fillMaxHeight().weight(6F)) {
//                Lobbies()
//            }
//            }
//        }
    }


}


