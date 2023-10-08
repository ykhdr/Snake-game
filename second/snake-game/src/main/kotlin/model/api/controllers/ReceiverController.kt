package model.api.controllers

import me.ippolitov.fit.snakes.SnakesProto.GameMessage
import model.dto.messages.Message
import model.mappers.ProtoMapper
import mu.KotlinLogging
import java.net.DatagramPacket
import java.net.InetSocketAddress
import java.net.MulticastSocket


object ReceiverController {
    private const val BUFFER_SIZE = 1024

    private val protoMapper = ProtoMapper
    private val buffer = ByteArray(BUFFER_SIZE)

    private val logger = KotlinLogging.logger {}

    fun receive(socket: MulticastSocket): Result<Message> {
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        socket.receive(datagramPacket)
        logger.info("Message received")
        val protoBytes = datagramPacket.data.copyOf(datagramPacket.length)
        // TODO проверить какой адресс приходит
        return runCatching { protoMapper.toMessage(GameMessage.parseFrom(protoBytes), InetSocketAddress(datagramPacket.address,datagramPacket.port)) }
    }

}