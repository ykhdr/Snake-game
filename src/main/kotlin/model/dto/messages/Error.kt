package model.dto.messages

import java.net.InetSocketAddress

class Error(
    address: InetSocketAddress,
    val errorMessage: String,
) : Message(address)
