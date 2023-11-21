package model.states.impl

import model.exceptions.NoSpaceOnFieldError
import model.exceptions.NodeRoleHasNotPrivilegesError
import model.exceptions.UnknownPlayerError
import model.models.core.*
import model.models.requests.*
import model.states.State
import model.states.StateEditor
import java.net.InetSocketAddress
import java.util.*

internal class StateEditorImpl internal constructor() : StateEditor {
    companion object {
        private const val DEFAULT_PLAYER_NAME = "Player"
    }

    private val foods: MutableList<Coord> = mutableListOf()
    private val playersToAdding: MutableList<GamePlayer> = mutableListOf()
    private val players: MutableList<GamePlayer> = mutableListOf()
    private val deputyListeners: MutableList<InetSocketAddress> = mutableListOf()
    private val snakes: MutableList<Snake> = mutableListOf()
    private val announcements: MutableMap<InetSocketAddress, GameAnnouncement> = mutableMapOf()
    private var nodeRole: NodeRole = NodeRole.VIEWER
    private var config: Optional<GameConfig> = Optional.empty()
    private var stateOrder: Optional<Int> = Optional.empty()
    private var currentGamePlayer: Optional<GamePlayer> = Optional.empty()
    private var gameName: Optional<String> = Optional.empty()
    private var gameAddress: Optional<InetSocketAddress> = Optional.empty()
    private var playerName: String = DEFAULT_PLAYER_NAME
    private var nodeId: Optional<Int> = Optional.empty()
    private val errors: Queue<String> = LinkedList()
    private var availableCoords: MutableList<Coord> = mutableListOf()
    private var joinRequest: Optional<JoinRequest> = Optional.empty()
    private var steerRequest: Optional<SteerRequest> = Optional.empty()
    private var leaveRequest: Optional<ChangeRoleRequest> = Optional.empty()
    private var gameCreateRequest: Optional<GameCreateRequest> = Optional.empty()
    private var deputyListenTaskRequest: DeputyListenTaskRequest = DeputyListenTaskRequest.DISABLE
    private var moveSnakeTaskRequest: MoveSnakeTaskRequest = MoveSnakeTaskRequest.DISABLE

    @Synchronized
    override fun addFoods(foods: List<Coord>) {
        this.foods.addAll(foods)
    }

    @Synchronized
    override fun setFoods(foods: List<Coord>) {
        this.foods.clear()
        this.foods.addAll(foods)
    }

    @Synchronized
    override fun updatePlayers(players: List<GamePlayer>) {
        val playersIds = players.stream().map { p -> p.id }.toList()

        for (i in 0 until this.players.size) {
            if (this.players[i].id in playersIds) {
                //TODO ловить исключение или указать его прокидывание
                this.players[i] = players.stream().filter { p -> p.id == players[i].id }.findFirst().get()
            }
        }
    }

    @Synchronized
    override fun addPlayerToAdding(player: GamePlayer) {
        if (availableCoords.isEmpty()) {
            throw NoSpaceOnFieldError("No available coords on field")
        }

        this.playersToAdding.add(player)
    }

