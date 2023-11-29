package model.dto.messages

import model.models.core.GameState
import java.net.InetSocketAddress

class State(
    address: InetSocketAddress,
    senderId: Int,
    receiverId: Int,
    val state: GameState
) : Message(address, senderId, 0, receiverId)
