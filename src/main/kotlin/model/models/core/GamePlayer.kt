package model.models.core

import java.net.InetSocketAddress

data class GamePlayer(
    val name: String,
    val id: Int,
    var ip: InetSocketAddress = DEFAULT_LOCAL_IP,
    val port: Int = DEFAULT_PORT,
    var role: NodeRole,
    val type: PlayerType = DEFAULT_PLAYER_TYPE,
    val score: Int = DEFAULT_SCORE,
) {
    companion object {
        const val DEFAULT_STR_IP = "127.0.0.1"
        const val DEFAULT_PORT = 1244
        const val DEFAULT_SCORE = 0
        val DEFAULT_PLAYER_TYPE = PlayerType.HUMAN
        val DEFAULT_LOCAL_IP = InetSocketAddress(DEFAULT_PORT)

        val UNKNOWN_PLAYER = GamePlayer("Unkown", 0, role = NodeRole.EMPTY)
    }
}
