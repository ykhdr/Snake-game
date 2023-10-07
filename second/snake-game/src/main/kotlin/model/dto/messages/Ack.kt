package model.dto.messages

import java.net.InetAddress

class Ack(
    address: InetAddress,
    msgSeq: Long = DEFAULT_MESSAGE_SEQUENCE,
    receiverId: Int,
    senderId: Int
) : Message(
    address,
    msgSeq = msgSeq,
    receiverId = receiverId,
    senderId = senderId
)