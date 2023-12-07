package view.models

import androidx.compose.runtime.MutableState
import java.net.InetSocketAddress
import java.util.Optional

data class JoinConfig(
    val openDialog : MutableState<Boolean>,
    var address : Optional<InetSocketAddress>,
    var gameName : Optional<String>,
)