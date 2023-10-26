package model.controllers

import config.NetworkConfig
import model.api.MessageManager
import model.dto.core.*
import model.exceptions.NoSpaceOnFieldError
import model.exceptions.NodeError
import model.exceptions.NodeRoleHasNotPrivilegesError
import model.exceptions.UnknownPlayerError
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

class GameController {

    companion object {
        private const val DEFAULT_GAME_STATE_DELAY = 1000L
        private const val DEFAULT_SCORE = 0
    }

    private object ErrorMessages {
        const val NO_SPACE_ON_FIELD_MESSAGE = "No free space on field"
        const val NO_AVAILABLE_GAME_ON_NODE_MESSAGE = "No available game on this node"
        const val ROLE_DENIAL_MESSAGE = "The requested role is not available to join the game with it"
        const val NODE_ERROR_MESSAGE = "Error on this node side"
        const val UNKNOWN_PLAYER_MESSAGE = "Player with this id isn't in game"
    }

    object IdSequence {
        private var currentId = 0

        fun getNextId(): Int {
            if (currentId == Int.MAX_VALUE) {
                currentId = 0
            }
            return currentId++
        }
    }

    private val messageManager = MessageManager(NetworkConfig(), this)
    private val viewGameController = ViewGameController()

    private var config: Optional<GameConfig> = Optional.empty()
    private var gameState: Optional<GameState> = Optional.empty()
    private var nodeRole: Optional<NodeRole> = Optional.empty()
    private var players: Optional<GamePlayers> = Optional.empty()
    private var gameName: Optional<String> = Optional.empty()
    private var nodeId: Optional<Int> = Optional.empty()

    private var isGameRunning = AtomicBoolean(false)

    private var deputyListenersAddresses = mutableListOf<InetSocketAddress>()
    private val availableGames = mutableMapOf<InetSocketAddress, GameAnnouncement>()

    private val idSequence = IdSequence

    private val logger = KotlinLogging.logger { }

    fun getGameStateDelay(): Long {
        return if (config.isPresent) config.get().stateDelayMs.toLong() else DEFAULT_GAME_STATE_DELAY
    }

    /**
     * @throws NullPointerException если текущая нода не состоит ни в одной из игр
     **/
    fun getGameAnnouncement(): GameAnnouncement {
        val players = players.orElseThrow()
        val config = config.orElseThrow()
        val canJoin = canJoin()
        val gameName = gameName.orElseThrow()

        return GameAnnouncement(players, config, canJoin, gameName)
    }

    fun joinGame(address: InetSocketAddress, playerName: String, gameName: String, role: NodeRole) {
        messageManager.sendJoinMessage(address, playerName, gameName, role)
    }

    fun acceptOurNodeJoin(id: Int) {
        nodeId = Optional.of(id)
    }

    fun acceptRoleChange(senderRole: NodeRole, receiverRole: NodeRole, playerAddress: InetSocketAddress) {
        runCatching { getGameState() }.onSuccess { state ->
            val player: GamePlayer
            synchronized(state) {
                player = state.players.players.filter { p ->
                    p.ip == playerAddress.address.toString() && p.port == playerAddress.port
                }.first
            }

            // Заместитель становиться главным
            if (receiverRole == NodeRole.MASTER && player.role == NodeRole.DEPUTY) {
                player.role = NodeRole.MASTER

                // От мастера о том, что он выходит и мы становимся главным
            } else if (senderRole == NodeRole.VIEWER && receiverRole == NodeRole.MASTER) {
                nodeRole = Optional.of(NodeRole.MASTER)
                //TODO если мы становимся мастером, то надо ли здесь что то менять?

                // Выходящий игрок
            } else if (senderRole == NodeRole.VIEWER) {
                player.role = NodeRole.VIEWER
                // От главного умершему
            } else if (senderRole == NodeRole.MASTER && receiverRole == NodeRole.VIEWER) {
                nodeRole = Optional.of(NodeRole.VIEWER)
                // От главного новому заместителю
            } else if (senderRole == NodeRole.MASTER && receiverRole == NodeRole.DEPUTY) {
                nodeRole = Optional.of(NodeRole.DEPUTY)
            }
        }.onFailure { e ->
            logger.warn("Game state is not present", e)
        }
    }

    fun acceptAnotherNodeJoin(
        address: InetSocketAddress,
        playerType: PlayerType,
        playerName: String,
        gameName: String,
        requestedRole: NodeRole
    ): RolesLinkage {
        val currentGameName = getGameName()
        synchronized(currentGameName) {
            if (!isGameRunning.get() || gameName == currentGameName) {
                throw NodeRoleHasNotPrivilegesError(ErrorMessages.NO_AVAILABLE_GAME_ON_NODE_MESSAGE)
            }
        }
        if (requestedRole == NodeRole.DEPUTY || requestedRole == NodeRole.MASTER) {
            throw NodeRoleHasNotPrivilegesError(ErrorMessages.ROLE_DENIAL_MESSAGE)
        }

        runCatching { findCoordsForNewPlayer() }.onSuccess { coord ->
            val playerId = idSequence.getNextId()
            val player = GamePlayer(
                playerName,
                playerId,
                address.address.toString(),
                address.port,
                requestedRole,
                playerType,
                DEFAULT_SCORE
            )

            runCatching { getNodeId() }.onSuccess { nodeId ->
                addNewPlayerToGameState(player, coord)
                return RolesLinkage(nodeId, playerId)
            }.onFailure { e ->
                logger.warn("Node id error", e)
                throw NodeError("This Node exception")
            }
        }.onFailure {
            throw NoSpaceOnFieldError(ErrorMessages.NO_SPACE_ON_FIELD_MESSAGE)
        }

        //TODO как то пофиксить?
        logger.warn("Unknown error")
        throw NodeError("Unknown error")
    }


