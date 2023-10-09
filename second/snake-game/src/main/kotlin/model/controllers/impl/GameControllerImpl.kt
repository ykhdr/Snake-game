package model.controllers.impl

import config.ApiConfig
import model.api.MessageManager
import model.controllers.GameController
import model.controllers.MessageHandlingController
import model.dto.core.GameAnnouncement
import model.dto.core.GameConfig
import model.dto.core.GamePlayers
import model.dto.core.NodeRole
import model.dto.messages.*
import java.util.*

class GameControllerImpl : MessageHandlingController, GameController {
    private val messageManager = MessageManager(this, ApiConfig(), this)

    private lateinit var nodeRole: NodeRole
    private lateinit var config: GameConfig
    private lateinit var players: GamePlayers


    override fun acceptAck(ack: Ack) {
        TODO("Not yet implemented")
    }

    override fun acceptAnnouncement(announcement: Announcement) {
        TODO("Not yet implemented")
    }

    /**
     * В ответ должны выслать имеющиеся игры
     */
    override fun acceptDiscover(discover: Discover) {
        if (nodeRole == NodeRole.MASTER) {
            val message = Announcement()
            messageManager.sendMessage()
        }
        TODO("Not yet implemented")
    }

    override fun acceptError(error: Error) {
        TODO("Not yet implemented")
    }

    override fun acceptJoin(join: Join) {
        TODO("Not yet implemented")
    }

    override fun acceptPing(ping: Ping) {
        TODO("Not yet implemented")
    }

    override fun acceptRoleChange(roleChange: RoleChange) {
        TODO("Not yet implemented")
    }

    override fun acceptState(state: State) {
        TODO("Not yet implemented")
    }

    override fun acceptSteer(steer: Steer) {
        TODO("Not yet implemented")
    }

    override fun getGameAnnouncement(): Optional<GameAnnouncement> {
        TODO("Not yet implemented")
    }

}