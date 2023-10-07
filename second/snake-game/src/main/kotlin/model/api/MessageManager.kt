package model.api

import config.ApiConfig
import me.ippolitov.fit.snakes.SnakesProto.GameMessage
import model.api.controllers.MessageReceiver
import model.api.controllers.MessageSender
import model.mappers.ProtoMapper
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class MessageManager(
    private val config: ApiConfig,
    private val mapper: ProtoMapper
) {
    val messageReceiver: MessageReceiver = MessageReceiver
    val messageSender: MessageSender = MessageSender

    val receiveExecutor = Executors.newSingleThreadExecutor()
    val scheduledExecutor =  Executors.newSingleThreadScheduledExecutor()

    val isReceiveTaskRunning = AtomicBoolean(true)
    
    //TODO сделать ресивер синглтоном и без ранабл таски, прописать таску отдельно
    init {
        receiveExecutor
    }

   private val pingTask = {

   }

    private fun initSocket(): MulticastSocket {
        //TODO добавить проверки на валидность адреса
        val socket = MulticastSocket(config.groupAddress.port)
        socket.joinGroup(
            InetSocketAddress(config.groupAddress.address, config.groupAddress.port),
            NetworkInterface.getByInetAddress(config.groupAddress.address)
        )
        return socket
    }

    fun sendMessage(gameMessage: GameMessage, address: InetAddress, port: Int) {
        MessageSender.sendMessage(gameMessage.toByteArray(), address, port)
    }

    private fun handleReceiveMessage(byteArray: ByteArray) {
        val message = GameMessage.parseFrom(byteArray)
        //TODO куда то положить
        val gameMessage = mapper.toMessage(message)
    }
}

