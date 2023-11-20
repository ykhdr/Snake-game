package model.client

import model.api.MessageManager
import model.api.config.NetworkConfig
import model.controllers.FieldController
import model.controllers.impl.GameControllerImpl
import model.controllers.impl.LobbyControllerImpl
import model.models.contexts.NetworkContext
import model.states.impl.StateHolderImpl
import java.io.Closeable

class Client : Closeable {
    private val context = NetworkContext(NetworkConfig(), StateHolderImpl())

    private val gameController = GameControllerImpl(context)
    private val lobbyController = LobbyControllerImpl(context)

    private val fieldController = FieldController(context)
    private val messageManager = MessageManager(context)

    fun getLobbyController() = lobbyController

    fun getGameController() = gameController

    override fun close() {
        messageManager.close()
        fieldController.close()
    }
}