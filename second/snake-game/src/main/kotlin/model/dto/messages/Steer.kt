package model.dto.messages

import model.dto.core.Direction


data class Steer(
    val direction: Direction
) : Message
