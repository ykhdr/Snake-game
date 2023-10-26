package model.dto.core

data class GameAnnouncement(
    val players: GamePlayers,
    val config: GameConfig,
    val canJoin: Boolean = true,
    val gameName: String,
)
