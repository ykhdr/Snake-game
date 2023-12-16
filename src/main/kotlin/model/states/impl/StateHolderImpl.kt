package model.states.impl

import model.models.core.GameAnnouncement
import model.models.core.GameState
import model.models.core.NodeRole
import model.states.State
import model.states.StateEditor
import model.states.StateHolder
import java.net.InetSocketAddress
import java.net.SocketAddress

class StateHolderImpl(
    nodeAddress: SocketAddress
) : StateHolder {
    private val stateEditor = StateEditorImpl(nodeAddress as InetSocketAddress)

    override fun isNodeMaster(): Boolean {
        return stateEditor.edit().getNodeRole() == NodeRole.MASTER
    }

    override fun getGameAnnouncement(): GameAnnouncement {
        val state = stateEditor.edit()
        return GameAnnouncement(
            state.getPlayers(),
            state.getConfig(),
            state.getAvailableCoords().isNotEmpty(),
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

    internal fun setOnStateEditListener(onStateEdit: (State) -> Unit) {
        this.stateEditor.setOnStateEditListener(onStateEdit)
    }
}