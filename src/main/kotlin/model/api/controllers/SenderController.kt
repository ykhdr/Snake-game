package model.api.controllers

import model.dto.messages.Ack
import model.dto.messages.Announcement
import model.dto.messages.Discover
import model.dto.messages.Message
import model.mappers.ProtoMapper
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class SenderController(
    private val sentMessageTime: MutableMap<InetSocketAddress, Long>
) {
    companion object {
        private const val BUFFER_SIZE = 4 * 1024
    }

    private val datagramSocket = DatagramSocket()
    private val mapper = ProtoMapper
    private val buffer = ByteArray(BUFFER_SIZE)

    fun sendMessage(message: Message) {
        val address = message.address
        val packet = DatagramPacket(buffer, BUFFER_SIZE, address)
        val protoMessage = mapper.toProtoMessage(message)
        packet.setData(protoMessage.toByteArray())
        datagramSocket.send(packet)

        if (message !is Announcement && message !is Ack && message !is Discover) {
            synchronized(sentMessageTime) {
                sentMessageTime[address] = System.currentTimeMillis()
            }
        }
    }

}