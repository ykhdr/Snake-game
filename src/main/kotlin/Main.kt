import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.client.Client
import model.models.core.Direction
import view.views.MenuView

fun main() = application {
    val client = Client()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Snake game",
        state = rememberWindowState(width = 1440.dp, height = 1280.dp),
        onKeyEvent = { handleKeyEvent(client, it) }
    ) {
        MaterialTheme { MenuView(client) }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
private fun handleKeyEvent(client: Client, keyEvent: KeyEvent): Boolean {
    if (!client.getState().isGameRunning()) {
        return false
    }

    try {
        client.getGameController().move(
            when (keyEvent.key) {
                Key.W, Key.DirectionUp -> Direction.UP
                Key.D, Key.DirectionRight -> Direction.RIGHT
                Key.S, Key.DirectionDown -> Direction.DOWN
                Key.A, Key.DirectionLeft -> Direction.LEFT
                else -> return false
            }
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return true
}