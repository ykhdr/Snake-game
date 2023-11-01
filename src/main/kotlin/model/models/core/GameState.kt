package model.models.core

data class GameState(
    val stateOrder: Int,
    val snakes: List<Snake>,
    val foods: List<Coord>,
    val players: List<GamePlayer>
)