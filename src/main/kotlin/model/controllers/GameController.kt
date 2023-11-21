package model.controllers

import model.exceptions.NoGameError
import model.models.core.Direction

interface GameController : Controller {

    /**
     * @throws NoGameError если текущая нода не в игре
     */
    fun move(direction: Direction)
}