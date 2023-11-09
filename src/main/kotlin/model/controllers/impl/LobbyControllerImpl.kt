package model.controllers.impl

import model.controllers.LobbyController
import model.models.contexts.Context
import model.models.requests.JoinRequest
import model.models.core.NodeRole
import model.models.requests.ChangeRoleRequest
import java.net.InetSocketAddress
import kotlin.jvm.optionals.getOrElse

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

        val senderId = state.getPlayers().stream()
            .filter { player -> player.role == NodeRole.MASTER }
            .map { p -> p.id }
            .findFirst()


        if (senderId.isEmpty) {
            context.stateHolder.getStateEditor().addError("Master node has not found in game")
        }
//        val leaveRequest = ChangeRoleRequest(}
//        )
//
//        context.stateHolder.getStateEditor().setLeaveRequest(true)

    }

    override fun createGame(gameName: String) {
        // TODO add to state CreateGameRequest()
    }

    override fun setPlayerName(playerName: String) {
        context.stateHolder.getStateEditor().setPlayerName(playerName)
    }
}