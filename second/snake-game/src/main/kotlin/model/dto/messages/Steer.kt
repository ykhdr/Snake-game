package model.dto.messages

import model.dto.core.Direction
import java.net.InetAddress


class Steer(
    address: InetAddress,
    val direction: Direction
) : Message(address)
