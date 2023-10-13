package model.controllers

import config.NetworkConfig
import model.api.MessageManager
import model.dto.core.*
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
    private var playerId: Optional<Int> = Optional.empty()

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
        playerId = Optional.of(id)
    }

    fun acceptAnotherNodeJoin(
        address: InetSocketAddress,
        playerType: PlayerType,
        playerName: String,
        gameName: String,
        requestedRole: NodeRole
    ) {
        if (!isGameRunning.get()) {
            messageManager.sendErrorMessage(address, ErrorMessages.NO_AVAILABLE_GAME_ON_NODE_MESSAGE)
            return
        }
        if (requestedRole == NodeRole.DEPUTY || requestedRole == NodeRole.MASTER) {
            messageManager.sendErrorMessage(address, ErrorMessages.ROLE_DENIAL_MESSAGE)
        }

        val playerCoordRes = runCatching { findCoordsForNewPlayer() }
        playerCoordRes.onSuccess { coord ->
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

            addNewPlayerToGameState(player, coord)
            //TODO реализовать обновление gameState и отправку Ack
        }.onFailure {
            messageManager.sendErrorMessage(address, ErrorMessages.NO_SPACE_ON_FIELD_MESSAGE)
        }
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
    fun getPlayerId(): Int {
        return playerId.orElseThrow { NoSuchElementException("Field is empty") }
    }

    fun getDeputyListenersAddresses() = deputyListenersAddresses

}