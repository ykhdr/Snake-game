package model.models.util

import model.dto.messages.Message

data class MessageTimestamp(
    var messageSentTime : Long,
    val message: Message,
)
