package model.api

import model.api.controllers.ReceiverController
import model.api.controllers.SenderController
import model.models.Context
import model.states.StateHolder
import model.dto.core.GameConfig
import model.dto.core.GamePlayer
import model.dto.core.NodeRole
import model.dto.messages.*
import model.models.AckConfirmation
import model.utils.IdSequence
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class MessageManager(
    context: Context
) {
    private val stateHolder: StateHolder = context.stateHolder

    private val threadExecutor = Executors.newSingleThreadExecutor()
    private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    private val ackConfirmations = mutableListOf<AckConfirmation>()
    private val sentMessageTime = mutableMapOf<InetSocketAddress, Long>()

    private val isPingTaskRunning = AtomicBoolean(true)
    private val isThreadExecutorTasksRunning = AtomicBoolean(true)

    private val receiverController: ReceiverController = ReceiverController(context.networkConfig)
    private val senderController: SenderController = SenderController()

    private val logger = KotlinLogging.logger {}


    companion object {
        const val ANNOUNCEMENT_DELAY_MS = 1000
        const val ANNOUNCEMENT_INITIAL_DELAY_MS = 1000
    }

    private val receiveTask = {
        while (isThreadExecutorTasksRunning.get()) {
            val result = runCatching { receiverController.receive() }
            result.onSuccess {
                handleMessage(it)
            }.onFailure {
                logger.warn("Receiving data has not any message type", it)
            }
        }
    }

    // Подтерждения только в рамках игры
    private val ackConfirmationTask = {
        while (isThreadExecutorTasksRunning.get()) {
            if (stateHolder.isGameRunning()) {
                val state = stateHolder.getState()

                if (state.getNodeRole() != NodeRole.VIEWER) {
                    runCatching { stateHolder.getState().getConfig() }.onSuccess { config ->
                        ackConfirm(config)
                    }.onFailure { e ->
                        logger.warn("Game config is empty", e)
                    }
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

    private val pingTask = {
        while (isThreadExecutorTasksRunning.get()) {
            if (isPingTaskRunning.get() && stateHolder.isGameRunning()) {
                runCatching { stateHolder.getState().getConfig() }.onSuccess { config ->
                    var stateDelay: Int
                    synchronized(config) {
                        stateDelay = config.stateDelayMs / 10
                    }
                    synchronized(sentMessageTime) {
                        for (entry in sentMessageTime) {
                            val currentTime = System.currentTimeMillis()
                            if (entry.value > currentTime - stateDelay) {
                                sendMessage(Ping(entry.key))
                                entry.setValue(currentTime)
                            }
                        }
                    }
                }.onFailure { e ->
                    logger.warn("Game config is empty", e)
                }
            }
        }
    }

    private val announcementTask = {
        if (stateHolder.isGameRunning()) {
            val state = stateHolder.getState()
            if (state.getNodeRole() == NodeRole.MASTER) {
                runCatching { stateHolder.getGameAnnouncement() }.onSuccess { announcement ->
                    sendMessage(Announcement(context.networkConfig.groupAddress, listOf(announcement)))
                    logger.info("Sent announcement to nodes")
                }.onFailure { e ->
                    logger.warn("Game Announcement is empty", e)
                }
            }

        }
    }

    // Здесь мы смотрим является ли наша нода Deputy и после спрашиваем у нее
    // какие ноды просят вместо mastera у нас инфу об игре (GameState)
    private val deputyListenersTask = {
        if (stateHolder.isGameRunning()) {
            val state = stateHolder.getState()
            if (state.getNodeRole() == NodeRole.DEPUTY) {
                val messages: List<State> = state.getDeputyListeners()
                    .map { address -> State(address, stateHolder.getGameState()) }

                for (message in messages) {
                    sendMessage(message)
                    waitAckOnMessage(message)
                }

                logger.info("Sent state to deputy listeners")
            }
        }
    }


    init {
        threadExecutor.execute(receiveTask)
        threadExecutor.execute(ackConfirmationTask)
        threadExecutor.execute(pingTask)

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
            stateHolder.getState().getConfig().stateDelayMs.toLong(),
            TimeUnit.MILLISECONDS
        )

        logger.info("All tasks are running ")
    }

    fun stopTasks() {
        isThreadExecutorTasksRunning.set(false)
        isPingTaskRunning.set(false)

        threadExecutor.shutdown()
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


    //TODO перенести проверку
    private fun sendAnnouncement(address: InetSocketAddress) {
        if (stateHolder.isGameRunning() && stateHolder.getState().getNodeRole() == NodeRole.MASTER) {
            runCatching { stateHolder.getGameAnnouncement() }.onSuccess { announcement ->
                val message = Announcement(address, listOf(announcement))
                sendMessage(message)
            }.onFailure { e ->
                logger.warn("Game Announcement error", e)
            }
        }
    }

    fun sendAck(address: InetSocketAddress, msgSeq: Long, senderId: Int, receiverId: Int) {
        val ack = Ack(
            address = address,
            msgSeq = msgSeq,
            senderId = senderId,
            receiverId = receiverId
        )

        sendMessage(ack)
    }

    private fun sendMessage(message: Message) {
        senderController.sendMessage(message)

        if (message !is Announcement && message !is Ack && message !is Discover) {
            synchronized(sentMessageTime) {
                sentMessageTime[message.address] = System.currentTimeMillis()
            }
        }
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
            is Ack -> {/*не моожет быть*/
            }

            is Announcement -> {/*не моожет быть*/
            }

            is Discover -> {/*не моожет быть*/
            }

            is Error -> {/*в handleMessage */
            }

            is Join -> {
                val stateEditor = stateHolder.getStateEditor()
                stateEditor.setNodeId(ack.receiverId)
            }
//            gameController.acceptOurNodeJoin(ack.receiverId)
            is Ping -> synchronized(sentMessageTime) { sentMessageTime.remove(ack.address) }
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

            is Announcement -> {
                stateHolder.getStateEditor().addAnnouncements(message.address, message.games)
            }

            is Discover -> sendAnnouncement(message.address)
            is Error -> {
                stateHolder.getStateEditor().addError(message.errorMessage)
                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
            }

            is Join -> {
                val stateEditor = stateHolder.getStateEditor()

                if (!stateHolder.isGameRunning()) {
                    sendErrorMessage(message.address, "No game on node")
                    return
                }

                if (message.requestedRole == NodeRole.DEPUTY || message.requestedRole == NodeRole.MASTER) {
                    sendErrorMessage(message.address, "The requested role is not available to join the game with it")
                }

                runCatching {
                    GamePlayer(
                        message.playerName,
                        IdSequence.getNextId(),
                        message.address,
                        message.address.port,
                        message.requestedRole,
                        message.playerType,
                        0
                    )
                    //TODO продумать как передать игрока на основе acceptAnotherNodeJoin
                }.onSuccess {player ->
                    stateEditor.addPlayer(listOf(player))
                    sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
                }.onFailure { e ->
                    sendErrorMessage(message.address, e.message ?: "error")
                }
            }
//            runCatching {
//                gameController.acceptAnotherNodeJoin(
//                    message.address,
//                    message.playerType,
//                    message.playerName,
//                    message.gameName,
//                    message.requestedRole
//                )
//            }.onSuccess {
//                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
//            }.onFailure { e ->
//                sendErrorMessage(message.address, e.message ?: "error")
//            }

            is Ping -> sendAck(
                message.address,
                message.msgSeq,
                message.senderId,
                message.receiverId
            )

            is RoleChange -> runCatching {
                //TODO реализовать
                gameController.acceptRoleChange(message.senderRole, message.receiverRole, message.address)
            }.onSuccess {
                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
            }.onFailure { e ->
                sendErrorMessage(message.address, e.message ?: "error")
            }

            is State -> {
                gameController.acceptState(message.state)
                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
            }

            is Steer -> runCatching {
                gameController.acceptSteer(message.senderId, message.direction)
            }.onSuccess {
                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
            }.onFailure { e ->
                sendErrorMessage(message.address, e.message ?: "error")
            }
        }
    }
}