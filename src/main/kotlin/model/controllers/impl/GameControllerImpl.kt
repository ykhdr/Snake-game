package model.controllers.impl

import model.controllers.GameController
import model.models.contexts.Context
import model.models.requests.SteerRequest
import model.models.core.Direction
import model.models.core.Snake
import mu.KotlinLogging
import java.net.InetSocketAddress

class GameControllerImpl(
    context: Context
) : GameController {

    private val stateHolder = context.stateHolder

    private val logger = KotlinLogging.logger {}

    override fun move(address: InetSocketAddress, direction: Direction) {
        val steerRequest = SteerRequest(address, direction)

        stateHolder.getStateEditor().setSteerRequest(steerRequest)
        logger.info("Player moved snake")
    }
}