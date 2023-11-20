import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import model.client.Client
import view.views.MenuView

fun main() /*= application */{
    val client = Client()

    client.getLobbyController().setPlayerName("hi")


    client.getLobbyController().createGame("A", 10, 10,3,1000)
//    Window(
//        onCloseRequest = ::exitApplication,
//        title = "Snake game"
//    ) {
//        MaterialTheme { MenuView() }
//    }

    Thread.sleep(10000)
    client.close()
}
