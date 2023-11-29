package model.dto.messages

import java.net.InetSocketAddress

sealed class Message(
    open val address: InetSocketAddress,
    open val senderId: Int = DEFAULT_SENDER_ID,
    open val receiverId: Int = DEFAULT_RECEIVER_ID,
    open val msgSeq: Long = DEFAULT_MESSAGE_SEQUENCE,
){
    companion object{
        const val DEFAULT_RECEIVER_ID = -1
        const val DEFAULT_SENDER_ID = -1
        const val DEFAULT_MESSAGE_SEQUENCE = -1L
    }

    override fun toString(): String {
        return "Message(address=$address, senderId=$senderId, receiverId=$receiverId, msgSeq=$msgSeq)"
    }
}