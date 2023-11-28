package model.api.controllers

import me.ippolitov.fit.snakes.SnakesProto.GameMessage
import model.api.config.NetworkConfig
import model.dto.messages.Ack
import model.dto.messages.Error
import model.dto.messages.Message
import model.exceptions.UndefinedMessageTypeError
import model.mappers.ProtoMapper
import mu.KotlinLogging
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.SocketException


class ReceiverController(
    config: NetworkConfig
) : Closeable{
    companion object {
        private const val BUFFER_SIZE = 1024
    }

    private val logger = KotlinLogging.logger {}

    private val protoMapper = ProtoMapper
    private val buffer = ByteArray(BUFFER_SIZE)

    private val waitingForAck = mutableMapOf<InetSocketAddress, Long>()
    private val receivedAck = mutableMapOf<InetSocketAddress, Ack>()
    private val receivedErrors = mutableMapOf<InetSocketAddress, Error>()

    private val multicastSocket: MulticastSocket = initMulticastSocket(config)
    private val localSocket : DatagramSocket = initLocalSocket(config)



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

        logger.info("Message received from ${address.address}, $protoMessage")



        val message = protoMapper.toMessage(
            protoMessage,
            address
        )

        checkOnAck(message, protoMessage.msgSeq, address)
        checkOnError(message, protoMessage.msgSeq, address)

        return message
    }

    fun receiveNodeMessage() : Message {
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        localSocket.receive(datagramPacket)
        val protoBytes = datagramPacket.data.copyOf(datagramPacket.length)
        val protoMessage = GameMessage.parseFrom(protoBytes)
        val address = InetSocketAddress(datagramPacket.address, datagramPacket.port)

        logger.info("Message received from ${address.address}, $protoMessage")



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
                if (waitingForAck.containsKey(address) && waitingForAck[address] == msgSeq) {
                    waitingForAck.remove(address)
                    synchronized(receivedAck) {
                        receivedAck[address] = message
                    }
                    logger.info("Ack confirmed from ${address.address}")
                }
            }
        }
    }

    private fun checkOnError(message: Message, msgSeq: Long, address: InetSocketAddress) {
        if (message is Error) {
            synchronized(waitingForAck) {
                if (waitingForAck.containsKey(address) && waitingForAck[address] == msgSeq) {
                    waitingForAck.remove(address)
                    synchronized(receivedErrors) {
                        receivedErrors[address] = message as Error
                    }
                    logger.info("Error message confirmed from ${address.address}")
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

    /**
     * @throws NoSuchElementException если такой Ack не пришел
     */
    fun getReceivedAckByAddress(address: InetSocketAddress): Ack {
        synchronized(receivedAck) {
            val ack = receivedAck[address]
            receivedAck.remove(address)
            return ack ?: throw NoSuchElementException("Ack with this address has not in received Ack")
        }
    }

    /**
     * @throws NoSuchElementException если такой Error не пришел
     */
    fun getReceivedErrorByAddress(address: InetSocketAddress): Error {
        synchronized(receivedErrors) {
            val error = receivedErrors[address]
            receivedErrors.remove(address)
            return error ?: throw NoSuchElementException("Error message with this address has not in received Errors")
        }
    }

    private fun initMulticastSocket(config : NetworkConfig): MulticastSocket {
        //TODO добавить проверки на валидность адреса
        val socket = MulticastSocket(config.groupAddress.port)
        socket.joinGroup(
            config.groupAddress,
            config.localInterface
        )
        logger.info("Multicast socket init")
        return socket
    }

    private fun initLocalSocket(config: NetworkConfig): DatagramSocket {
        return DatagramSocket(config.localAddress)
    }

    override fun close() {
        multicastSocket.close()
        logger.info ("Multicast socket closed")
    }
}