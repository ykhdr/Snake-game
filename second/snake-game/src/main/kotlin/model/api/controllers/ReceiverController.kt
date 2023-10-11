package model.api.controllers

import me.ippolitov.fit.snakes.SnakesProto.GameMessage
import model.dto.messages.Ack
import model.dto.messages.Error
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

    private val waitingForAck = mutableMapOf<InetSocketAddress, Long>()
    private val receivedAck = mutableMapOf<InetSocketAddress, Ack>()
    private val receivedErrors = mutableMapOf<InetSocketAddress, Error>()

    private val logger = KotlinLogging.logger {}

    fun receive(socket: MulticastSocket): Result<Message> {
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        socket.receive(datagramPacket)
        val protoBytes = datagramPacket.data.copyOf(datagramPacket.length)
        val protoMessage = GameMessage.parseFrom(protoBytes)
        val address = InetSocketAddress(datagramPacket.address, datagramPacket.port)

        logger.info("Message received from ${address.address}")

        val result = runCatching {
            protoMapper.toMessage(
                protoMessage,
                address
            )
        }

        if (result.isFailure) return result

        val message = result.getOrThrow()

        checkOnAck(message, protoMessage.msgSeq, address)
        checkOnError(message, protoMessage.msgSeq, address)

        return result
    }

    private fun checkOnAck(message: Message, msgSeq: Long, address: InetSocketAddress) {
        if (message is Ack) {
            synchronized(waitingForAck) {
                if (waitingForAck.containsKey(address) && waitingForAck[address] == msgSeq) {
                    waitingForAck.remove(address)
                    logger.info("Ack confirmed from ${address.address}")
                    synchronized(receivedAck) {
                        receivedAck[address] = message as Ack
                    }
                }
            }
        }
    }

    private fun checkOnError(message: Message, msgSeq: Long, address: InetSocketAddress) {
        if (message is Error) {
            synchronized(waitingForAck) {
                if (waitingForAck.containsKey(address) && waitingForAck[address] == msgSeq) {
                    waitingForAck.remove(address)
                    logger.info("Error message confirmed from ${address.address}")
                    synchronized(receivedErrors) {
                        receivedErrors[address] = message as Error
                    }
                }
            }
        }
    }

    fun addNodeForWaitingAck(address: InetSocketAddress, msqSeq: Long) {
        synchronized(waitingForAck) {
            waitingForAck.put(address, msqSeq)
        }
    }

    fun isAckInWaitingList(address: InetSocketAddress): Boolean {
        synchronized(waitingForAck) {
            return waitingForAck.containsKey(address)
        }
    }

    fun getReceivedAckByAddress(address: InetSocketAddress): Result<Ack> {
        synchronized(receivedAck) {
            val ack = receivedAck[address]
            receivedAck.remove(address)
            return ack?.let { Result.success(ack) }
                ?: Result.failure(NoSuchElementException("Ack with this address has not in received Ack"))
        }
    }

    fun getReceivedErrorByAddress(address: InetSocketAddress): Result<Error> {
        synchronized(receivedErrors) {
            val error = receivedErrors[address]
            receivedErrors.remove(address)
            return error?.let { Result.success(error) }
                ?: Result.failure(NoSuchElementException("Error message with this address has not in received Errors"))
        }
    }
}