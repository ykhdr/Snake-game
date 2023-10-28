package model.models

import model.api.config.NetworkConfig
import model.states.StateHolder

data class Context(
    val networkConfig: NetworkConfig,
    val stateHolder: StateHolder
)