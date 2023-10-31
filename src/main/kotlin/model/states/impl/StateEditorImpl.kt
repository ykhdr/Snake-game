package model.states.impl

import model.states.State
import model.states.StateEditor
import model.dto.core.*
import model.exceptions.NoSpaceOnFieldError
import java.net.InetSocketAddress
import java.util.*

internal class StateEditorImpl internal constructor() : StateEditor {
    private val foods: MutableList<Coord> = mutableListOf()
    private val playersToAdding: Queue<GamePlayer> = LinkedList()
    private val players: MutableList<GamePlayer> = mutableListOf()
    private val deputyListeners: MutableList<InetSocketAddress> = mutableListOf()
    private val snakes: MutableList<Snake> = mutableListOf()
    private val announcements: MutableMap<InetSocketAddress, GameAnnouncement> = mutableMapOf()
    private var nodeRole: NodeRole = NodeRole.VIEWER
    private var config: Optional<GameConfig> = Optional.empty()
    private var stateOrder: Optional<Int> = Optional.empty()
    private var gameName: Optional<String> = Optional.empty()
    private var nodeId: Optional<Int> = Optional.empty()
    private val errors: Queue<String> = LinkedList()
    private var availableCoords: MutableList<Coord> = mutableListOf()

    @Synchronized
    override fun addFoods(foods: List<Coord>) {
        this.foods.addAll(foods)
    }

    @Synchronized
    override fun addPlayerToAdding(player: GamePlayer) {
        if (availableCoords.isEmpty()) {
            throw NoSpaceOnFieldError("No available coords on field")
        }

        this.playersToAdding.add(player)
    }

    @Synchronized
    override fun addPlayer(player: GamePlayer) {
        this.players.add(player)
    }

    @Synchronized
    override fun removePlayer(player: GamePlayer): Boolean = this.playersToAdding.remove(player)

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
    override fun addSnake(snake: Snake) {
        this.snakes.add(snake)
    }

    @Synchronized
    override fun updateSnake(updatedSnake: Snake) {
        if (this.snakes.removeIf { snake -> snake.playerId == updatedSnake.playerId }) {
            this.snakes.add(updatedSnake)
        }
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
    override fun setNodeId(id: Int) {
        nodeId = Optional.of(id)
    }

    @Synchronized
    override fun addError(errorMessage: String) {
        errors.add(errorMessage)
    }

    @Synchronized
    override fun updateAvailableCoords(coords: List<Coord>) {
        availableCoords = coords.toMutableList()
    }

    private fun resetState() {
        this.foods.clear()
        this.snakes.clear()
        this.playersToAdding.clear()
        this.players.clear()
        this.deputyListeners.clear()
        this.config = Optional.empty()
        this.stateOrder = Optional.empty()
        this.nodeRole = NodeRole.VIEWER
        this.gameName = Optional.empty()
        this.errors.clear()
        this.availableCoords.clear()
    }

    @Synchronized
    internal fun edit(): State {
        return StateImpl(
            foods,
            playersToAdding,
            players,
            deputyListeners,
            snakes,
            announcements,
            nodeRole,
            config,
            stateOrder,
            gameName,
            errors,
            availableCoords
        )
    }
}