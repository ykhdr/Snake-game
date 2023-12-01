package model.dto.messages

import model.models.core.GameState
import java.net.InetSocketAddress

class State(
    address: InetSocketAddress,
    senderId: Int,
    receiverId: Int,
    msgSeq : Long,
    val state: GameState
) : Message(address, senderId, receiverId, msgSeq)
