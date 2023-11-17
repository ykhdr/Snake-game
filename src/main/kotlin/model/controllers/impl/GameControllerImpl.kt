package model.controllers.impl

import model.controllers.GameController
import model.models.contexts.Context
import model.models.requests.SteerRequest
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

    private val stateHolder = context.stateHolder

    override fun move(address: InetSocketAddress, snake: Snake, direction: Direction) {
        val updatedSnake = Snake(snake.playerId, snake.points, snake.state, direction)

        val steerRequest = SteerRequest(address, direction)

        stateHolder.getStateEditor().setSteerRequest(steerRequest)
        stateHolder.getStateEditor().updateSnake(updatedSnake)
    }
}