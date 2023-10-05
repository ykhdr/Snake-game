package model.dto.messages

import model.dto.core.NodeRole

data class RoleChangeMsg(
    val senderRole: NodeRole = NodeRole.EMPTY,
    val receiverRole: NodeRole = NodeRole.EMPTY
) : Msg
