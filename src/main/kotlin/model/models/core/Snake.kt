package model.models.core

data class Snake(
    val playerId: Int,
    val points: List<Coord>,
    var state: SnakeState = SnakeState.ALIVE,
    var headDirection: Direction
)