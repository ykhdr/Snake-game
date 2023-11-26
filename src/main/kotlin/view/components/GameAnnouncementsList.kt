package view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import model.client.Client
import model.controllers.LobbyController
import model.dto.messages.Announcement
import model.models.core.GameAnnouncement
import model.models.core.NodeRole
import java.util.*


@Composable
fun GameAnnouncementsList(lobbyController: LobbyController, modifier: Modifier, announcements: List<Announcement>) {
    Card(
        modifier = modifier,
        elevation = 0.dp,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(0.5.dp, color = Color.LightGray)
    ) {
        Box {
//            if (announcements.isEmpty()) {
//                CircularProgressIndicator(
//                    Modifier.size(256.dp).align(Alignment.Center),
//                    strokeWidth = 32.dp,
//                    strokeCap = StrokeCap.Round,
//                    color = Color(225, 225, 225)
//                )
//            }
            val state = rememberLazyListState()
            LazyColumn(modifier = Modifier.fillMaxSize(), state = state, content = {
                items(announcements.size) { index ->
                    val announcement = announcements[index]
                    GameItemBlock(
                        games = announcement.games,
                        address = announcement.address,
                        onView = { address, gameName ->
                            lobbyController.watch(
                                address,
                                gameName,
                            )
                        },
                        onJoin = { address, gameName ->
                            lobbyController.join(
                                address,
                                gameName,
                            )
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