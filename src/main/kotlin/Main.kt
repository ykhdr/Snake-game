import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import model.client.Client
import model.models.core.Direction
import model.models.core.NodeRole
import view.views.MenuView

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Snake game"
    ) {
        MaterialTheme { MenuView() }
    }
}
