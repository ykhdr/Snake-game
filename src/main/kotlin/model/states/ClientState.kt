package model.states

import model.dto.messages.Announcement
import model.models.core.Coord
import model.models.core.GameConfig
import model.models.core.GamePlayer
import model.models.core.Snake

interface ClientState {
    fun getFoods() : List<Coord>
    fun getSnakes() : List<Snake>

    fun getPlayers() : List<GamePlayer>

    fun getAnnouncements() : List<Announcement>

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getCurNodePlayer() : GamePlayer
    fun isGameRunning(): Boolean

    /**
     * @throws NoSuchElementException если текущая нода не в игре
     */
    fun getConfig() : GameConfig

}