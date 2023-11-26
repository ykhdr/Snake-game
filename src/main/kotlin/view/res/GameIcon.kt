package view.res

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

enum class GameIcon(path: String) {
    HEIGHT("/icons/height_FILL0_wght400_GRAD0_opsz24.svg"),
    WIDTH("/icons/width_FILL0_wght400_GRAD0_opsz24.svg"),
    DELAY("/icons/timer_FILL0_wght400_GRAD0_opsz24.svg"),
    FOOD("/icons/lunch_dining_FILL0_wght400_GRAD0_opsz24.svg"),
    LOGO("/icons/stream_FILL0_wght400_GRAD0_opsz24.svg"),
    VIEW_GAME("/icons/visibility_FILL0_wght400_GRAD0_opsz24.svg"),
    JOIN_GAME("/icons/login_FILL0_wght400_GRAD0_opsz24.svg");

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