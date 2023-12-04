package model.states.impl

import model.dto.messages.Announcement
import model.exceptions.NoSpaceOnFieldError
import model.exceptions.NodeRoleHasNotPrivilegesError
import model.exceptions.UnknownPlayerError
import model.models.core.*
import model.models.requests.*
import model.models.requests.tasks.DeputyListenTaskRequest
import model.models.requests.tasks.MoveSnakeTaskRequest
import model.states.State
import model.states.StateEditor
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.util.*

internal class StateEditorImpl internal constructor() : StateEditor {
    companion object {
        private const val DEFAULT_PLAYER_NAME = "Player"
    }

    private var onStateEdit: (State) -> Unit = {}

    private var foods: MutableList<Coord> = mutableListOf()
    private val playersToAdding: MutableList<GamePlayer> = mutableListOf()
    private var players: MutableList<GamePlayer> = mutableListOf()
    private val deputyListeners: MutableList<InetSocketAddress> = mutableListOf()
    private var snakes: MutableList<Snake> = mutableListOf()
    private val announcements: MutableList<Announcement> = mutableListOf()
    private var nodeRole: NodeRole = NodeRole.VIEWER
    private var config: Optional<GameConfig> = Optional.empty()
    private var stateOrder: Optional<Int> = Optional.empty()
    private var curNodePlayer: Optional<GamePlayer> = Optional.empty()
    private var gameName: Optional<String> = Optional.empty()
    private var gameAddress: Optional<InetSocketAddress> = Optional.empty()
    private var playerName: String = DEFAULT_PLAYER_NAME
    private var nodeId: Optional<Int> = Optional.empty()
    private val errors: Queue<String> = LinkedList()
    private var availableCoords: MutableList<Coord> = mutableListOf()
    private val changeRoleRequests: MutableList<ChangeRoleRequest> = mutableListOf()
    private var joinRequest: Optional<JoinRequest> = Optional.empty()
    private var steerRequest: Optional<SteerRequest> = Optional.empty()
    private var leaveRequest: Optional<ChangeRoleRequest> = Optional.empty()
    private var gameCreateRequest: Optional<GameCreateRequest> = Optional.empty()
    private var deputyListenTaskRequest: DeputyListenTaskRequest = DeputyListenTaskRequest.DISABLE
    private var moveSnakeTaskRequest: MoveSnakeTaskRequest = MoveSnakeTaskRequest.DISABLE

    private val logger = KotlinLogging.logger {}

    internal fun setOnStateEditListener(onStateEdit: (State) -> Unit) {
        this.onStateEdit = onStateEdit
    }

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
    override fun setCurNodePlayer(player: GamePlayer) {
        this.curNodePlayer = Optional.of(player)
    }

    @Synchronized
    override fun addPlayerToAdding(player: GamePlayer) {
        if (availableCoords.isEmpty()) {
            throw NoSpaceOnFieldError("No available coords on field")
        }

        this.playersToAdding.add(player)
    }

    @Synchronized
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
    override fun addAnnouncement(announcement: Announcement) {
        if (announcements.stream().filter { an -> an.address == announcement.address }.findFirst().isEmpty) {
            announcements.add(announcement)
        }
    }

