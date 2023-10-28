package model.dto.core

data class GameAnnouncement(
    val players: List<GamePlayer>,
    val config: GameConfig,
    val canJoin: Boolean = true,
    val gameName: String,
)
