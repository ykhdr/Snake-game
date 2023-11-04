package model.controllers.impl

import model.controllers.GameController
import model.models.Context
import model.models.SteerRequest
import model.models.core.Coord
import model.models.core.Direction
import model.models.core.Snake
import model.models.core.SnakeState
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GameControllerImpl(
    context: Context
) : GameController {

    companion object {
        private const val THREAD_PULL_SIZE = 2
        private const val CREATING_SNAKES_TASK_DELAY = 300L
    }

    private val stateHolder = context.stateHolder

    private val threadExecutor = Executors.newScheduledThreadPool(THREAD_PULL_SIZE)


    private val creatingSnakesTask = {
        if (stateHolder.isGameRunning()) {
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

    init {
        threadExecutor.schedule(
            creatingSnakesTask,
            CREATING_SNAKES_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )
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

    override fun move(address: InetSocketAddress, snake: Snake, direction: Direction) {
        val updatedSnake = Snake(snake.playerId, snake.points, snake.state, direction)

        val steerRequest = SteerRequest(address, direction)

        stateHolder.getStateEditor().setSteerRequest(steerRequest)
        stateHolder.getStateEditor().updateSnake(updatedSnake)
    }
}