    override fun removePlayerToAdding(player: GamePlayer) {
        this.playersToAdding.remove(player)
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
    override fun setSnakes(snakes: List<Snake>) {
        this.snakes.clear()
        this.snakes.addAll(snakes)
    }

    @Synchronized
    override fun removeSnake(snake: Snake): Boolean = this.snakes.remove(snake)

    @Synchronized
    override fun setNodeRole(nodeRole: NodeRole) {
        if (nodeRole == NodeRole.VIEWER) {
            resetState()
        }

        this.nodeRole = nodeRole
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
    override fun setGameAddress(address: InetSocketAddress) {
        this.gameAddress = Optional.of(address)
    }

    @Synchronized
    override fun setPlayerName(name: String) {
        this.playerName = name
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

    @Synchronized
    override fun updateRole(playerAddress: InetSocketAddress, senderRole: NodeRole, receiverRole: NodeRole) {
        runCatching {
            players.first { player ->
                player.ip == playerAddress && player.port == playerAddress.port
            }
        }.onSuccess { player ->


            // Заместитель становиться главным
            if (receiverRole == NodeRole.MASTER && player.role == NodeRole.DEPUTY) {
                player.role = NodeRole.MASTER


                // От мастера о том, что он выходит и мы становимся главным
            } else if (senderRole == NodeRole.VIEWER && receiverRole == NodeRole.MASTER) {
                nodeRole = NodeRole.MASTER
                player.role = NodeRole.MASTER
                //TODO если мы становимся мастером, то надо ли здесь что то менять?

                // Выходящий игрок
            } else if (senderRole == NodeRole.VIEWER) {
                player.role = NodeRole.VIEWER
                nodeRole = NodeRole.VIEWER
                // От главного умершему
            } else if (senderRole == NodeRole.MASTER && receiverRole == NodeRole.VIEWER) {
                nodeRole = NodeRole.VIEWER
                player.role = NodeRole.VIEWER

                // От главного новому заместителю
            } else if (senderRole == NodeRole.MASTER && receiverRole == NodeRole.DEPUTY) {
                nodeRole = NodeRole.DEPUTY
                player.role = NodeRole.DEPUTY
                deputyListenTaskRequest = DeputyListenTaskRequest.RUN
            } else {
                throw NodeRoleHasNotPrivilegesError("sender node has not privileges to change role")
            }
        }.onFailure {
            throw UnknownPlayerError("player did not find in game")
        }
    }

    override fun clearDeputyListenTaskToRun() {
        this.deputyListenTaskRequest = DeputyListenTaskRequest.DISABLE
    }

    @Synchronized
    override fun setState(newState: GameState) {
        resetState()

        this.foods.addAll(newState.foods)
        this.snakes.addAll(newState.snakes)
        this.players.addAll(newState.players)
        this.stateOrder = Optional.of(newState.stateOrder)

        this.currentGamePlayer = Optional.of(
            players.stream().filter { pl -> pl.id == this.nodeId.get() }.findFirst().get()
        )
    }

    @Synchronized
    override fun updateSnakeDirection(playerId: Int, direction: Direction) {
        val snake = this.snakes.find { snake -> snake.playerId == playerId }
            ?: throw UnknownPlayerError("Unknown player")

        val updatedSnake = snake.copy(headDirection = direction)

        this.snakes.remove(snake)
        this.snakes.add(updatedSnake)
    }

    @Synchronized
    override fun setJoinRequest(joinRequest: JoinRequest) {
        this.joinRequest = Optional.of(joinRequest)
    }

    @Synchronized
    override fun clearJoinRequest() {
        this.joinRequest = Optional.empty()
    }

    @Synchronized
    override fun setSteerRequest(steerRequest: SteerRequest) {
        this.steerRequest = Optional.of(steerRequest)
    }

    @Synchronized
    override fun clearSteerRequest() {
        this.steerRequest = Optional.empty()
    }

    @Synchronized
    override fun setLeaveRequest(leaveRequest: ChangeRoleRequest) {
        this.leaveRequest = Optional.of(leaveRequest)
        moveSnakeTaskRequest = MoveSnakeTaskRequest.STOP
    }

    @Synchronized
    override fun clearLeaveRequest() {
        this.leaveRequest = Optional.empty()
    }

    @Synchronized
    override fun setGameCreateRequest(gameCreateRequest: GameCreateRequest) {
        this.gameCreateRequest = Optional.of(gameCreateRequest)
    }

    @Synchronized
    override fun clearGameCreateRequest() {
        this.gameCreateRequest = Optional.empty()
        moveSnakeTaskRequest = MoveSnakeTaskRequest.RUN
    }

    private fun resetState() {
        this.foods.clear()
        this.snakes.clear()
        this.playersToAdding.clear()
        this.players.clear()
//        this.deputyListeners.clear()
//        this.config = Optional.empty()
        this.stateOrder = Optional.empty()
//        this.gameName = Optional.empty()
        this.gameAddress = Optional.empty()
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
            currentGamePlayer,
            config,
            stateOrder,
            gameName,
            gameAddress,
            playerName,
            errors,
            availableCoords,
            joinRequest,
            steerRequest,
            leaveRequest,
            gameCreateRequest,
            deputyListenTaskRequest,
            moveSnakeTaskRequest
        )
    }


}