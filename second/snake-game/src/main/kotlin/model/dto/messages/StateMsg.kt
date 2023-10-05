package model.dto.messages

import model.dto.core.GameState

data class StateMsg(
    val state: GameState
) : Msg
