package model.api.utils

import model.controllers.MessageHandlingController
import model.dto.messages.*

class MessageDispatcher(
    private val messageHandlingController: MessageHandlingController
) {

    //TODO реализовать методы
    fun dispatchMessageToGameController(message: Message){
        when(message){
            is Ack -> messageHandlingController.acceptAck(message)
            is Announcement -> messageHandlingController.acceptAnnouncement(message)
            is Discover -> messageHandlingController.acceptDiscover(message)
            is Error -> messageHandlingController.acceptError(message)
            is Join -> messageHandlingController.acceptJoin(message)
            is Ping -> messageHandlingController.acceptPing(message)
            is RoleChange -> messageHandlingController.acceptRoleChange(message)
            is State -> messageHandlingController.acceptState(message)
            is Steer -> messageHandlingController.acceptSteer(message)
        }
    }


}