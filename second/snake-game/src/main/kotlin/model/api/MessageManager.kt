package model.api

import config.ApiConfig
import model.api.controllers.ReceiverController
import model.api.controllers.SenderController
import model.api.utils.MessageDispatcher
import model.controllers.GameController
import model.controllers.MessageHandlingController
import model.dto.messages.Message
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


class MessageManager(
    messageHandlingController: MessageHandlingController,
    private val config: ApiConfig,
    private val gameController: GameController
) {
    private val receiverController: ReceiverController = ReceiverController
    private val senderController: SenderController = SenderController
    private val messageDispatcher: MessageDispatcher = MessageDispatcher(messageHandlingController)

    private val receiveExecutor = Executors.newSingleThreadExecutor()
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    private val isReceiveTaskRunning = AtomicBoolean(true)

    private val isMasterNode = AtomicBoolean(false)

    private val logger = KotlinLogging.logger {}


    companion object{
        const val ANNOUNCEMENT_DELAY_MS = 1000
    }

    private val receiveTask = {
        val socket = MulticastSocket()
        while (isReceiveTaskRunning.get()) {
            val result = receiverController.receive(socket)
            if (result.isFailure) {
                logger.warn("Receiving data has not any message type", result.exceptionOrNull())
            }
            result.getOrNull()?.let { messageDispatcher.dispatchMessageToGameController(it) }
        }
    }


    private val pingTask = {

    }

    private val announcementTask = {
        if(isMasterNode.get()){
            val announcement =
        }
    }

    init {
        receiveExecutor.execute(receiveTask)


        logger.info("All tasks are running ")
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

    fun sendMessage(message: Message) {
        senderController.sendMessage(message)
    }

    fun announceGame()

    fun stopTasks() {
        isReceiveTaskRunning.set(false)

    }
}

