package model.dto.messages

import model.models.core.GameAnnouncement
import java.net.InetSocketAddress

class Announcement(
    address: InetSocketAddress,
    msgSeq: Long,
    val games: List<GameAnnouncement>
) : Message(address, msgSeq = msgSeq)
