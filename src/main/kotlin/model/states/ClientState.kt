package model.states

import model.models.core.Coord
import model.models.core.GameAnnouncement
import model.models.core.GamePlayer
import model.models.core.Snake
import java.net.InetSocketAddress
import java.util.NoSuchElementException

interface ClientState {
    fun getFoods() : List<Coord>
    fun getSnakes() : List<Snake>

    fun getPlayers() : List<GamePlayer>

    fun getAnnouncements() : Map<InetSocketAddress, GameAnnouncement>

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getCurNodePlayer() : GamePlayer

}