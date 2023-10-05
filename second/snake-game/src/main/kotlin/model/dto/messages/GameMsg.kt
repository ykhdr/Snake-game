package model.dto.messages

data class GameMessage(
    val msgSeq: Long,
    val senderId: Int = -1,
    val receiverId: Int = -1,
    val type: Msg
)
