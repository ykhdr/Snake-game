package model.dto.messages

import model.dto.core.GameAnnouncement
import java.net.InetAddress

class Announcement(
    address: InetAddress,
    val games: List<GameAnnouncement>
) : Message(address)
