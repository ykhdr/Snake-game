package model.states.impl

import model.states.State
import model.states.StateEditor
import model.states.StateHolder
import model.dto.core.NodeRole
import model.dto.core.GameAnnouncement
import model.dto.core.GameState

class StateHolderImpl : StateHolder {
    private val stateEditor = StateEditorImpl()

    override fun isGameRunning(): Boolean {
        val state = stateEditor.edit()
        return state.getNodeRole() == NodeRole.MASTER || state.getNodeRole() == NodeRole.DEPUTY
    }

    override fun getGameAnnouncement(): GameAnnouncement {
        val state = stateEditor.edit()

        return GameAnnouncement(
            state.getPlayers(),
            state.getConfig(),
            state.canJoin(),
            state.getGameName()
        )
    }

    override fun getState(): State {
        return stateEditor.edit()
    }

    override fun getStateEditor(): StateEditor = stateEditor
    override fun getGameState(): GameState {
        val state = stateEditor.edit()
        return GameState(
            state.getStateOrder(),
            state.getSnakes(),
            state.getFoods(),
            state.getPlayers(),
        )
    }
}