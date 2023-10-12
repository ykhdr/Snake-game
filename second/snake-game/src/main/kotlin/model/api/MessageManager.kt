package model.api

import config.NetworkConfig
import model.api.controllers.ReceiverController
import model.api.controllers.SenderController
import model.controllers.GameController
import model.dto.core.GameConfig
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
            val result = runCatching { receiverController.receive(socket) }
            result.onSuccess {
                handleMessage(it)
            }.onFailure {
                logger.warn("Receiving data has not any message type", it)
            }
        }
        socket.close()
    }

    // Подтерждения только в рамках игры
    private val ackConfirmationTask = {
        while (isAckConfirmationTaskRunning.get()) {
            if (gameController.isGameRunning()) {
                val nodeRoleRes = runCatching { gameController.getNodeRole() }
                nodeRoleRes.onSuccess { nodeRole ->
                    if (nodeRole == NodeRole.VIEWER) {
                        return@onSuccess
                    }

                    val gameConfigRes = runCatching { gameController.getConfig() }
                    gameConfigRes.onSuccess { config ->
                        ackConfirm(config)
                    }.onFailure { e ->
                        logger.warn("Game config is empty", e)
                    }
                }.onFailure { e ->
                    logger.warn("Node role is empty", e)
                }
            }
        }
    }

    // TODO надо подумаьт над тем, что будет, когда ресивер отпадет и не будет отвечать совсем
    private fun ackConfirm(gameConfig: GameConfig) {
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
                    val ackResult = runCatching {
                        receiverController.getReceivedAckByAddress(ackConfirmation.message.address)
                    }
                    ackResult.onSuccess { ack ->
                        handleAckOnMessage(ackConfirmation.message, ack)
                    }.onFailure {
                        val errorResult = runCatching {
                            receiverController.getReceivedErrorByAddress(ackConfirmation.message.address)
                        }
                        errorResult.onSuccess { message ->
                            handleMessage(message)
                        }.onFailure { e ->
                            logger.warn("Error in ack receiving", e)
                        }

                    }

                }
            }
        }
    }

    private val announcementTask = {
        if (gameController.isGameRunning()) {
            val nodeRoleRes = runCatching { gameController.getNodeRole() }
            nodeRoleRes.onSuccess { nodeRole ->
                if (nodeRole != NodeRole.MASTER) return@onSuccess

                val announcementRes = runCatching { gameController.getGameAnnouncement() }
                announcementRes.onSuccess { announcement ->
                    sendMessage(Announcement(config.groupAddress, listOf(announcement)))
                    logger.info("Sent announcement to nodes")
                }.onFailure { e ->
                    logger.warn("Game Announcement is empty", e)
                }


            }.onFailure { e ->
                logger.warn("Node role is empty", e)
            }

        }
    }

    // Здесь мы смотрим является ли наша нода Deputy и после спрашиваем у нее
// какие ноды просят вместо mastera у нас инфу об игре (GameState)
//
    private val deputyListenersTask = {
        if (gameController.isGameRunning()) {
            val nodeRoleRes = runCatching { gameController.getNodeRole() }
            nodeRoleRes.onSuccess { nodeRole ->
                if (nodeRole != NodeRole.DEPUTY) {
                    return@onSuccess
                }

                val gameStateRes = runCatching { gameController.getGameState() }
                gameStateRes.onSuccess { gameState ->
                    val messages = gameController.getDeputyListenersAddresses()
                        .map { address -> State(address, gameState) }

                    for (message in messages) {
                        sendMessage(message)
                        waitAckOnMessage(message)
                    }
                    logger.info("Sent state to deputy listeners")
                }.onFailure { e ->
                    logger.warn("Game state is empty", e)
                }

            }.onFailure { e ->
                logger.warn("Node role is empty", e)
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


    fun sendErrorMessage(address: InetSocketAddress, errorMessage: String) {
        val message = Error(
            address = address,
            errorMessage = errorMessage
        )

        sendMessage(message)
        waitAckOnMessage(message)
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


    //TODO перенести проверку
    private fun sendAnnouncement(address: InetSocketAddress) {
        runCatching {
            if (gameController.isGameRunning()
                && gameController.getNodeRole() == NodeRole.MASTER
            ) {
                val result = runCatching { gameController.getGameAnnouncement() }
                result.onFailure {
                    logger.warn("Game Announcement error", it)
                }
                result.onSuccess {
                    val message = Announcement(address, listOf(it))
                    sendMessage(message)
                }
            }
        }.onFailure { e ->
            logger.warn("Game node error", e)
        }
    }

    private fun sendAckOnJoin(address: InetSocketAddress) {

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


    /**
     * Обрабатывает приходящие Ack-и на сообщения
     */
    private fun handleAckOnMessage(message: Message, ack: Message) {
        when (message) {
            is Ack -> TODO()
            is Announcement -> TODO()
            is Discover -> TODO()
            is Error -> TODO()
            is Join -> gameController.acceptOurNodeJoin(ack.receiverId)
            is Ping -> TODO()
            is RoleChange -> TODO()
            is State -> TODO()
            is Steer -> TODO()
        }
    }

    /**
     * Обрабатывает приходящие от других людей сообщения
     */
    private fun handleMessage(message: Message) {
        when (message) {
            is Ack -> { /* за это отвечает специальная таска, никак не обрабатываем */
            }

            is Announcement -> gameController.acceptAnnouncement(message.address, message.games)
            is Discover -> sendAnnouncement(message.address)
            is Error -> gameController.acceptError(message.errorMessage)
            is Join -> gameController.acceptAnotherNodeJoin(
                message.address,
                message.playerType,
                message.playerName,
                message.gameName,
                message.requestedRole
            )

            is Ping -> TODO()
            is RoleChange -> TODO()
            is State -> TODO()
            is Steer -> TODO()
        }
    }
}

