package model.dto.messages

import model.models.core.NodeRole
import model.models.core.PlayerType
import java.net.InetSocketAddress

class Join(
    address: InetSocketAddress,
    val playerType: PlayerType = PlayerType.HUMAN,
    val playerName: String,
    val gameName: String,
    val requestedRole: NodeRole
) : Message(address)
