package model.models

import model.models.core.NodeRole
import java.net.InetSocketAddress

data class JoinRequest(
    val address: InetSocketAddress,
    val requestedRole: NodeRole
)