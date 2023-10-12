package view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gameobjects.Lobby

@Composable
fun Lobbies(modifier: Modifier = Modifier, lobbies: List<Lobby> = listOf()) {
    Box(modifier) {
        Surface(
            border = BorderStroke(1.dp, Color.Black),
            modifier = Modifier.fillMaxSize()
        ) {
            Row {
                LazyColumn(
                    modifier = Modifier
                        .border(BorderStroke(1.dp, color = Color.Gray))
                        .weight(2F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text( textAlign = TextAlign.Center,
                            text="Ведущий")
                    }
                    items(lobbies) { lobby ->
                        Text("${lobby.masterName} [${lobby.masterIp}]")
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .border(BorderStroke(1.dp, color = Color.Gray))
                        .weight(1F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text( textAlign = TextAlign.Center,
                            text="#")
                    }
                    items(lobbies) { lobby ->
                        Text("${lobby.groupSize}")
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .border(BorderStroke(1.dp, color = Color.Gray))
                        .weight(1F),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    item {
                        Text( textAlign = TextAlign.Center,
                            text="Размер")
                    }
                    items(lobbies) { lobby ->
                        Text("${lobby.fieldSize.weight}x${lobby.fieldSize.height}")
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .border(BorderStroke(1.dp, color = Color.Gray))
                        .weight(1F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text( textAlign = TextAlign.Center,
                            text="Еда")
                    }
                    items(lobbies) { lobby ->
                        Text("${lobby.foodStatic}+${lobby.snakesAlive}")
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .border(BorderStroke(1.dp, color = Color.Gray))
                        .weight(1F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            textAlign = TextAlign.Center,
                            text= "Вход")
                    }
                    items(lobbies) { lobby ->
                        // TODO: Сделать здесь IconButton с переходом в лобби 
                        Text("=>")
                    }
                }
            }


        }
    }
}