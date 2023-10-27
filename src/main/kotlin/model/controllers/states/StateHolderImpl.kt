package model.controllers.states

import model.controllers.StateHolder
import model.dto.core.NodeRole
import model.controllers.states.State
import model.controllers.states.StateEditor

class StateHolderImpl : StateHolder {
    private val stateEditor = StateEditor(this::onEdit)
    private lateinit var state: State

    init {
        stateEditor.setNodeRole(NodeRole.VIEWER)
        stateEditor.edit()
    }

    override fun isGameRunning(): Boolean {
        stateEditor.edit()
        return state.getNodeRole() == NodeRole.MASTER || state.getNodeRole() == NodeRole.DEPUTY
    }

    override fun getState(): State {
        stateEditor.edit()
        return state
    }

    override fun getStateEditor(): StateEditor = stateEditor

    private fun onEdit(state: State) {
        this.state = state
    }
}