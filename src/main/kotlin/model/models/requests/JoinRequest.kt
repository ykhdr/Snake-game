package model.models.requests

import model.models.core.NodeRole
import java.net.InetSocketAddress

data class JoinRequest(
    val address: InetSocketAddress,
    val gameName : String,
    val requestedRole: NodeRole
)