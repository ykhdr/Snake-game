package model.dto.core

data class GamePlayer(
    val name: String,
    val id: Int,
    val ip: String = "",
    val port: Int = 0,
    val role: NodeRole,
    val type: PlayerType = PlayerType.HUMAN,
    val score: Int,
)
