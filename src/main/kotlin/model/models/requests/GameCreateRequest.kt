package model.models.requests

import model.models.core.GameConfig


data class GameCreateRequest(
    val gameName: String,
    val gameConfig: GameConfig
)