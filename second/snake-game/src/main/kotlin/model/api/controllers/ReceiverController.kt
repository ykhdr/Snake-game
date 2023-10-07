package model.api.controllers

import me.ippolitov.fit.snakes.SnakesProto.GameMessage
import model.dto.messages.Message
import model.exceptions.UndefinedMessageTypeError
import model.mappers.ProtoMapper
import mu.KotlinLogging
import java.net.DatagramPacket
import java.net.MulticastSocket


object ReceiverController {
    private const val BUFFER_SIZE = 1024

    private val protoMapper = ProtoMapper
    private val buffer = ByteArray(BUFFER_SIZE)

    private val logger = KotlinLogging.logger {}

    fun receive(socket: MulticastSocket): Result<Message> {
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        socket.receive(datagramPacket)
        val protoBytes = datagramPacket.data.copyOf(datagramPacket.length)
        return runCatching { matchMessageType(GameMessage.parseFrom(protoBytes)) }
    }

    private fun matchMessageType(message: GameMessage): Message {
        return if (message.hasAck())
            TODO("implement protoMapper method")
        else if (message.hasAnnouncement())
            TODO("implement protoMapper method")
        else if (message.hasDiscover())
            TODO("implement protoMapper method")
        else if (message.hasJoin())
            TODO("implement protoMapper method")
        else if (message.hasError())
            TODO("implement protoMapper method")
        else if (message.hasPing())
            TODO("implement protoMapper method")
        else if (message.hasRoleChange())
            TODO("implement protoMapper method")
        else if (message.hasState())
            TODO("implement protoMapper method")
        else if (message.hasSteer())
            TODO("implement protoMapper method")
        else
            throw UndefinedMessageTypeError(message = "No match type of message")
    }

}