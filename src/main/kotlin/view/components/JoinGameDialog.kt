package view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import view.models.JoinConfig
import java.net.InetSocketAddress
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun JoinGameDialog(
    joinConfig: JoinConfig,
    joinGame: (address: InetSocketAddress, playerName: String, gameName: String) -> Unit
){
    val defaultPlayerName = "Player ${UUID.randomUUID().toString().subSequence(1..3)}"
    var playerNameText by remember { mutableStateOf("") }
    var playerNameError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            joinConfig.openDialog.value = false
        },
        title = {
            Text("Подключение к игре")
        },
        text = {
            Column {
                val padding = 6.dp
                val itemModifier = Modifier

                OutlinedTextField(
                    label = {
                        Text(
                            if (!playerNameError) {
                                "Имя игрока"
                            } else {
                                "Не меньше 3 символов"
                            }
                        )
                    },
                    modifier = itemModifier,
                    value = playerNameText,
                    onValueChange = {
                        playerNameError = it.length in 1..3
                        playerNameText = it
                    },
                    placeholder = {
                        Text(text = defaultPlayerName)
                    },
                    isError = playerNameError,
                )
            }

        },
        dismissButton = {
            CancelButton {
                joinConfig.openDialog.value = false
            }
        },
        confirmButton = {
            JoinGameButton{
                if (playerNameText.isEmpty()) {
                    playerNameText = defaultPlayerName
                }

                joinGame(
                    joinConfig.address.get(),
                    playerNameText,
                    joinConfig.gameName.get(),
                )

                joinConfig.openDialog.value = false
            }
        }
    )
}