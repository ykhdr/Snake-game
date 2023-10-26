package model.dto.core

data class GameState(
    val stateOrder: Int,
    val snakes: MutableList<Snake>,
    val foods: MutableList<Coord>,
    val players: GamePlayers
)