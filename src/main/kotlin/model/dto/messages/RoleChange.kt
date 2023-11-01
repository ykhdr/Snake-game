package model.dto.messages

import model.models.core.NodeRole
import java.net.InetSocketAddress

class RoleChange(
    address: InetSocketAddress,
    senderId: Int,
    receiverId: Int,
    val senderRole: NodeRole = NodeRole.EMPTY,
    val receiverRole: NodeRole = NodeRole.EMPTY
) : Message(
    address,
    senderId = senderId,
    receiverId = receiverId
)
