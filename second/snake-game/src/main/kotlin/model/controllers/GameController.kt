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

    private val messageManager = MessageManager(NetworkConfig(), this)
    private val viewGameController = ViewGameController()

    //TODO прописать для каждого геттер и возвращать конкретно экземлпяр + сделать поля приватынми
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
        val canJoin = countCanJoin()
        val gameName = gameName.orElseThrow()

        return GameAnnouncement(players, config, canJoin, gameName)
    }

    fun joinGame(address: InetSocketAddress, playerName: String, gameName: String, role: NodeRole) {
        messageManager.sendJoinMessage(address, playerName, gameName, role)
    }

    fun acceptJoin(id: Int) {
        playerId = Optional.of(id)
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


    //TODO Rename
    private fun countCanJoin(): Boolean {
        TODO("not implemented yet")
    }

    fun isGameRunning(): Boolean {
        return isGameRunning.get()
    }

    fun getConfig(): Result<GameConfig> {
        return if (config.isPresent) Result.success(config.get()) else Result.failure(NoSuchElementException("Field is empty"))
    }

    fun getGameState(): Result<GameState> {
        return if (gameState.isPresent) Result.success(gameState.get()) else Result.failure(NoSuchElementException("Field is empty"))
    }

    fun getNodeRole(): Result<NodeRole> {
        return if (nodeRole.isPresent) Result.success(nodeRole.get()) else Result.failure(NoSuchElementException("Field is empty"))
    }

    fun getPlayers(): Result<GamePlayers> {
        return if (players.isPresent) Result.success(players.get()) else Result.failure(NoSuchElementException("Field is empty"))
    }

    fun getGameName(): Result<String> {
        return if (gameName.isPresent) Result.success(gameName.get()) else Result.failure(NoSuchElementException("Field is empty"))
    }

    fun getPlayerId(): Result<Int> {
        return if (playerId.isPresent) Result.success(playerId.get()) else Result.failure(NoSuchElementException("Field is empty"))
    }

    fun getDeputyListenersAddresses() = deputyListenersAddresses

}