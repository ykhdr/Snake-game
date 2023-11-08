package model.controllers

import model.models.contexts.Context
import model.models.core.Coord
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

class FieldController(
    context: Context
) {
    private data class FieldSize(
        val width: Int,
        val height: Int,
    )

    private val stateHolder = context.stateHolder

    private var fieldSize = FieldSize(0, 0)

    private var isNewGame = AtomicBoolean(false)

    private val threadExecutor = Executors.newSingleThreadExecutor()

    private val scanFieldTask = {
        while (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()
            val config = state.getConfig()

            if (isNewGame.get()) {
                fieldSize = FieldSize(config.width, config.height)
            }

            val snakeCoords = state.getSnakes().stream().flatMap { snake -> snake.points.stream() }.toList()
            val foodCoords = state.getFoods()

            val availableCoords = findAvailableCoords(snakeCoords, foodCoords)
            //TODO сделать проверку на то когда у нас четыре еды вокруг одной клетки
            stateHolder.getStateEditor().updateAvailableCoords(availableCoords)
        }
    }

    private fun findAvailableCoords(snakesCoords: List<Coord>, foodCoords: List<Coord>): List<Coord> {
        val allCoords = (0 until fieldSize.width).flatMap { x ->
            (0 until fieldSize.height).map { y -> Coord(x, y) }
        }.toMutableList()

        for (snakeCoord in snakesCoords) {
            allCoords.removeAll { coord ->
                (abs(coord.x - snakeCoord.x) <= 2 && abs(coord.y - snakeCoord.y) <= 2)
            }
        }

        allCoords.removeAll { coord -> foodCoords.contains(coord) }

        return allCoords
    }

    fun runScan() {
        isNewGame.set(true)
        threadExecutor.execute(scanFieldTask)
    }

    fun stopScan() {
        threadExecutor.shutdown()
    }
}