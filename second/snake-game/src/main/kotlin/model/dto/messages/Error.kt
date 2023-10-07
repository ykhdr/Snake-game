package model.dto.messages

import java.net.InetAddress

class Error(
    address: InetAddress,
    val errorMessage: String,
) : Message(address)
