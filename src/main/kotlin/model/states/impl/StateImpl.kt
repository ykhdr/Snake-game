package model.states.impl

import model.states.State
import model.dto.core.*
import java.net.InetSocketAddress
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayDeque

internal class StateImpl internal constructor(
    private val foods: List<Coord>,
    private val players: List<GamePlayer>,
    private val deputyListeners: List<InetSocketAddress>,
    private val snakes: List<Snake>,
    private val announcements: Map<InetSocketAddress, GameAnnouncement>,
    private val nodeRole: NodeRole,
    private val config: Optional<GameConfig>,
    private val stateOrder: Optional<Int>,
    private val canJoin: Boolean,
    private val gameName: Optional<String>,
    private val errors: ArrayDeque<String>
) : State {


    override fun getFoods(): List<Coord> = foods

    override fun getSnakes(): List<Snake> = snakes

    override fun getNodeRole(): NodeRole = nodeRole

    override fun getPlayers(): List<GamePlayer> = players

    override fun getDeputyListeners(): List<InetSocketAddress> = deputyListeners

    override fun getConfig(): GameConfig = config.orElseThrow { NoSuchElementException("State order is empty") }

    override fun getStateOrder(): Int = stateOrder.orElseThrow { NoSuchElementException("State order is empty") }
    override fun getErrors(): ArrayDeque<String> = errors

    override fun canJoin(): Boolean = canJoin

    override fun getAnnouncements(): Map<InetSocketAddress, GameAnnouncement> = announcements

    override fun getGameName(): String = gameName.orElseThrow { NoSuchElementException("State order is empty") }
}