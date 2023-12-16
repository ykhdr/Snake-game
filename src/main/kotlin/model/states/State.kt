package model.states

import model.dto.messages.Announcement
import model.models.core.*
import model.models.requests.ChangeRoleRequest
import model.models.requests.GameCreateRequest
import model.models.requests.JoinRequest
import model.models.requests.SteerRequest
import model.models.requests.tasks.DeputyListenTaskRequest
import model.models.requests.tasks.MoveSnakeTaskRequest
import java.net.InetSocketAddress
import java.util.*


interface State : ClientState {
    override fun getFoods() : List<Coord>
    override fun getSnakes() : List<Snake>
    override fun getPlayers() : List<GamePlayer>
    override fun getAnnouncements() : List<Announcement>

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    override fun getCurNodePlayer() : GamePlayer

    override fun isGameRunning(): Boolean

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    override fun getConfig() : GameConfig

    fun getNodeRole() : NodeRole
    fun getPlayersToAdding(): List<GamePlayer>
    fun getDeputyListeners() : List<InetSocketAddress>

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
    fun getDeputyPlayer() : GamePlayer

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
    fun getChangeRoleRequests(): List<ChangeRoleRequest>
    fun getJoinRequest() : Optional<JoinRequest>
    fun getSteerRequest() : Optional<SteerRequest>
    fun getLeaveRequest() : Optional<ChangeRoleRequest>
    fun getGameCreateRequest() : Optional<GameCreateRequest>

    fun getDeputyListenTaskRequest() : DeputyListenTaskRequest

    fun getMoveSnakeTaskRequest() : MoveSnakeTaskRequest
}
