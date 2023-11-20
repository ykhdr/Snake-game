package model.states

import model.models.core.*
import model.models.requests.*
import java.net.InetSocketAddress
import java.util.*


interface State {
    fun getFoods() : List<Coord>
    fun getSnakes() : List<Snake>
    fun getNodeRole() : NodeRole
    fun getPlayersToAdding(): List<GamePlayer>
    fun getPlayers() : List<GamePlayer>
    fun getDeputyListeners() : List<InetSocketAddress>
    fun getAnnouncements() : Map<InetSocketAddress, GameAnnouncement>

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getConfig() : GameConfig

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getStateOrder() : Int
    fun getErrors() : Queue<String>

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getMasterPlayer() : GamePlayer

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getCurNodePlayer() : GamePlayer

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getGameName() : String

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getGameAddress() : InetSocketAddress
    fun getPlayerName() : String
    fun getAvailableCoords() : List<Coord>
    fun getJoinRequest() : Optional<JoinRequest>
    fun getSteerRequest() : Optional<SteerRequest>
    fun getLeaveRequest() : Optional<ChangeRoleRequest>
    fun getGameCreateRequest() : Optional<GameCreateRequest>

    fun getDeputyListenTaskRequest() : DeputyListenTaskRequest
}
