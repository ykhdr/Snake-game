package model.states.impl

import model.states.State
import model.dto.core.*
import java.net.InetSocketAddress
import java.util.*
import kotlin.NoSuchElementException

internal class StateImpl internal constructor(
    private val foods: List<Coord>,
    private val playersToAdding: Queue<GamePlayer>,
    private val players : List<GamePlayer>,
    private val deputyListeners: List<InetSocketAddress>,
    private val snakes: List<Snake>,
    private val announcements: Map<InetSocketAddress, GameAnnouncement>,
    private val nodeRole: NodeRole,
    private val config: Optional<GameConfig>,
    private val stateOrder: Optional<Int>,
    private val gameName: Optional<String>,
    private val errors: Queue<String>,
    private val availableCoords: List<Coord>
) : State {


    override fun getFoods(): List<Coord> = foods

    override fun getSnakes(): List<Snake> = snakes

    override fun getNodeRole(): NodeRole = nodeRole

    override fun getPlayersToAdding(): Queue<GamePlayer> = playersToAdding

    override fun getPlayers(): List<GamePlayer> = players

    override fun getDeputyListeners(): List<InetSocketAddress> = deputyListeners

    override fun getConfig(): GameConfig = config.orElseThrow { NoSuchElementException("State order is empty") }

    override fun getStateOrder(): Int = stateOrder.orElseThrow { NoSuchElementException("State order is empty") }
    override fun getErrors(): Queue<String> = errors

    override fun getAnnouncements(): Map<InetSocketAddress, GameAnnouncement> = announcements

    override fun getGameName(): String = gameName.orElseThrow { NoSuchElementException("State order is empty") }
    override fun getAvailableCoords(): List<Coord> = availableCoords
}