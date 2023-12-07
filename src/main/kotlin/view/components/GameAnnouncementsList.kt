package view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.controllers.LobbyController
import model.dto.messages.Announcement


@Composable
fun GameAnnouncementsList(lobbyController: LobbyController, modifier: Modifier, announcements: List<Announcement>) {
    Card(
        modifier = modifier,
        elevation = 0.dp,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(0.5.dp, color = Color.LightGray)
    ) {
        val openViewDialog = remember { mutableStateOf(false) }
        val openJoinDialog = remember { mutableStateOf(false) }


        Box {
            val state = rememberLazyListState()
            LazyColumn(modifier = Modifier.fillMaxSize(), state = state, content = {
                items(announcements.size) { index ->
                    val announcement = announcements[index]
                    GameItemBlock(
                        games = announcement.games,
                        address = announcement.address,
                        onView = { address, gameName ->
                            openViewDialog.value = true
                            JoinGameDialog(
                                openViewDialog,
                                address,
                                gameName,
                            ) { watchAddress, playerName, watchGameName ->
                                lobbyController.watch(
                                    watchAddress,
                                    playerName,
                                    watchGameName,
                                )
                            }

                        },
                        onJoin = { address, gameName ->
                            openJoinDialog.value = true
                            if (openViewDialog.value) {

                                JoinGameDialog(
                                    openJoinDialog,
                                    address,
                                    gameName,
                                ) { joinAddress, playerName, joinGameName ->
                                    lobbyController.join(
                                        joinAddress,
                                        playerName,
                                        joinGameName,
                                    )
                                }
                            }
                        },
                        last = index == announcements.size - 1
                    )
                }
            })

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp).fillMaxHeight().width(8.dp),
                adapter = rememberScrollbarAdapter(
                    scrollState = state
                )
            )
        }


    }
}