package model.controllers

import model.models.core.NodeRole
import java.net.InetSocketAddress

interface LobbyController : Controller {
    fun join(address: InetSocketAddress, gameName: String, requestedRole: NodeRole)
    fun leave()
    fun createGame(playerName : String, gameName: String, width: Int, height : Int, foodStatic: Int, stateDelay : Int)
}