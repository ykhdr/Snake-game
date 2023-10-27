package model.controllers.states

import model.dto.core.Coord
import model.dto.core.GamePlayer
import model.dto.core.NodeRole
import model.dto.core.Snake
import java.net.InetSocketAddress


interface State {
    fun getFoods() : List<Coord>
    fun getSnakes() : List<Snake>
    fun getNodeRole() : NodeRole
    fun getPlayers() : List<GamePlayer>
    fun getDeputyListeners() : List<InetSocketAddress>
}
