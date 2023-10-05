package model.dto.core

data class GameState(
    val stateOrder: Int,
    val snakes: List<Snake>,
    val coords: List<Coord>,
    val players: GamePlayers
)