package model.controllers.impl

import model.controllers.FieldController
import model.controllers.GameController
import model.dto.core.Coord
import model.dto.core.Direction
import model.dto.core.Snake
import model.dto.core.SnakeState
import model.models.Context
import kotlin.random.Random

class GameControllerImpl(
    context: Context
) : GameController {

    private val stateHolder = context.stateHolder;
    private val fieldController = FieldController(context)


    //TODO некоторая scheduled task
    private val creatingSnakesTask = {
        while (stateHolder.isGameRunning()) {
            val state = stateHolder.getState()
            val playersToAdding = state.getPlayersToAdding()
            val availableCoords = state.getAvailableCoords().toMutableList()
            while (playersToAdding.isNotEmpty() && availableCoords.isNotEmpty()) {
                val player = playersToAdding.poll()
                val headCoord = availableCoords.first ?: continue
                val bodyCoord = getRandomSecondSnakeCoord(headCoord)
                val snake = Snake(
                    player.id,
                    listOf(headCoord, bodyCoord),
                    SnakeState.ALIVE,
                    getHeadDirection(headCoord, bodyCoord)
                )

                stateHolder.getStateEditor().addSnake(snake)
                availableCoords.remove(headCoord)
            }
        }
    }

    //TODO сделать проверку на координаты соседние
    private fun getRandomSecondSnakeCoord(headCoord: Coord): Coord {
        return when (Random.nextInt(0, 4)) {
            0 -> Coord(headCoord.x - 1, headCoord.y)
            1 -> Coord(headCoord.x + 1, headCoord.y)
            2 -> Coord(headCoord.x, headCoord.y + 1)
            3 -> Coord(headCoord.x, headCoord.y - 1)
            else -> Coord(-1, -1)
        }
    }

    private fun getHeadDirection(headCoord: Coord, bodyCoord: Coord): Direction {
        return if (bodyCoord.x > headCoord.x) {
            Direction.LEFT
        } else if (bodyCoord.x < headCoord.x) {
            Direction.RIGHT
        } else if (bodyCoord.y > headCoord.y) {
            Direction.DOWN
        } else {
            Direction.UP
        }
    }
}