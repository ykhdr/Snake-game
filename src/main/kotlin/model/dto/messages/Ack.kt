package model.dto.messages

import java.net.InetSocketAddress

class Ack(
    address: InetSocketAddress,
    msgSeq: Long = DEFAULT_MESSAGE_SEQUENCE,
    receiverId: Int,
    senderId: Int
) : Message(
    address,
    msgSeq = msgSeq,
    receiverId = receiverId,
    senderId = senderId
)