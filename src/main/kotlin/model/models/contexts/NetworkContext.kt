package model.models.contexts

import model.api.config.NetworkConfig
import model.models.contexts.Context
import model.states.StateHolder

class NetworkContext(
    val networkConfig: NetworkConfig,
    stateHolder: StateHolder
) : Context(stateHolder)