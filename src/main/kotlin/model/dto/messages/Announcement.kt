package model.dto.messages

import model.dto.core.GameAnnouncement
import java.net.InetSocketAddress

class Announcement(
    address: InetSocketAddress,
    val games: List<GameAnnouncement>
) : Message(address)
