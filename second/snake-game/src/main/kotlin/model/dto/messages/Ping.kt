package model.dto.messages

import java.net.InetAddress

class Ping(
    address: InetAddress,
) : Message(address)
