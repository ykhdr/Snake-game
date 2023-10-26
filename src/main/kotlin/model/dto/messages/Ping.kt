package model.dto.messages

import java.net.InetSocketAddress

class Ping(
    address: InetSocketAddress,
) : Message(address)
