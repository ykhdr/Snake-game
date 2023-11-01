package model.dto.messages

import model.models.core.GameState
import java.net.InetSocketAddress

class State(
    address: InetSocketAddress,
    val state: GameState
) : Message(address)
