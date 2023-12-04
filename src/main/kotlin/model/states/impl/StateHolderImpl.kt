package model.states.impl

import model.models.core.GameAnnouncement
import model.models.core.GameState
import model.models.core.NodeRole
import model.states.State
import model.states.StateEditor
import model.states.StateHolder

class StateHolderImpl : StateHolder {
    private val stateEditor = StateEditorImpl()
    private var cachedState: State

    init {
        cachedState = getState()
    }

    override fun isNodeMaster(): Boolean {
        return cachedState.getNodeRole() == NodeRole.MASTER
    }

    override fun getGameAnnouncement() = GameAnnouncement(
        cachedState.getPlayers(),
        cachedState.getConfig(),
        cachedState.getAvailableCoords().isNotEmpty(),
        cachedState.getGameName()
    )


    override fun getState(): State {
        cachedState = stateEditor.edit()
        return cachedState
    }

    override fun getStateEditor(): StateEditor = stateEditor
    override fun getGameState() = GameState(
        cachedState.getStateOrder(),
        cachedState.getSnakes(),
        cachedState.getFoods(),
        cachedState.getPlayers(),
    )

    internal fun setOnStateEditListener(onStateEdit: (State) -> Unit) {
        this.stateEditor.setOnStateEditListener(onStateEdit)
    }
}