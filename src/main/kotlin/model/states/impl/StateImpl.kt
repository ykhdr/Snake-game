package model.states.impl

import model.models.requests.JoinRequest
import model.models.requests.SteerRequest
import model.models.core.*
import model.models.requests.ChangeRoleRequest
import model.states.State
import java.net.InetSocketAddress
import java.util.*

internal class StateImpl internal constructor(
    private val foods: List<Coord>,
    private val playersToAdding: Queue<GamePlayer>,
    private val players: List<GamePlayer>,
    private val deputyListeners: List<InetSocketAddress>,
    private val snakes: List<Snake>,
    private val announcements: Map<InetSocketAddress, GameAnnouncement>,
    private val nodeRole: NodeRole,
    private val curNodePlayer: Optional<GamePlayer>,
    private val config: Optional<GameConfig>,
    private val stateOrder: Optional<Int>,
    private val gameName: Optional<String>,
    private val gameAddress: Optional<InetSocketAddress>,
    private val playerName: String,
    private val errors: Queue<String>,
    private val availableCoords: List<Coord>,
    private val joinRequest: Optional<JoinRequest>,
    private val steerRequest: Optional<SteerRequest>,
    private val leaveRequest: Optional<ChangeRoleRequest>
) : State {

    override fun getFoods(): List<Coord> = foods
    override fun getSnakes(): List<Snake> = snakes
    override fun getNodeRole(): NodeRole = nodeRole
    override fun getPlayersToAdding(): Queue<GamePlayer> = playersToAdding
    override fun getPlayers(): List<GamePlayer> = players
    override fun getDeputyListeners(): List<InetSocketAddress> = deputyListeners
    override fun getConfig(): GameConfig = config.orElseThrow { NoSuchElementException("State order is empty") }
    override fun getStateOrder(): Int = stateOrder.orElseThrow { NoSuchElementException("State order is empty") }
    override fun getErrors(): Queue<String> = errors
    override fun getAnnouncements(): Map<InetSocketAddress, GameAnnouncement> = announcements
    override fun getMasterPlayer(): GamePlayer {
        return players.stream().filter { pl -> pl.role == NodeRole.MASTER }.findFirst()
            .orElseThrow { NoSuchElementException("No master player in game") }
    }

    override fun getCurNodePlayer(): GamePlayer =
        curNodePlayer.orElseThrow { NoSuchElementException("Current node player is empty") }

    override fun getGameName(): String = gameName.orElseThrow { NoSuchElementException("State order is empty") }
    override fun getGameAddress(): InetSocketAddress =
        gameAddress.orElseThrow { NoSuchElementException("State order is empty") }

    override fun getPlayerName(): String = playerName
    override fun getAvailableCoords(): List<Coord> = availableCoords
    override fun getJoinRequest(): Optional<JoinRequest> = joinRequest
    override fun getSteerRequest(): Optional<SteerRequest> = steerRequest
    override fun getLeaveRequest(): Optional<ChangeRoleRequest> = leaveRequest
}