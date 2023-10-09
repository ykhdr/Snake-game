package model.controllers

import model.dto.core.GameAnnouncement
import java.util.*

interface GameController {
    fun getGameAnnouncement() : Optional<GameAnnouncement>
}