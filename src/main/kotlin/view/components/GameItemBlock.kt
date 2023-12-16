package view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.models.core.GameAnnouncement
import java.net.InetSocketAddress


@Composable
fun GameItemBlock(
    modifier: Modifier = Modifier,
    address: InetSocketAddress,
    games: List<GameAnnouncement>,
    onView: (address: InetSocketAddress, gameName: String) -> Unit,
    onJoin: (address: InetSocketAddress, gameName: String) -> Unit,
    last: Boolean
) {
    Card(
        modifier = modifier
            .padding(start = 6.dp, end = 17.dp, top = 6.dp, bottom = if (last) 6.dp else 0.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column {
            games.forEach { game: GameAnnouncement ->
                GameItem(
                    address = address,
                    width = game.config.width,
                    height = game.config.height,
                    foodStatic = game.config.foodStatic,
                    alive = game.players.size,
                    canJoin = game.canJoin,
                    onView = onView,
                    onJoin = onJoin,
                    name = game.gameName
                )
            }
        }
    }
}