package model.dto.messages

import java.net.InetAddress

class Discover(
    address: InetAddress,
) : Message(address)