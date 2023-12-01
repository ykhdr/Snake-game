package model.dto.messages

import model.models.core.Direction
import java.net.InetSocketAddress


class Steer(
    address: InetSocketAddress,
    senderId : Int,
    receiverId: Int,
    msgSeq : Long,
    val direction: Direction
) : Message(address, senderId, receiverId, msgSeq)
