package model.controllers.impl

import model.controllers.LobbyController
import model.models.contexts.Context
import model.models.core.GameConfig
import model.models.requests.JoinRequest
import model.models.core.NodeRole
import model.models.requests.ChangeRoleRequest
import model.models.requests.GameCreateRequest
import mu.KotlinLogging
import java.net.InetSocketAddress

class LobbyControllerImpl(
    private val context: Context
) : LobbyController {

    private val logger = KotlinLogging.logger {}

    override fun join(address: InetSocketAddress, gameName: String) {
        val joinRequest = JoinRequest(address, NodeRole.NORMAL)
        context.stateHolder.getStateEditor().setJoinRequest(joinRequest)
        logger.info("Player with address ${address.address.hostAddress} sent join request")
    }

    override fun watch(address: InetSocketAddress, gameName: String) {
        val joinRequest = JoinRequest(address, NodeRole.VIEWER)
        context.stateHolder.getStateEditor().setJoinRequest(joinRequest)
        logger.info("Player with address ${address.address.hostAddress} sent join on watch request")
    }

    override fun leave() {
        val state = context.stateHolder.getState()

        runCatching {
            val receiverPlayer = state.getMasterPlayer()

            runCatching {
                val senderPlayer = state.getCurNodePlayer()

                if (state.getMasterPlayer() == senderPlayer) {
                    context.stateHolder.getStateEditor().setNodeRole(NodeRole.VIEWER)
                    logger.info("Current node player leave game")
                } else {
                    val leaveRequest = ChangeRoleRequest(
                        senderPlayer.id,
                        receiverPlayer.id,
                        senderPlayer.role,
                        receiverPlayer.role
                    )
                    context.stateHolder.getStateEditor().setLeaveRequest(leaveRequest)
                    logger.info("Player ${senderPlayer.id} sent leave request to master ${receiverPlayer.id}")
                }

            }.onFailure { e ->
                throw e
            }

        }.onFailure { e ->
            e.message?.let { context.stateHolder.getStateEditor().addError(it) }
            logger.warn("Player can not send leave request", e)
        }
    }

    override fun createGame(
        playerName: String,
        gameName: String,
        width: Int,
        height: Int,
        foodStatic: Int,
        stateDelay: Int
    ) {
        val gameConfig = GameConfig(width, height, foodStatic, stateDelay)
        val gameCreateRequest = GameCreateRequest(gameName, gameConfig)

        context.stateHolder.getStateEditor().setPlayerName(playerName)
        context.stateHolder.getStateEditor().setGameCreateRequest(gameCreateRequest)
        logger.info("Player create game create request")
    }
}