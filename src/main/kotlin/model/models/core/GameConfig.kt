package model.models.core

data class GameConfig(
    val width: Int = DEFAULT_WIDTH,
    val height: Int = DEFAULT_HEIGHT,
    val foodStatic: Int = DEFAULT_FOOD_STATIC,
    val stateDelayMs: Int = DEFAULT_STATE_DELAY_MS,
) {
    companion object{
        const val DEFAULT_WIDTH = 40
        const val DEFAULT_HEIGHT = 30
        const val DEFAULT_FOOD_STATIC = 1
        const val DEFAULT_STATE_DELAY_MS = 1000
    }
}