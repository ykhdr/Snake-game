package view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import view.res.GameIcon
import java.net.InetSocketAddress

@Composable
fun GameItem(
    modifier: Modifier = Modifier,
    address: InetSocketAddress,
    width: Int,
    height: Int,
    foodStatic: Int,
    alive: Int,
    canJoin: Boolean,
    background: Color = Color.White,
    onView: (address: InetSocketAddress, gameName: String) -> Unit,
    onJoin: (address: InetSocketAddress, gameName: String) -> Unit,
    name: String
) {
    Card(
        modifier = modifier
            .padding(start = 6.dp, end = 6.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
        backgroundColor = background,
        shape = RoundedCornerShape(12.dp),
    ) {
//        val textStyle = Font.snakeIOTypography.body1
        val padding = 8.dp

        Row(Modifier.padding(start = padding, end = padding, top = padding, bottom = padding)) {
            // Username
            Text(
                text = "username",
                modifier = Modifier
                    .weight(0.3f)
                    .align(Alignment.CenterVertically),
//                style = textStyle
            )

            // Address
            Text(
                text = "${address.hostName}:${address.port}",
                modifier = Modifier
                    .weight(0.3f)
                    .align(Alignment.CenterVertically),
//                style = textStyle
            )

            // Size
            Text(
                text = "${width}x${height}",
                modifier = Modifier
                    .weight(0.1f)
                    .align(Alignment.CenterVertically),
//                style = textStyle
            )

            // Formula
            Text(
                text = "${alive}x + $foodStatic",
                modifier = Modifier
                    .weight(0.1f)
                    .align(Alignment.CenterVertically),
//                style = textStyle
            )

            // Buttons
            Box(
                modifier = Modifier
                    .weight(0.2f)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(
                        onClick = { onView(address, name) },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        enabled = canJoin
                    ) {
                        Icon(
                            painter = GameIcon.VIEW_GAME.painter(),
                            contentDescription = "watch the game button"
                        )
                    }
                    IconButton(
                        onClick = { onJoin(address, name) },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        enabled = canJoin
                    ) {
                        Icon(
                            painter = GameIcon.JOIN_GAME.painter(),
                            contentDescription = "join the game button"
                        )
                    }
                }
            }
        }
    }
}
