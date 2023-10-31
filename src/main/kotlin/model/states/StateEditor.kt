package model.states

import model.dto.core.*
import java.net.InetSocketAddress

/**
 * Отвечает за изменения состояния внешними источниками
 */
interface StateEditor {
    fun addFoods(foods: List<Coord>)

    /**
     * @throws NoSpaceOnFieldError если на поле нет доступных клеток
     */
    fun addPlayerToAdding(player: GamePlayer)

    fun addPlayer(player: GamePlayer)

    fun removePlayer(player: GamePlayer): Boolean

    fun addDeputyListeners(listeners: List<InetSocketAddress>)

    fun removeDeputyListener(listener: InetSocketAddress): Boolean

    fun addAnnouncements(address: InetSocketAddress, announcements: List<GameAnnouncement>)

    fun removeAnnouncement(address: InetSocketAddress) : Boolean

    fun addSnake(snake: Snake)

    fun updateSnake(updatedSnake: Snake)

    fun removeSnake(snake: Snake): Boolean

    fun setNodeRole(nodeRole: NodeRole)

    fun setStateOrder(stateOrder: Int)

    fun setGameName(name: String)

    fun setGameConfig(gameConfig: GameConfig)


    fun setNodeId(id : Int)

    fun addError(errorMessage: String)

    fun updateAvailableCoords(coords: List<Coord>)
}