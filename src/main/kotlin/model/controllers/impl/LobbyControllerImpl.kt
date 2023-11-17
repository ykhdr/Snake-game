package model.controllers.impl

import model.controllers.LobbyController
import model.models.contexts.Context
import model.models.core.GameConfig
import model.models.requests.JoinRequest
import model.models.core.NodeRole
import model.models.requests.ChangeRoleRequest
import model.models.requests.GameCreateRequest
import java.net.InetSocketAddress

class LobbyControllerImpl(
    private val context: Context
) : LobbyController {
    override fun join(address: InetSocketAddress, gameName: String, requestedRole: NodeRole) {
        if (requestedRole == NodeRole.MASTER || requestedRole == NodeRole.DEPUTY) {
            context.stateHolder.getStateEditor()
                .addError("Player has not privilage to request role ${requestedRole.name}")
        } else {
            val joinRequest = JoinRequest(address, requestedRole)
            context.stateHolder.getStateEditor().setJoinRequest(joinRequest)
        }

    }

    override fun leave() {
        val state = context.stateHolder.getState()

        runCatching {
            val receiverPlayer = state.getMasterPlayer()

            runCatching {
                val senderPlayer = state.getCurNodePlayer()

                val leaveRequest = ChangeRoleRequest(
                    senderPlayer.id,
                    receiverPlayer.id,
                    senderPlayer.role,
                    receiverPlayer.role
                )

                context.stateHolder.getStateEditor().setLeaveRequest(leaveRequest)

            }.onFailure { e ->
                throw e
            }

        }.onFailure { e ->
            e.message?.let { context.stateHolder.getStateEditor().addError(it) }
        }
    }

    override fun createGame(gameName: String, width: Int, height: Int, foodStatic: Int, stateDelay: Int) {
        val gameConfig = GameConfig(width, height, foodStatic, stateDelay)
        val gameCreateRequest = GameCreateRequest(gameName, gameConfig)

        context.stateHolder.getStateEditor().setGameCreateRequest(gameCreateRequest)
    }

    override fun setPlayerName(playerName: String) {
        context.stateHolder.getStateEditor().setPlayerName(playerName)
    }
}