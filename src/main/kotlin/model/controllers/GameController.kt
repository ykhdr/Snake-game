package model.controllers

import model.dto.core.Coord
import model.dto.core.Direction
import model.dto.core.Snake

interface GameController {
    fun move(snake: Snake, direction: Direction)
}