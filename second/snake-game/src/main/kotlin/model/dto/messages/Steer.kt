package model.dto.messages

import model.dto.core.Direction
import java.net.InetSocketAddress


class Steer(
    address: InetSocketAddress,
    val direction: Direction
) : Message(address)
