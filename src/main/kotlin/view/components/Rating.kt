package view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.models.core.GamePlayer
import model.models.core.NodeRole

@Composable
fun Rating(
    modifier: Modifier = Modifier,
    selfName: String,
    players: Array<GamePlayer>,
    expandedInfoEnable: Boolean
) {

    val contentPadding = 2.dp

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Рейтинг")
        Card(
            modifier = Modifier.padding(contentPadding),
            backgroundColor = Color(240, 240, 240),
            elevation = 0.dp,
            shape = RoundedCornerShape(12.dp),
        ) {
            LazyColumn(
                modifier = Modifier.padding(4.dp),
                content = {
                    items(players) { player ->
                        PlayerRatingItem(player, selfName, expandedInfoEnable)
                    }
                }
            )
        }
    }
}

@Composable
fun PlayerRatingItem(player: GamePlayer, selfName: String, expandedInfoEnable: Boolean) {
    val isMe = player.name == selfName
    val backgroundColor = if (isMe) {
        Color.LightGray
    } else {
        Color.Transparent
    }
    val textStyle = if (player.role == NodeRole.VIEWER) {
        TextStyle(lineHeight = 10.sp)
    } else {
        TextStyle(lineHeight = 12.sp)
    }
    Card(
        backgroundColor = backgroundColor,
        elevation = 0.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            Modifier
                .padding(4.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                Text(player.name, style = textStyle, modifier = Modifier.align(Alignment.CenterStart))
                if (player.role != NodeRole.VIEWER) {
                    Text(
                        player.score.toString(),
                        style = textStyle,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
            if (expandedInfoEnable) {
                PlayerRatingItemTextExpandable(player)
            }
        }
    }
}

@Composable
fun PlayerRatingItemTextExpandable(player: GamePlayer) {
    val ip = try {
        player.ip
    } catch (e: Exception) {
        null
    }

    Text("${player.ip}:${player.port}")
    Text("NodeRole: ${player.role}")
    Text("PlayerType: ${player.type}")
}

