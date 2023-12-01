package model.dto.messages

import java.net.InetSocketAddress

class Error(
    address: InetSocketAddress,
    msgSeq: Long,
    val errorMessage: String,
) : Message(address = address, msgSeq = msgSeq)
