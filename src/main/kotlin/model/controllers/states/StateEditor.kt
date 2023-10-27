package model.controllers.states

import model.dto.core.Coord
import model.dto.core.GamePlayer
import model.dto.core.NodeRole
import model.dto.core.Snake
import model.exceptions.StateEditError
import java.net.InetSocketAddress

class StateEditor(
    private val onEdit: (State) -> Unit
) {
    private val foods: MutableList<Coord> = mutableListOf()
    private val players: MutableList<GamePlayer> = mutableListOf()
    private val deputyListeners: MutableList<InetSocketAddress> = mutableListOf()
    private val snakes: MutableList<Snake> = mutableListOf()
    private var nodeRole: NodeRole = NodeRole.EMPTY

    @Synchronized
    fun addFoods(foods: List<Coord>) = this.foods.addAll(foods)

    @Synchronized
    fun addPlayers(players: List<GamePlayer>) = this.players.addAll(players)

    @Synchronized
    fun removePlayer(player: GamePlayer): Boolean = this.players.remove(player)

    @Synchronized
    fun addDeputyListeners(listeners: List<InetSocketAddress>) = this.deputyListeners.addAll(listeners)

    @Synchronized
    fun removeDeputyListener(listener: InetSocketAddress): Boolean = this.deputyListeners.remove(listener)

    @Synchronized
    fun addSnakes(snakes: List<Snake>) = this.snakes.addAll(snakes)

    @Synchronized
    fun removeSnake(snake: Snake): Boolean = this.snakes.remove(snake)

    @Synchronized
    fun setNodeRole(nodeRole: NodeRole) {
        this.nodeRole = nodeRole
    }


    /**
     * @throws StateEditError если не был указан nodeRole
     */
    @Synchronized
    internal fun edit() {
        if (nodeRole == NodeRole.EMPTY) {
            throw StateEditError("Node role in editor is empty")
        }

        val state = StateImpl(foods, players, deputyListeners, snakes, nodeRole)
        onEdit(state)
    }
}