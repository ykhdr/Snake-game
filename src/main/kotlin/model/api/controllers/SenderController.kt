package model.api.controllers

import model.api.config.NetworkConfig
import model.dto.messages.Message
import model.mappers.ProtoMapper
import model.utils.MessageSequence
import mu.KotlinLogging
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket

class SenderController(config: NetworkConfig) : Closeable {
    companion object {
        private const val BUFFER_SIZE = 4 * 1024
    }

    private val datagramSocket = config.nodeSocket
    private val mapper = ProtoMapper
    private val buffer = ByteArray(BUFFER_SIZE)

    private val logger = KotlinLogging.logger {}

    fun sendMessage(message: Message) {
        val address = message.address
        val packet = DatagramPacket(buffer, BUFFER_SIZE, address)
        try {
            val protoMessage = mapper.toProtoMessage(message)
            packet.setData(protoMessage.toByteArray())
        } catch (e : Exception){
            logger.warn("Errreeeeeeeeeeeeor: ", e)
        }
        datagramSocket.send(packet)
        logger.info("Message with seq ${message.msgSeq} sent")
    }

    override fun close() {
        datagramSocket.close()
        logger.info("Socket closed")
    }

}