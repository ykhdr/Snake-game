package model.models

import model.api.config.NetworkConfig
import model.states.StateHolder

class NetworkContext(
    val networkConfig: NetworkConfig,
    stateHolder: StateHolder
) : Context(stateHolder)