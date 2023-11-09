package model.models.requests

import model.models.core.NodeRole

data class ChangeRoleRequest(
    private val senderId : Int,
    private val receiverId : Int,
    private val senderRole: NodeRole,
    private val receiverRole: NodeRole
)
