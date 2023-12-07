package model.controllers.impl

import model.controllers.LobbyController
import model.dto.messages.Announcement
import model.models.contexts.Context
import model.models.core.GameConfig
import model.models.requests.JoinRequest
import model.models.core.NodeRole
import model.models.requests.ChangeRoleRequest
import model.models.requests.GameCreateRequest
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.util.Optional

class LobbyControllerImpl(
    private val context: Context
) : LobbyController {

    private val logger = KotlinLogging.logger {}

    override fun join(address: InetSocketAddress, playerName: String, gameName: String) {
        val joinRequest = JoinRequest(address, playerName, gameName, NodeRole.NORMAL)
        connect2Host(joinRequest)
    }

    override fun view(address: InetSocketAddress, playerName: String, gameName: String) {
        val joinRequest = JoinRequest(address, playerName, gameName, NodeRole.VIEWER)
        connect2Host(joinRequest)
    }

    override fun leave() {
        val state = context.stateHolder.getState()

        runCatching {
            state.getMasterPlayer()
        }.onSuccess { receiverPlayer ->
            runCatching {
                state.getCurNodePlayer()
            }.onSuccess { senderPlayer ->
                val leaveRequest = ChangeRoleRequest(
                    senderPlayer.id,
                    receiverPlayer.id,
                    NodeRole.VIEWER,
                    NodeRole.MASTER
                )
                context.stateHolder.getStateEditor().setLeaveRequest(leaveRequest)
                logger.info("Player ${senderPlayer.id} sent leave request to master ${receiverPlayer.id}")

            }.onFailure { e ->
                logger.warn("Player can not send leave request", e)
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

    private fun connect2Host(joinRequest: JoinRequest) {
        val announcement = findAnnouncement(joinRequest.address)
        if (announcement.isPresent) {
            val ann = announcement.get()
            context.stateHolder.getStateEditor().setGameConfig(ann.games[0].config)

            context.stateHolder.getStateEditor().setJoinRequest(joinRequest)
            logger.info("Player with address ${joinRequest.address.address.hostAddress} sent join request")
        }
    }

    private fun findAnnouncement(address: InetSocketAddress): Optional<Announcement> {
        val state = context.stateHolder.getState()
        val announcement = state.getAnnouncements()
            .stream()
            .filter { a -> a.address == address }
            .findFirst()

        if (announcement.isEmpty) {
            logger.warn("Announcement with address $address has no found")
        }

        return announcement
    }
}