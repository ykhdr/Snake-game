package model.controllers

import config.NetworkConfig
import model.api.MessageManager
import model.dto.core.*
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class GameController {

    companion object {
        private const val DEFAULT_GAME_STATE_DELAY = 1000L
    }

    private object ErrorMessages {
        const val NO_SPACE_ON_FIELD_MESSAGE = "No free space on field"
        const val NO_AVAILABLE_GAME_ON_NODE_MESSAGE = "No available game on this node"
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
        gameName: String,
        requestedRole: NodeRole
    ) {
        if (!isGameRunning.get()) {
            messageManager.sendErrorMessage(address, ErrorMessages.NO_AVAILABLE_GAME_ON_NODE_MESSAGE)
            return
        }

        val playerCoordRes = findCoordsForNewPlayer()
        playerCoordRes.onFailure {
            messageManager.sendErrorMessage(address, ErrorMessages.NO_SPACE_ON_FIELD_MESSAGE)
            return
        }
        playerCoordRes.onSuccess {

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

    private fun findCoordsForNewPlayer(): Result<Coord> {
        TODO("not implemented yet")
    }

    fun isGameRunning(): Boolean {
        return isGameRunning.get()
    }

    fun getConfig(): GameConfig {
        return config.orElseThrow { NoSuchElementException("Field is empty") }
    }

    fun getGameState():GameState {
        return gameState.orElseThrow { NoSuchElementException("Field is empty") }
    }

    fun getNodeRole(): NodeRole {
        return nodeRole.orElseThrow { NoSuchElementException("Field is empty") }
    }

    fun getPlayers(): GamePlayers {
        return players.orElseThrow { NoSuchElementException("Field is empty") }
    }

    fun getGameName(): String {
        return gameName.orElseThrow { NoSuchElementException("Field is empty") }
    }

    fun getPlayerId(): Int {
        return playerId.orElseThrow { NoSuchElementException("Field is empty") }
    }

    fun getDeputyListenersAddresses() = deputyListenersAddresses

}