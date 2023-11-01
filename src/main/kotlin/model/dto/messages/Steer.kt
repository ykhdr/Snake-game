package model.dto.messages

import model.models.core.Direction
import java.net.InetSocketAddress


class Steer(
    address: InetSocketAddress,
    val direction: Direction
) : Message(address)
