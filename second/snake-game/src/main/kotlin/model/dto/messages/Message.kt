package model.dto.messages

import java.net.InetAddress

open class Message(
    open val address: InetAddress,
    open val senderId: Int = DEFAULT_SENDER_ID,
    open val msgSeq: Long = DEFAULT_MESSAGE_SEQUENCE,
    open val receiverId: Int = DEFAULT_RECEIVER_ID,
){
    companion object{
        const val DEFAULT_RECEIVER_ID = -1
        const val DEFAULT_SENDER_ID = -1
        const val DEFAULT_MESSAGE_SEQUENCE = -1L
    }
}