package model.controllers

import model.controllers.states.State
import model.controllers.states.StateEditor

interface StateHolder {
    fun isGameRunning(): Boolean
    fun getState(): State
    fun getStateEditor(): StateEditor


}

