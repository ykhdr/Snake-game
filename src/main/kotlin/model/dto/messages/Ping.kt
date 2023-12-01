package model.dto.messages

import java.net.InetSocketAddress

class Ping(
    address: InetSocketAddress,
    msgSeq: Long,
) : Message(address, msgSeq = msgSeq)