    @Synchronized
    override fun removeAnnouncement(announcement: Announcement): Boolean = this.announcements.remove(announcement)

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
//        curNodePlayer.get().role = nodeRole
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
    override fun updateRole(id: Int, senderRole: NodeRole, receiverRole: NodeRole) {

        // Ищем того, КТО является ОПРАВИТЕЛЕМ
        runCatching {
            players.first { player ->
                player.id == id
            }
        }.onSuccess { player ->


            // Заместитель становится главным
            if (receiverRole == NodeRole.MASTER && player.role == NodeRole.DEPUTY) {
                player.role = NodeRole.MASTER


                // От мастера о том, что он выходит и мы становимся главным
            } else if (senderRole == NodeRole.VIEWER && player.role == NodeRole.MASTER && receiverRole == NodeRole.MASTER) {
                setGameAddress(curNodePlayer.get().ip)
                leavePlayer(player)
                nodeRole = NodeRole.MASTER
                curNodePlayer.get().role = NodeRole.MASTER

                this.moveSnakeTaskRequest = MoveSnakeTaskRequest.RUN

                logger.info { "Node has become Master node" }
                // Выходящий игрок
                //TODO нужно ли проверять на то что эта нода является deputy?
            } else if (senderRole == NodeRole.VIEWER && (receiverRole == NodeRole.MASTER || receiverRole == NodeRole.DEPUTY)) {
                player.role = NodeRole.VIEWER
                leavePlayer(player)

                // От главного умершему
            } else if (senderRole == NodeRole.MASTER && receiverRole == NodeRole.VIEWER &&
                (player.role == NodeRole.NORMAL || player.role == NodeRole.DEPUTY)
            ) {
                nodeRole = NodeRole.VIEWER

                // От главного новому заместителю
            } else if (senderRole == NodeRole.MASTER && receiverRole == NodeRole.DEPUTY) {
                nodeRole = NodeRole.DEPUTY
                deputyListenTaskRequest = DeputyListenTaskRequest.RUN
            } else {
                throw NodeRoleHasNotPrivilegesError("sender node has not privileges to change role")
            }
        }.onFailure {
            throw UnknownPlayerError("player did not find in game")
        }
    }

    override fun addChangeRoleRequests(players: List<ChangeRoleRequest>) {
        this.changeRoleRequests.addAll(players)
    }

    override fun removeChangeRoleRequests(players: List<ChangeRoleRequest>) {
        this.changeRoleRequests.removeAll(players)
    }

    override fun clearDeputyListenTaskToRun() {
        this.deputyListenTaskRequest = DeputyListenTaskRequest.DISABLE
    }

    @Synchronized
    override fun setState(newState: GameState) {
//        resetState()

        this.foods = newState.foods.toMutableList()
        this.snakes = newState.snakes.toMutableList()
        this.players = newState.players.toMutableList()

        this.stateOrder = Optional.of(newState.stateOrder)

        this.curNodePlayer = players.stream().filter { pl -> pl.id == this.nodeId.get() }.findFirst()
    }

    @Synchronized
    override fun updateSnakeDirection(playerId: Int, direction: Direction) {
        val snake = this.snakes.find { snake -> snake.playerId == playerId }
            ?: throw UnknownPlayerError("Unknown player")

        val curDirection = snake.headDirection

        if (curDirection == Direction.DOWN && direction != Direction.UP ||
            curDirection == Direction.UP && direction != Direction.DOWN ||
            curDirection == Direction.LEFT && direction != Direction.RIGHT ||
            curDirection == Direction.RIGHT && direction != Direction.LEFT
        ) {

            val updatedSnake = snake.copy(headDirection = direction)
            this.snakes.remove(snake)
            this.snakes.add(updatedSnake)
        }
    }

    @Synchronized
    override fun setJoinRequest(joinRequest: JoinRequest) {
        //TODO сделать так чтобы мы ПОТОМ уже брали этот конфиг

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
        this.deputyListeners.clear()
        this.config = Optional.empty()
        this.stateOrder = Optional.empty()
        this.gameName = Optional.empty()
        this.gameAddress = Optional.empty()
        this.availableCoords.clear()
    }

    private fun leavePlayer(player: GamePlayer) {
        this.players.remove(player)
        this.snakes.removeIf { s -> s.playerId == player.id }
    }

    @Synchronized
    internal fun edit(): State {
        val newState = StateImpl(
            foods.toList(),
            playersToAdding.toList(),
            players.toList(),
            deputyListeners.toList(),
            snakes.toList(),
            announcements.toList(),
            nodeRole,
            curNodePlayer,
            config,
            stateOrder,
            gameName,
            gameAddress,
            playerName,
            errors,
            availableCoords,
            changeRoleRequests,
            joinRequest,
            steerRequest,
            leaveRequest,
            gameCreateRequest,
            deputyListenTaskRequest,
            moveSnakeTaskRequest
        )

        onStateEdit(newState)

        return newState
    }


}