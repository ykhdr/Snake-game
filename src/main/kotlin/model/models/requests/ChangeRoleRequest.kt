package model.models.requests

import model.models.core.NodeRole

data class ChangeRoleRequest(
    val senderId : Int,
    val receiverId : Int,
    val senderRole: NodeRole,
    val receiverRole: NodeRole
)
