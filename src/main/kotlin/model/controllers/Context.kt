package model.controllers

import model.api.config.NetworkConfig

data class Context(
    val networkConfig: NetworkConfig,
    val stateHolder: StateHolder
)