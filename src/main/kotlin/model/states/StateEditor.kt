package model.states

import model.dto.core.*
import java.net.InetSocketAddress

/**
 * StateEditor отвечает за изменения состояния внешними источниками
 */
interface StateEditor {
    fun addFoods(foods: List<Coord>)

    fun addPlayers(players: List<GamePlayer>)

    fun removePlayer(player: GamePlayer): Boolean

    fun addDeputyListeners(listeners: List<InetSocketAddress>)

    fun removeDeputyListener(listener: InetSocketAddress): Boolean

    fun addAnnouncements(address: InetSocketAddress, announcements: List<GameAnnouncement>)

    fun removeAnnouncement(address: InetSocketAddress) : Boolean

    fun addSnakes(snakes: List<Snake>)

    fun removeSnake(snake: Snake): Boolean

    fun setNodeRole(nodeRole: NodeRole)

    fun setStateOrder(stateOrder: Int)

    fun setGameName(name: String)

    fun setGameConfig(gameConfig: GameConfig)

    fun setCanJoin(canJoin: Boolean)

    fun setNodeId(id : Int)

    fun addError(errorMessage: String)
}