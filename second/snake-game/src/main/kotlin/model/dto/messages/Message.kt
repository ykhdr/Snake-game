package model.dto.messages

import java.net.InetAddress

sealed class Message(
    val address: InetAddress,
    val senderId: Int = DEFAULT_SENDER_ID,
    var msgSeq: Long = DEFAULT_MESSAGE_SEQUENCE,
    val receiverId: Int = DEFAULT_RECEIVER_ID,
){
    companion object{
        const val DEFAULT_RECEIVER_ID = -1
        const val DEFAULT_SENDER_ID = -1
        const val DEFAULT_MESSAGE_SEQUENCE = -1L
    }
}