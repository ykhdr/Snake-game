package model.dto.messages

import model.dto.core.NodeRole
import model.dto.core.PlayerType
import java.net.InetAddress

class Join(
    address: InetAddress,
    val playerType: PlayerType = PlayerType.HUMAN,
    val playerName: String,
    val gameName: String,
    val requestedRole: NodeRole
) : Message(address)
