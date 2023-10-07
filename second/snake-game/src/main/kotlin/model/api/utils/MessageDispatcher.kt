package model.api.utils

import model.controllers.GameController
import model.dto.messages.*

class MessageDispatcher(
    val gameController: GameController
) {

    //TODO реализовать методы
    fun dispatchMessage(message: Message){
        when(message){
            is Ack -> println("ack")
            is Announcement -> println("announcement")
            is Discover -> println("discover")
            is Error -> println("error")
            is Join -> println("join")
            is Ping -> println("ping")
            is RoleChange -> println("role change")
            is State -> println("state")
            is Steer -> println("steer")
        }
    }

}