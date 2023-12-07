package model.models.requests

import model.models.core.NodeRole
import java.net.InetSocketAddress

data class JoinRequest(
    val address: InetSocketAddress,
    val gameName : String,
    val playerName : String,
    val requestedRole: NodeRole
)