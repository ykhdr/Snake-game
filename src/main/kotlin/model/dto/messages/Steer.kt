package model.dto.messages

import model.models.core.Direction
import java.net.InetSocketAddress


class Steer(
    address: InetSocketAddress,
    senderId : Int,
    receiverId: Int,
    val direction: Direction
) : Message(address, senderId, receiverId)
