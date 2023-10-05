package model.dto.core

data class GameConfig(
    val width: Int = 40,
    val height: Int = 30,
    val foodStatic: Int = 1,
    val stateDelayMs: Int = 1000,
)