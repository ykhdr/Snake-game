package model.dto.messages

import model.dto.core.GameState
import java.net.InetAddress

class State(
    address: InetAddress,
    val state: GameState
) : Message(address)
