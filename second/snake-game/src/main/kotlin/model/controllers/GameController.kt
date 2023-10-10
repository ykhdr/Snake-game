package model.controllers

import config.ApiConfig
import model.api.MessageManager
import model.dto.core.*
import java.net.InetSocketAddress
import java.util.*

class GameController {

    companion object {
        private const val DEFAULT_GAME_STATE_DELAY = 1000L
    }

    private val messageManager = MessageManager(ApiConfig(), this)

    val config: Optional<GameConfig> = Optional.empty()
    val gameState: Optional<GameState> = Optional.empty()
    val nodeRole: Optional<NodeRole> = Optional.empty()
    val players: Optional<GamePlayers> = Optional.empty()
    val gameName: Optional<String> = Optional.empty()
    val deputyListenersAddresses: List<InetSocketAddress> = listOf()

    fun getGameStateDelay(): Long {
        return if (config.isPresent) config.get().stateDelayMs.toLong() else DEFAULT_GAME_STATE_DELAY
    }

    fun getGameAnnouncement(): Result<GameAnnouncement> {
        val players = players.orElseThrow()
        val config = config.orElseThrow()
        val canJoin = countCanJoin()
        val gameName = gameName.orElseThrow()

        return Result.success(GameAnnouncement(players, config, canJoin, gameName))
    }

    //TODO Rename
    private fun countCanJoin() : Boolean{
        TODO("not implemented yet")
    }
}