    /**
     * @throws NoSuchElementException если gameState пуст
     */
    private fun addNewPlayerToGameState(player: GamePlayer, playerCoord: Coord) {
        val state = gameState.get()
        synchronized(state) {
            state.players.players.add(player)
            state.snakes.add(createSnake(player, playerCoord))
        }
    }

    private fun createSnake(player: GamePlayer, headCoord: Coord): Snake {
        //TODO добавить проверку на координаты (прокидывать исключение)
        val bodyCoord = getRandomSecondSnakeCoord(headCoord)
        return Snake(
            playerId = player.id,
            points = mutableListOf(headCoord, bodyCoord),
            state = SnakeState.ALIVE,
            headDirection =
            if (bodyCoord.x > headCoord.x) {
                Direction.LEFT
            } else if (bodyCoord.x < headCoord.x) {
                Direction.RIGHT
            } else if (bodyCoord.y > headCoord.y) {
                Direction.DOWN
            } else {
                Direction.UP
            }
        )
    }

    private fun getRandomSecondSnakeCoord(headCoord: Coord): Coord {
        return when (Random.nextInt(0, 4)) {
            0 -> Coord(headCoord.x - 1, headCoord.y)
            1 -> Coord(headCoord.x + 1, headCoord.y)
            2 -> Coord(headCoord.x, headCoord.y + 1)
            3 -> Coord(headCoord.x, headCoord.y - 1)
            else -> Coord(-1, -1)
        }
    }

    /**
     * @throws UnknownPlayerError если игрок с таким id не был найден
     * @throws NodeError если во время обновления направления змейки произошла ошибка
     */
    fun acceptSteer(playerId: Int, direction: Direction) {
        runCatching { getGameState() }.onSuccess { state ->
            synchronized(state) {
                val snake = (state.snakes.filter { snake -> snake.playerId == playerId }.first
                    ?: throw UnknownPlayerError(ErrorMessages.UNKNOWN_PLAYER_MESSAGE))

                snake.headDirection = direction
            }
        }.onFailure { e ->
            logger.warn("This node haven't game state to update", e)
            throw NodeError(ErrorMessages.NODE_ERROR_MESSAGE)
        }
    }

    fun acceptState(newState: GameState) {
        runCatching { getGameState() }.onSuccess { state ->
            //TODO добавить проверку на нашу ноду (чтобы никто левый не прислал и не сменил нам state)

            if (state.stateOrder <= newState.stateOrder) {
                logger.info("Node accepted previous state from master node")
                return
            }

            synchronized(state) {
                gameState = Optional.of(newState)
            }
        }.onFailure { e ->
            logger.warn("This node haven't game state to update", e)
        }
    }

    fun acceptError(message: String) {
        viewGameController.showErrorMessage(message)
    }

    fun acceptAnnouncement(masterAddress: InetSocketAddress, announcements: List<GameAnnouncement>) {
        for (announcement in announcements) {
            if (availableGames.containsKey(masterAddress)) {
                availableGames.replace(masterAddress, announcement)
            } else {
                availableGames[masterAddress] = announcement
            }
        }
    }

    private fun canJoin(): Boolean {
        TODO("not implemented yet")
    }

    private fun findCoordsForNewPlayer(): Coord {
        TODO("not implemented yet")
    }

    fun isGameRunning(): Boolean {
        return isGameRunning.get()
    }

    /**
     * @throws NoSuchElementException если поле пустое (в случае если текущая нода не в игре)
     */
    fun getConfig(): GameConfig {
        return config.orElseThrow { NoSuchElementException("Field is empty") }
    }

    /**
     * @throws NoSuchElementException если поле пустое (в случае если текущая нода не в игре)
     */
    fun getGameState(): GameState {
        return gameState.orElseThrow { NoSuchElementException("Field is empty") }
    }

    /**
     * @throws NoSuchElementException если поле пустое (в случае если текущая нода не в игре)
     */
    fun getNodeRole(): NodeRole {
        return nodeRole.orElseThrow { NoSuchElementException("Field is empty") }
    }

    /**
     * @throws NoSuchElementException если поле пустое (в случае если текущая нода не в игре)
     */
    fun getPlayers(): GamePlayers {
        return players.orElseThrow { NoSuchElementException("Field is empty") }
    }

    /**
     * @throws NoSuchElementException если поле пустое (в случае если текущая нода не в игре)
     */
    fun getGameName(): String {
        return gameName.orElseThrow { NoSuchElementException("Field is empty") }
    }

    /**
     * @throws NoSuchElementException если поле пустое (в случае если текущая нода не в игре)
     */
    fun getNodeId(): Int {
        return nodeId.orElseThrow { NoSuchElementException("Field is empty") }
    }

    fun getDeputyListenersAddresses() = deputyListenersAddresses

}