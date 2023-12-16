package model.controllers.impl

import model.controllers.GameController
import model.exceptions.NoGameError
import model.models.contexts.Context
import model.models.core.Direction
import model.models.requests.SteerRequest
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
            runCatching {
                stateHolder.getState().getCurNodePlayer()
            }.onSuccess { player ->
                if (player.ip == master.ip) {
                    stateHolder.getStateEditor().updateSnakeDirection(master.id, direction)
                } else {
                    val gameAddress = stateHolder.getState().getGameAddress()
                    val steerRequest = SteerRequest(gameAddress, player.id, master.id, direction)
                    stateHolder.getStateEditor().setSteerRequest(steerRequest)
                }
                logger.info("Player moved snake")
            }.onFailure { e ->
                throw e
            }
        }.onFailure { e ->
            logger.info("Error on move snake", e)
            throw NoGameError("Error on move snake", e)
        }

    }
}