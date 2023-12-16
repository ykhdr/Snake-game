package model.client

import model.api.MessageManager
import model.api.config.NetworkConfig
import model.controllers.FieldController
import model.controllers.impl.GameControllerImpl
import model.controllers.impl.LobbyControllerImpl
import model.models.contexts.NetworkContext
import model.states.ClientState
import model.states.impl.StateHolderImpl
import java.io.Closeable
import java.net.InetSocketAddress

class Client : Closeable {
    private val networkConfig = NetworkConfig()
    private val stateHolder = StateHolderImpl(
        if (networkConfig.nodeSocket.localSocketAddress != null) networkConfig.nodeSocket.localSocketAddress else InetSocketAddress(
            4444
        )
    )
    private val context = NetworkContext(networkConfig, stateHolder)

    private val gameController = GameControllerImpl(context)
    private val lobbyController = LobbyControllerImpl(context)

    private val fieldController = FieldController(context)
    private val messageManager = MessageManager(context)

    fun getLobbyController() = lobbyController

    fun getGameController() = gameController

    fun getState(): ClientState = context.stateHolder.getState()

    fun setOnStateEditListener(onStateEdit: (ClientState) -> Unit) = stateHolder.setOnStateEditListener(onStateEdit)

    override fun close() {
        messageManager.close()
        fieldController.close()
    }
}