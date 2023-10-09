package model.api.controllers

import model.dto.messages.Message
import model.mappers.ProtoMapper
import java.net.DatagramPacket
import java.net.DatagramSocket

object SenderController {
    private const val BUFFER_SIZE = 1024

    private val datagramSocket = DatagramSocket()
    private val mapper = ProtoMapper
    private val buffer = ByteArray(BUFFER_SIZE)
    fun sendMessage(message: Message){
        val address = message.address
        val packet = DatagramPacket(buffer, BUFFER_SIZE, address)
        val protoMessage = mapper.toProtoMessage(message)
        packet.setData(protoMessage.toByteArray())
        datagramSocket.send(packet)
    }
}