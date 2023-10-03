import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import view.views.MenuView

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Snake game"
    ) {
        MaterialTheme { MenuView() }
    }
}
