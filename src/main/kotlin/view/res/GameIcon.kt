package view.res

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

enum class GameIcon(path: String) {
    HEIGHT("/icons/arrow_down_up.svg"),
    WIDTH("/icons/arrow_left_right.svg"),
    DELAY("/icons/timer.svg"),
    FOOD("/icons/apple.svg"),
    VIEW_GAME("/icons/watch.svg"),
    JOIN_GAME("/icons/login.svg");

    private val content: String

    init {
        javaClass.getResource(path) ?: throw IllegalStateException("${this.name} icon resource must not be null")
        this.content = path
    }

    @Composable
    fun painter(): Painter {
        return painterResource(content)
    }
}