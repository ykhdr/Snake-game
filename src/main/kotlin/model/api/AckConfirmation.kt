package model.api

import model.dto.messages.Message

data class AckConfirmation(
    var messageSentTime : Long,
    val message: Message,
)
