package model.controllers.impl

import model.controllers.GameController
import model.exceptions.NoGameError
import model.models.contexts.Context
import model.models.requests.SteerRequest
import model.models.core.Direction
import mu.KotlinLogging

class GameControllerImpl(
    context: Context
) : GameController {

    private val stateHolder = context.stateHolder

    private val logger = KotlinLogging.logger {}

    override fun move(direction: Direction) {
        runCatching {
            stateHolder.getState().getMasterPlayer()
        }.onSuccess { master ->
            if(stateHolder.getState().getGameAddress() == master.ip){
                stateHolder.getStateEditor().updateSnakeDirection(master.id,direction)
            } else {
                val steerRequest = SteerRequest(master.ip, direction)
                stateHolder.getStateEditor().setSteerRequest(steerRequest)
            }
            logger.info("Player moved snake")
        }.onFailure { e ->
            logger.info("Error on move snake", e)
            throw NoGameError("Error on move snake", e)
        }

    }
}