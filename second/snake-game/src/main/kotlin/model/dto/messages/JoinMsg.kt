package model.dto.messages

import model.dto.core.NodeRole
import model.dto.core.PlayerType

data class JoinMsg(
    val playerType: PlayerType = PlayerType.HUMAN,
    val playerName: String,
    val gameName: String,
    val requestedRole: NodeRole
) : Msg
