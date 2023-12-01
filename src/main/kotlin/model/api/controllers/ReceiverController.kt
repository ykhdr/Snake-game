package model.api.controllers

import me.ippolitov.fit.snakes.SnakesProto.GameMessage
import model.api.config.NetworkConfig
import model.dto.messages.Ack
import model.dto.messages.Error
import model.dto.messages.Message
import model.exceptions.UndefinedMessageTypeError
import model.mappers.ProtoMapper
import model.models.util.MessageInfo
import mu.KotlinLogging
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.SocketException
import kotlin.math.log


class ReceiverController(
    config: NetworkConfig
) : Closeable {
    companion object {
        private const val BUFFER_SIZE = 1024
    }

    private val logger = KotlinLogging.logger {}

    private val protoMapper = ProtoMapper
    private val buffer = ByteArray(BUFFER_SIZE)

    private val waitingForAck = mutableListOf<MessageInfo>()
    private val receivedAck = mutableListOf<Ack>()
    private val receivedErrors = mutableListOf<Error>()

    private val multicastSocket: MulticastSocket = initMulticastSocket(config)
    private val nodeSocket: DatagramSocket = config.nodeSocket


    /**
     * @throws UndefinedMessageTypeError если полученное сообщение явялется неизвестным
     * @throws SocketException если сокет закрыт | any IO exception
     */
    fun receiveGroupMessage(): Message {
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        multicastSocket.receive(datagramPacket)
        val protoBytes = datagramPacket.data.copyOf(datagramPacket.length)
        val protoMessage = GameMessage.parseFrom(protoBytes)
        val address = InetSocketAddress(datagramPacket.address, datagramPacket.port)

        logger.info("Group message received from ${address.address}")

        val message = protoMapper.toMessage(
            protoMessage,
            address
        )

        checkOnAck(message, protoMessage.msgSeq, address)
        checkOnError(message, protoMessage.msgSeq, address)

        return message
    }

    fun receiveNodeMessage(): Message {
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        nodeSocket.receive(datagramPacket)
        val protoBytes = datagramPacket.data.copyOf(datagramPacket.length)
        val protoMessage = GameMessage.parseFrom(protoBytes)
        val address = InetSocketAddress(datagramPacket.address, datagramPacket.port)

        logger.info("Node message received from ${address.address}")

        val message = protoMapper.toMessage(
            protoMessage,
            address
        )

        checkOnAck(message, protoMessage.msgSeq, address)
        checkOnError(message, protoMessage.msgSeq, address)

        return message
    }

    private fun checkOnAck(message: Message, msgSeq: Long, address: InetSocketAddress) {
        if (message is Ack) {
            synchronized(waitingForAck) {
                runCatching {
                    waitingForAck.stream().filter { info -> info.address == address && info.msgSequence == msgSeq }
                        .findFirst().get()
                }.onSuccess { info ->
                    waitingForAck.remove(info)
                    synchronized(receivedAck) {
                        receivedAck.add(message)
                    }
                    logger.info("Ack confirmed from ${address.address}")
                }
            }
        }
    }

    private fun checkOnError(message: Message, msgSeq: Long, address: InetSocketAddress) {
        if (message is Error) {
            synchronized(waitingForAck) {
                runCatching {
                    waitingForAck.stream().filter { info -> info.address == address && info.msgSequence == msgSeq }
                        .findFirst().get()
                }.onSuccess { info ->
                    waitingForAck.remove(info)
                    synchronized(receivedErrors) {
                        receivedErrors.add(message)
                    }
                    logger.info("Error message confirmed from ${address.address}")
                }
            }
        }
    }

    fun addNodeForWaitingAck(address: InetSocketAddress, msqSeq: Long) {
        synchronized(waitingForAck) {
            waitingForAck.add(MessageInfo(address, msqSeq))
        }
    }

    fun isAckInWaitingList(address: InetSocketAddress, msgSeq: Long): Boolean {
        synchronized(waitingForAck) {
            return waitingForAck.any { info -> info.address == address && info.msgSequence == msgSeq }
        }
    }

    /**
     * @throws NoSuchElementException если такой Ack не пришел
     */
    fun getReceivedAck(address: InetSocketAddress, msgSeq: Long): Ack {
        synchronized(receivedAck) {
            runCatching {
                receivedAck.first { a -> a.address == address && a.msgSeq == msgSeq }
            }.onSuccess { ack ->
                receivedAck.remove(ack)
                return ack
            }

            throw NoSuchElementException("Ack with this address has not in received Ack")
        }
    }

    /**
     * @throws NoSuchElementException если такой Error не пришел
     */
    fun getReceivedError(address: InetSocketAddress, msgSeq: Long): Error {
        synchronized(receivedErrors) {
            runCatching {
                receivedErrors.first { a -> a.address == address && a.msgSeq == msgSeq }
            }.onSuccess { error ->
                receivedErrors.remove(error)
                return error
            }

            throw NoSuchElementException("Error with this address has not in received Errors")
        }
    }

    private fun initMulticastSocket(config: NetworkConfig): MulticastSocket {
        //TODO добавить проверки на валидность адреса
        val socket = MulticastSocket(config.groupAddress.port)
        socket.joinGroup(
            config.groupAddress,
            config.localInterface
        )
        logger.info("Multicast socket init")
        return socket
    }

    override fun close() {
        multicastSocket.close()
        logger.info("Multicast socket closed")
    }
}