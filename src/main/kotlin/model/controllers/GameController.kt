package model.controllers

import model.models.core.Direction
import model.models.core.Snake
import java.net.InetSocketAddress

interface GameController : Controller {
    fun move(direction: Direction)
}