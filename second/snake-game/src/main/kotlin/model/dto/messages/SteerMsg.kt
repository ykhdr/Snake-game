package model.dto.messages

import model.dto.core.Direction


data class SteerMsg(
    val direction: Direction
) : Msg
