package model.states

import model.models.core.GameAnnouncement
import model.models.core.GameState

interface StateHolder {
    fun isGameRunning(): Boolean


    /**
     * @throws NoSuchElementException если нода не в игре
     */
    fun getGameAnnouncement(): GameAnnouncement


    fun getState(): State
    fun getStateEditor(): StateEditor

    fun getGameState() : GameState
}

