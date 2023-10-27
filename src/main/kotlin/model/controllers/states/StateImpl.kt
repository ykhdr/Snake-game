package model.controllers.states

import model.dto.core.Coord
import model.dto.core.GamePlayer
import model.dto.core.NodeRole
import model.dto.core.Snake
import java.net.InetSocketAddress

internal class StateImpl internal constructor(
    private val foods: List<Coord>,
    private val players: List<GamePlayer>,
    private val deputyListeners: List<InetSocketAddress>,
    private val snakes: List<Snake>,
    private val nodeRole: NodeRole
) : State {



    override fun getFoods(): List<Coord> = foods

    override fun getSnakes(): List<Snake> = snakes

    override fun getNodeRole(): NodeRole = nodeRole

    override fun getPlayers(): List<GamePlayer> = players

    override fun getDeputyListeners(): List<InetSocketAddress> = deputyListeners

}