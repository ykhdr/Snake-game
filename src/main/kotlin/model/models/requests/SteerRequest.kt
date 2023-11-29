package model.models.requests

import model.models.core.Direction
import java.net.InetSocketAddress

data class SteerRequest(
    val address: InetSocketAddress,
    val senderId : Int,
    val receiverId : Int,
    val direction: Direction
)
