package model.controllers

import model.dto.messages.*

interface MessageHandlingController {
    fun acceptAck(ack: Ack)
    fun acceptAnnouncement(announcement: Announcement)
    fun acceptDiscover(discover: Discover)
    fun acceptError(error: Error)
    fun acceptJoin(join: Join)
    fun acceptPing(ping: Ping)
    fun acceptRoleChange(roleChange: RoleChange)
    fun acceptState(state: State)
    fun acceptSteer(steer: Steer)


}