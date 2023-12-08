package model.states

import model.dto.messages.Announcement
import model.exceptions.NoSpaceOnFieldError
import model.exceptions.NodeRoleHasNotPrivilegesError
import model.exceptions.UnknownPlayerError
import model.models.requests.JoinRequest
import model.models.requests.SteerRequest
import model.models.core.*
import model.models.requests.ChangeRoleRequest
import model.models.requests.GameCreateRequest
import java.net.InetSocketAddress

/**
 * Отвечает за изменения состояния внешними источниками
 */
interface StateEditor {

    fun addFoods(foods: List<Coord>)

    fun setFoods(foods: List<Coord>)

    /**
     * @throws NoSpaceOnFieldError если на поле нет доступных клеток
     */
    fun addPlayerToAdding(player: GamePlayer)

    fun removePlayerToAdding(player: GamePlayer)

    fun addPlayer(player: GamePlayer)

    fun updatePlayers(players: List<GamePlayer>)

    fun removePlayer(player: GamePlayer): Boolean

    fun addDeputyListeners(listeners: List<InetSocketAddress>)

    fun removeDeputyListener(listener: InetSocketAddress): Boolean

    fun addAnnouncement(announcement: Announcement)

    fun removeAnnouncement(announcement: Announcement): Boolean

    fun clearAnnouncements()

    fun addSnake(snake: Snake)

    fun setSnakes(snakes: List<Snake>)

    fun removeSnake(snake: Snake): Boolean

    fun setNodeRole(nodeRole: NodeRole)

    fun setCurNodePlayer(player: GamePlayer)

    fun setStateOrder(stateOrder: Int)

    fun setGameName(name: String)
    fun setGameAddress(address: InetSocketAddress)

    fun setPlayerName(name: String)

    fun leavePlayer(player : GamePlayer)

    fun updatePlayerRole(player: GamePlayer, newRole: NodeRole)

    fun setGameConfig(gameConfig: GameConfig)

    fun setNodeId(id: Int)

    fun addError(errorMessage: String)

    fun updateAvailableCoords(coords: List<Coord>)

    /**
     * @throws NodeRoleHasNotPrivilegesError если запрос ноды является недопустимым в рамках ее привилегий
     * @throws UnknownPlayerError если игрок был не найден по адресу
     */
    fun updateRole(id: Int, senderRole: NodeRole, receiverRole: NodeRole)

    fun addChangeRoleRequests(players: List<ChangeRoleRequest>)

    fun removeChangeRoleRequests(players: List<ChangeRoleRequest>)


    fun setState(newState: GameState)

    /**
     * @throws UnknownPlayerError если змейка с таким playerId не была найдена
     */
    fun updateSnakeDirection(playerId: Int, direction: Direction)
    fun setJoinRequest(joinRequest: JoinRequest)

    fun clearJoinRequest()
    fun setSteerRequest(steerRequest: SteerRequest)

    fun clearSteerRequest()
    fun setLeaveRequest(leaveRequest: ChangeRoleRequest)

    fun clearLeaveRequest()
    fun setGameCreateRequest(gameCreateRequest: GameCreateRequest)

    fun clearGameCreateRequest()


    fun clearDeputyListenTaskToRun()
}