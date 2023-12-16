package view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .fillMaxWidth()
            .fillMaxHeight(),
        elevation = 0.dp,
        backgroundColor = background,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, color = Color.LightGray)
    ) {
        val textStyle = TextStyle(fontSize = 14.sp, fontStyle = FontStyle.Normal)
        val padding = 8.dp
        Column {
            Row(Modifier.padding(start = padding, end = padding, top = padding, bottom = padding)) {
                // Username
                Text(
                    text = name,
                    modifier = Modifier
                        .weight(0.3f)
                        .align(Alignment.CenterVertically),
                style = textStyle
                )


                // Size
                Text(
                    text = "${width}x${height}",
                    modifier = Modifier
                        .weight(0.1f)
                        .align(Alignment.CenterVertically),
                style = textStyle
                )

                // Formula
                Text(
                    text = "${alive}x + $foodStatic",
                    modifier = Modifier
                        .weight(0.1f)
                        .align(Alignment.CenterVertically),
                style = textStyle
                )


            }

            Row(Modifier.padding(start = padding, end = padding, top = padding, bottom = padding)) {
                // Address
                Text(
                    text = "${address.hostName}:${address.port}",
                    modifier = Modifier
                        .weight(0.3f)
                        .align(Alignment.CenterVertically),
                    style = textStyle
                )

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
                            enabled = canJoin,

                        ) {
                            Icon(
                                painter = GameIcon.VIEW_GAME.painter(),
                                contentDescription = "watch the game button",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                        IconButton(
                            onClick = { onJoin(address, name) },
                            modifier = Modifier.align(Alignment.CenterVertically),
                            enabled = canJoin
                        ) {
                            Icon(
                                painter = GameIcon.JOIN_GAME.painter(),
                                contentDescription = "join the game button",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
