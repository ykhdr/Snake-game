package model.api.controllers

import me.ippolitov.fit.snakes.SnakesProto
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object SenderController {
    private val datagramSocket = DatagramSocket()
    fun sendMessage(byteArray: ByteArray, address: InetAddress, port: Int) {
        val packet = DatagramPacket(byteArray, byteArray.size, address, port)
        datagramSocket.send(packet)
    }

    fun sendGameMessage(message: SnakesProto.GameMessage){

    }


}