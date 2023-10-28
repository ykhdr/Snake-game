package model.states

import model.dto.core.*
import java.net.InetSocketAddress
import java.util.NoSuchElementException


interface State {
    fun getFoods() : List<Coord>
    fun getSnakes() : List<Snake>
    fun getNodeRole() : NodeRole
    fun getPlayers() : List<GamePlayer>
    fun getDeputyListeners() : List<InetSocketAddress>
    fun canJoin() : Boolean
    fun getAnnouncements() : Map<InetSocketAddress, GameAnnouncement>

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getConfig() : GameConfig

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getStateOrder() : Int

    fun getErrors() : ArrayDeque<String>


    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getGameName() : String


}
