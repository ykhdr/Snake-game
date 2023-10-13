package model.dto.core

data class Snake(
    val playerId: Int,
    val points: List<Coord>,
    val state: SnakeState = SnakeState.ALIVE,
    val headDirection: Direction
)