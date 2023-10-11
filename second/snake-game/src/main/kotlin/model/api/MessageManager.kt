package model.api

import config.NetworkConfig
import model.api.controllers.ReceiverController
import model.api.controllers.SenderController
import model.controllers.GameController
import model.dto.core.NodeRole
import model.dto.messages.*
import mu.KotlinLogging
import java.net.InetSocketAddress
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

    private val ackConfirmations = mutableListOf<AckConfirmation>()


    private val isReceiveTaskRunning = AtomicBoolean(true)
    private val isAckConfirmationTaskRunning = AtomicBoolean(true)

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
                continue
            }
            val message = result.getOrThrow()
            handleMessage(message)
        }
        socket.close()
    }

    // Подтерждения только в рамках игры
    private val ackConfirmationTask = {
        while (isAckConfirmationTaskRunning.get()) {
            if (gameController.isGameRunning()) {
                val nodeRoleRes = gameController.getNodeRole()
                if (nodeRoleRes.isFailure) {
                    logger.warn("Node role is empty", nodeRoleRes.exceptionOrNull())
                    continue
                }

                if (nodeRoleRes.getOrThrow() == NodeRole.VIEWER) {
                    continue
                }

                val gameConfigRes = gameController.getConfig()
                if (gameConfigRes.isFailure) {
                    logger.warn("Game config is empty", gameConfigRes.exceptionOrNull())
                }

                val gameConfig = gameConfigRes.getOrThrow()

                val ackDelay = gameConfig.stateDelayMs / 10
                synchronized(ackConfirmations) {
                    for (ackConfirmation in ackConfirmations) {
                        if (receiverController.isAckInWaitingList(ackConfirmation.message.address)) {
                            val currentTime = System.currentTimeMillis()
                            if (ackDelay > ackConfirmation.messageSentTime - currentTime) {
                                sendMessage(ackConfirmation.message)
                                ackConfirmation.messageSentTime = currentTime
                            }
                        } else {
                            val ackResult = receiverController.getReceivedAckByAddress(ackConfirmation.message.address)
                            if (ackResult.isFailure) {
                                logger.warn("Error in ack receiving", ackResult.exceptionOrNull())
                                continue
                            }
                            handleAckOnMessage(ackConfirmation.message, ackResult.getOrThrow())
                        }
                    }
                }
            }
        }
    }

    private val announcementTask = {
        if (gameController.isGameRunning()) {
            val nodeRoleRes = gameController.getNodeRole()
            if (nodeRoleRes.isFailure) {
                logger.warn("Node role is empty", nodeRoleRes.exceptionOrNull())
            } else if (nodeRoleRes.getOrThrow() == NodeRole.MASTER) {

                val result = gameController.getGameAnnouncement()
                if (result.isFailure) {
                    logger.warn("Game Announcement is empty", result.exceptionOrNull())
                } else {
                    val announcement = result.getOrThrow()
                    sendMessage(Announcement(config.groupAddress, listOf(announcement)))
                    logger.info("Sent announcement to nodes")
                }
            }
        }
    }

    // Здесь мы смотрим является ли наша нода Deputy и после спрашиваем у нее
    // какие ноды просят вместо mastera у нас инфу об игре (GameState)
    //
    private val deputyListenersTask = {
        if (gameController.isGameRunning()
        ) {
            val nodeRoleRes = gameController.getNodeRole()
            if (nodeRoleRes.isFailure) {
                logger.warn("Node role is empty", nodeRoleRes.exceptionOrNull())
            } else if (nodeRoleRes.getOrThrow() == NodeRole.DEPUTY) {
                val gameStateRes = gameController.getGameState()
                if (gameStateRes.isFailure) {
                    logger.warn("Game state is empty", gameStateRes.exceptionOrNull())
                } else {
                    val messages = gameController.getDeputyListenersAddresses()
                        .map { address -> State(address, gameStateRes.getOrThrow()) }

                    for (message in messages) {
                        sendMessage(message)
                        waitAckOnMessage(message)
                    }
                    logger.info("Sent state to deputy listeners")
                }
            }
        }
    }

    init {
        receiveExecutor.execute(receiveTask)
        receiveExecutor.execute(ackConfirmationTask)

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

    fun stopTasks() {
        isReceiveTaskRunning.set(false)
        isAckConfirmationTaskRunning.set(false)
        receiveExecutor.shutdown()
    }


    fun sendJoinMessage(address: InetSocketAddress, playerName: String, gameName: String, role: NodeRole) {
        val message = Join(
            address = address,
            playerName = playerName,
            gameName = gameName,
            requestedRole = role
        )
        sendMessage(message)
        waitAckOnMessage(message)
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

    private fun sendMessage(message: Message) {
        senderController.sendMessage(message)
    }

    private fun waitAckOnMessage(message: Message) {
        val messageSentTime = System.currentTimeMillis()
        synchronized(ackConfirmations) {
            ackConfirmations.add(AckConfirmation(messageSentTime, message))
        }
        receiverController.addNodeForWaitingAck(message.address, message.msgSeq)
    }

    private fun handleAckOnMessage(message: Message, ack: Message) {
        when (message) {
            is Ack -> TODO()
            is Announcement -> TODO()
            is Discover -> TODO()
            is Error -> TODO()
            is Join -> gameController.acceptJoin(ack.receiverId)
            is Ping -> TODO()
            is RoleChange -> TODO()
            is State -> TODO()
            is Steer -> TODO()
        }
    }

    private fun handleMessage(message: Message) {
        when (message) {
            is Ack -> {
                // за это отвечает специальная таска, никак не обрабатываем
            }

            is Announcement -> TODO()
            is Discover -> TODO()
            is Error -> TODO()
            is Join -> TODO()
            is Ping -> TODO()
            is RoleChange -> TODO()
            is State -> TODO()
            is Steer -> TODO()
        }
    }
}

