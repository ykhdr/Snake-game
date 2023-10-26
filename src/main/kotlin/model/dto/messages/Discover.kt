package model.dto.messages

import java.net.InetSocketAddress

class Discover(
    address: InetSocketAddress,
) : Message(address)