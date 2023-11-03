package model.controllers.impl

import model.controllers.LobbyController
import model.exceptions.NodeRoleHasNotPrivilegesError
import model.models.Context
import model.models.JoinRequest
import model.models.core.NodeRole
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
        TODO("Not yet implemented")
    }

    override fun view(address: InetSocketAddress, playerName: String, gameName: String, requestedRole: NodeRole) {
        TODO("Not yet implemented")
    }

    override fun createGame(gameName: String) {
        TODO("Not yet implemented")
    }

    override fun setPlayerName(playerName: String) {
        context.stateHolder.getStateEditor().setPlayerName(playerName)
    }
}