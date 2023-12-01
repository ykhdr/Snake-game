package model.models.util

import java.net.InetSocketAddress

data class MessageInfo(
    val address: InetSocketAddress,
    val msgSequence: Long
)