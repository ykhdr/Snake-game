package model.controllers

import model.models.core.Direction
import model.models.core.Snake

interface GameController : Controller {
    fun move(snake: Snake, direction: Direction)
}