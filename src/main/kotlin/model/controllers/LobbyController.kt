package model.controllers

import model.models.core.NodeRole
import java.net.InetSocketAddress

interface LobbyController {
    fun join(address: InetSocketAddress, gameName: String, requestedRole: NodeRole)
    fun leave()
    fun view(address: InetSocketAddress, playerName: String, gameName: String, requestedRole: NodeRole)
    fun createGame(gameName: String)
    fun setPlayerName(playerName: String)
}