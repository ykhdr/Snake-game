package model.dto.messages

import model.dto.core.GameAnnouncement

data class AnnouncementMsg(
    val games: List<GameAnnouncement>
) : Msg
