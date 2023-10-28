package model.states.impl

import model.states.State
import model.states.StateEditor
import model.dto.core.*
import java.net.InetSocketAddress
import java.util.*
import kotlin.collections.ArrayDeque

internal class StateEditorImpl internal constructor() : StateEditor {
    private val foods: MutableList<Coord> = mutableListOf()
    private val players: MutableList<GamePlayer> = mutableListOf()
    private val deputyListeners: MutableList<InetSocketAddress> = mutableListOf()
    private val snakes: MutableList<Snake> = mutableListOf()
    private val announcements: MutableMap<InetSocketAddress, GameAnnouncement> = mutableMapOf()
    private var nodeRole: NodeRole = NodeRole.VIEWER
    private var config: Optional<GameConfig> = Optional.empty()
    private var stateOrder: Optional<Int> = Optional.empty()
    private var gameName: Optional<String> = Optional.empty()
    private var canJoin: Boolean = false
    private var nodeId: Optional<Int> = Optional.empty()
    private val errors: ArrayDeque<String> = ArrayDeque()

    @Synchronized
    override fun addFoods(foods: List<Coord>) {
        this.foods.addAll(foods)
    }

    @Synchronized
    override fun addPlayers(players: List<GamePlayer>) {
        //TODO при добавлении игрока создавать змейку
        this.players.addAll(players)
    }

    @Synchronized
    override fun removePlayer(player: GamePlayer): Boolean = this.players.remove(player)

    @Synchronized
    override fun addDeputyListeners(listeners: List<InetSocketAddress>) {
        this.deputyListeners.addAll(listeners)
    }

    @Synchronized
    override fun removeDeputyListener(listener: InetSocketAddress): Boolean = this.deputyListeners.remove(listener)

    @Synchronized
    override fun addAnnouncements(address: InetSocketAddress, announcements: List<GameAnnouncement>) {
        for (announcement in announcements) {
            if (this.announcements.containsKey(address)) {
                this.announcements.replace(address, announcement)
            } else {
                this.announcements[address] = announcement
            }
        }
    }

    @Synchronized
    override fun removeAnnouncement(address: InetSocketAddress): Boolean = this.announcements.remove(address) != null

    @Synchronized
    override fun addSnakes(snakes: List<Snake>) {
        this.snakes.addAll(snakes)
    }

    @Synchronized
    override fun removeSnake(snake: Snake): Boolean = this.snakes.remove(snake)

    @Synchronized
    override fun setNodeRole(nodeRole: NodeRole) {
        if (nodeRole == NodeRole.VIEWER) {
            resetState()
        } else {
            this.nodeRole = nodeRole
        }
    }

    @Synchronized
    override fun setStateOrder(stateOrder: Int) {
        this.stateOrder = Optional.of(stateOrder)
    }

    @Synchronized
    override fun setGameName(name: String) {
        this.gameName = Optional.of(name)
    }

    @Synchronized
    override fun setGameConfig(gameConfig: GameConfig) {
        this.config = Optional.of(gameConfig)
    }

    @Synchronized
    override fun setCanJoin(canJoin: Boolean) {
        this.canJoin = canJoin
    }

    @Synchronized
    override fun setNodeId(id: Int) {
        nodeId = Optional.of(id)
    }

    @Synchronized
    override fun addError(errorMessage: String) {
        errors.add(errorMessage)
    }

    private fun resetState() {
        this.foods.clear()
        this.snakes.clear()
        this.players.clear()
        this.deputyListeners.clear()
        this.config = Optional.empty()
        this.stateOrder = Optional.empty()
        this.nodeRole = NodeRole.VIEWER
        this.canJoin = false
        this.gameName = Optional.empty()
        this.errors.clear()
    }

    @Synchronized
    internal fun edit(): State {
        return StateImpl(
            foods,
            players,
            deputyListeners,
            snakes,
            announcements,
            nodeRole,
            config,
            stateOrder,
            canJoin,
            gameName,
            errors
        )
    }
}