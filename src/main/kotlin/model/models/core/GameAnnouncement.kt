package model.models.core

data class GameAnnouncement(
    val players: List<GamePlayer>,
    val config: GameConfig,
    val canJoin: Boolean = true,
    val gameName: String,
)
