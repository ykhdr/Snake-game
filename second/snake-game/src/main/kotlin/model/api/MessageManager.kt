package model.api

import config.NetworkConfig
import model.api.controllers.ReceiverController
import model.api.controllers.SenderController
import model.controllers.GameController
import model.dto.core.NodeRole
import model.dto.messages.Announcement
import model.dto.messages.Message
import model.dto.messages.State
import mu.KotlinLogging
import java.net.MulticastSocket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class MessageManager(
    private val config: NetworkConfig,
    private val gameController: GameController
) {
    private val receiverController: ReceiverController = ReceiverController
    private val senderController: SenderController = SenderController

    private val receiveExecutor = Executors.newSingleThreadExecutor()
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    private val isReceiveTaskRunning = AtomicBoolean(true)

    private val logger = KotlinLogging.logger {}


    companion object {
        const val ANNOUNCEMENT_DELAY_MS = 1000
        const val ANNOUNCEMENT_INITIAL_DELAY_MS = 1000
    }

    private val receiveTask = {
        val socket = initSocket()
        while (isReceiveTaskRunning.get()) {
            val result = receiverController.receive(socket)
            if (result.isFailure) {
                logger.warn("Receiving data has not any message type", result.exceptionOrNull())
            }
            // сделать здесь обработку
        }
    }


    private val pingTask = {

    }

    private val announcementTask = {
        if (gameController.nodeRole.isPresent
            && gameController.nodeRole.get() == NodeRole.MASTER
        ) {
            val result = gameController.getGameAnnouncement()
            if (result.isFailure) {
                logger.warn("Error in game Announcement", result.exceptionOrNull())
            }
            val announcement = result.getOrThrow()
            sendMessage(Announcement(config.groupAddress, listOf(announcement)))
            logger.info("Sent announcement to nodes")
        }
    }

    // Здесь мы смотрим является ли наша нода Deputy и после спрашиваем у нее
    // какие ноды просят вместо mastera у нас инфу об игре (GameState)
    //
    private val deputyListenersTask = {
        if (gameController.nodeRole.isPresent
            && gameController.nodeRole.get() == NodeRole.DEPUTY
            && gameController.gameState.isPresent
        ) {
            val gameState = gameController.gameState.get()
            for (address in gameController.deputyListenersAddresses) {

                sendMessage(State(address, gameState))
            }


            logger.info("Sent state to deputy listeners")
        }
    }

    init {
        receiveExecutor.execute(receiveTask)

        scheduledExecutor.scheduleWithFixedDelay(
            announcementTask,
            ANNOUNCEMENT_INITIAL_DELAY_MS.toLong(),
            ANNOUNCEMENT_DELAY_MS.toLong(),
            TimeUnit.MILLISECONDS
        )

        scheduledExecutor.scheduleWithFixedDelay(
            deputyListenersTask,
            0,
            // TODO подумать какое сюда лучше подставить время в случае, если мы не DEPUTY
            gameController.getGameStateDelay(),
            TimeUnit.MILLISECONDS
        )



        logger.info("All tasks are running ")
    }

    private fun initSocket(): MulticastSocket {
        //TODO добавить проверки на валидность адреса
        val socket = MulticastSocket(config.groupAddress.port)
        socket.joinGroup(
            config.groupAddress,
            config.localInterface
        )
        return socket
    }

    fun sendMessage(message: Message) {
        senderController.sendMessage(message)
    }

    fun stopTasks() {
        isReceiveTaskRunning.set(false)
        receiveExecutor.shutdown()
    }
}

