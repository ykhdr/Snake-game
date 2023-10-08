package model.dto.core

data class GamePlayer(
    val name: String,
    val id: Int,
    val ip: String = DEFAULT_IP,
    val port: Int = DEFAULT_PORT,
    val role: NodeRole,
    val type: PlayerType = DEFAULT_PLAYER_TYPE,
    val score: Int,
) {
    companion object {
        const val DEFAULT_IP = ""
        const val DEFAULT_PORT = -1
        val DEFAULT_PLAYER_TYPE = PlayerType.HUMAN
    }
}
