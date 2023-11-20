import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import model.client.Client
import view.views.MenuView

fun main() /*= application */{
    val client = Client()

    client.getLobbyController().setPlayerName("hi")

//    Window(
//        onCloseRequest = ::exitApplication,
//        title = "Snake game"
//    ) {
//        MaterialTheme { MenuView() }
//    }

    Thread.sleep(1000)
    client.close()
}
