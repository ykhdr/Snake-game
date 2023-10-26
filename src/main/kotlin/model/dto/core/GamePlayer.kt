package model.dto.core

import java.net.InetSocketAddress

data class GamePlayer(
    val name: String,
    val id: Int,
    val ip: InetSocketAddress,
    val port: Int = DEFAULT_PORT,
    var role: NodeRole,
    val type: PlayerType = DEFAULT_PLAYER_TYPE,
    val score: Int,
) {
    companion object {
        const val DEFAULT_STR_IP = "127.0.0.1"
        const val DEFAULT_PORT = -1
        val DEFAULT_PLAYER_TYPE = PlayerType.HUMAN
    }
}
