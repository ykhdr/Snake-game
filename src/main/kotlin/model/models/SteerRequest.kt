package model.models

import model.models.core.Direction
import java.net.InetSocketAddress

data class SteerRequest(
    val address: InetSocketAddress,
    val direction: Direction
)
