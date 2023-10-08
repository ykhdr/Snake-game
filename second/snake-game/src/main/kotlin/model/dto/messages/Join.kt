package model.dto.messages

import model.dto.core.NodeRole
import model.dto.core.PlayerType
import java.net.InetSocketAddress

class Join(
    address: InetSocketAddress,
    val playerType: PlayerType = PlayerType.HUMAN,
    val playerName: String,
    val gameName: String,
    val requestedRole: NodeRole
) : Message(address)
