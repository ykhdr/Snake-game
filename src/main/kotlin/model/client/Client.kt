package model.client

import model.controllers.GameController
import model.controllers.LobbyController

interface Client {
    fun getLobbyController() : LobbyController

    fun getGameController() : GameController
}