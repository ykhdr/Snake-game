package model.api

import model.api.controllers.ReceiverController
import model.api.controllers.SenderController
import model.dto.messages.*
import model.models.util.AckConfirmation
import model.models.contexts.NetworkContext
import model.models.core.*
import model.models.requests.tasks.DeputyListenTaskRequest
import model.states.StateHolder
import model.utils.IdSequence
import mu.KotlinLogging
import java.io.Closeable
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class MessageManager(
    context: NetworkContext
) : Closeable {
    companion object {
        private const val ANNOUNCEMENT_DELAY_MS = 1000L
        private const val ANNOUNCEMENT_INITIAL_DELAY_MS = 1000L
        private const val REQUEST_SENDER_DELAY_MS = 1000L
        private const val THREAD_POOL_SIZE = 10
    }

    private val stateHolder: StateHolder = context.stateHolder

    private val threadExecutor = Executors.newCachedThreadPool()
    private val scheduledExecutor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE)

    private val ackConfirmations = mutableListOf<AckConfirmation>()
    private val sentMessageTime = mutableMapOf<InetSocketAddress, Long>()

    private val receiverController: ReceiverController = ReceiverController(context.networkConfig)
    private val senderController: SenderController = SenderController(context.networkConfig)

    private var deputyListenTaskFuture: Optional<ScheduledFuture<*>> = Optional.empty()

    private val logger = KotlinLogging.logger {}


    private val receiveGroupMessagesTask = {
        runCatching {
            receiverController.receiveGroupMessage()
        }.onSuccess { message ->
            handleMessage(message)
            logger.warn("$message")
        }.onFailure { e ->
            logger.warn("Error on receiving data", e)
        }

        Unit
    }

    private val receiveNodeMessagesTask = {
        runCatching {
            receiverController.receiveNodeMessage()
        }.onSuccess { message ->
            handleMessage(message)
            logger.warn("$message")
        }.onFailure { e ->
            logger.warn("Error on receiving data", e)
        }

        Unit
    }

    // Подтерждения только в рамках игры
    private val ackConfirmationTask = {
        if (stateHolder.isNodeMaster()) {
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

    // TODO надо подумаьт над тем, что будет, когда ресивер отпадет и не будет отвечать совсем
    // TODO пересмотреть этот метод

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
                    runCatching {
                        receiverController.getReceivedAckByAddress(ackConfirmation.message.address)
                    }.onSuccess { ack ->
                        handleAckOnMessage(ackConfirmation.message, ack)
                    }.onFailure {
                        runCatching {
                            receiverController.getReceivedErrorByAddress(ackConfirmation.message.address)
                        }.onSuccess { message ->
                            handleMessage(message)
                        }
                    }

                }
            }
        }
    }

    private val pingTask = {
        if (stateHolder.isNodeMaster()) {
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
                        logger.info("Ping sent to ${entry.key.address.hostAddress}")
                    }

                }
            }.onFailure { e ->
                logger.warn("Game config is empty", e)
            }
        }
    }

    private val announcementTask = {
        if (stateHolder.isNodeMaster()) {
            runCatching { stateHolder.getGameAnnouncement() }.onSuccess { announcement ->
                sendMessage(Announcement(context.networkConfig.groupAddress, listOf(announcement)))
                logger.info("Sent announcement to nodes")
            }.onFailure { e ->
                logger.warn("Game Announcement is empty", e)
            }

        }
    }

    // Здесь мы смотрим является ли наша нода Deputy и после спрашиваем у нее
    // какие ноды просят вместо mastera у нас инфу об игре (GameState)
    //TODO сделать эту таску
    private val deputyListenersTask = {
        val state = stateHolder.getState()
        val nodeRole = state.getNodeRole()
        if (nodeRole == NodeRole.DEPUTY) {
//            val messages: List<State> =
//                state.getDeputyListeners().map { address -> State(address, stateHolder.getGameState()) }
//
//            for (message in messages) {
//                sendMessage(message)
//                waitAckOnMessage(message)
//            }

            logger.info("Sent state to deputy listeners")
        }
    }


    private val requestSenderTask = {
        val state = stateHolder.getState()
        if (state.isGameRunning()) {
            checkSteerRequest(state)
            checkLeaveRequest(state)
            checkDeputyListenRequest(state)
        }
        checkJoinRequest(state)
        logger.info("All request tasks checked")
    }

    private val stateTask = {
        if (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()


            runCatching {
                state.getCurNodePlayer()
            }.onSuccess { master ->
                val players = state.getPlayers()
                val gameState = stateHolder.getGameState()

                val messages = players.stream()
                    .filter { p -> p != master }
                    .map { p -> State(p.ip, master.id, p.id, gameState) }.toList()

                for (message in messages) {
                    sendMessage(message)
                    waitAckOnMessage(message)
                    logger.info("State sent to ${message.address}")
                }
            }.onFailure { e ->
                logger.warn("Error on sending state task", e)
            }

        }
    }


    init {
        scheduledExecutor.scheduleWithFixedDelay(
            receiveGroupMessagesTask, 0, 1, TimeUnit.MILLISECONDS
        )

        scheduledExecutor.scheduleWithFixedDelay(
            receiveNodeMessagesTask, 0, 1, TimeUnit.MILLISECONDS
        )

        scheduledExecutor.scheduleWithFixedDelay(
            ackConfirmationTask, 0, 400, TimeUnit.MILLISECONDS
        )

        scheduledExecutor.scheduleWithFixedDelay(
            pingTask, 0, 1000, TimeUnit.MILLISECONDS
        )

        scheduledExecutor.scheduleWithFixedDelay(
            announcementTask, ANNOUNCEMENT_INITIAL_DELAY_MS, ANNOUNCEMENT_DELAY_MS, TimeUnit.MILLISECONDS
        )

        scheduledExecutor.scheduleWithFixedDelay(
            requestSenderTask, 0, REQUEST_SENDER_DELAY_MS, TimeUnit.MILLISECONDS
        )

        scheduledExecutor.scheduleWithFixedDelay(
            stateTask, 0, 500, TimeUnit.MILLISECONDS
        )

        logger.info("All tasks are running ")
    }


    override fun close() {
        threadExecutor.shutdown()
        scheduledExecutor.shutdown()
        receiverController.close()
        senderController.close()

        logger.info("Sockets and executors closed")
    }


    private fun checkSteerRequest(state: model.states.State) {
        if (state.getSteerRequest().isEmpty) {
            return
        }

        val steer = state.getSteerRequest().get()
        sendSteerMessage(steer.address, steer.senderId, steer.receiverId, steer.direction)

        stateHolder.getStateEditor().clearSteerRequest()
        logger.info("Steer request confirmed")
    }

    private fun checkJoinRequest(state: model.states.State) {
        if (state.getJoinRequest().isEmpty) {
            return
        }

        val join = state.getJoinRequest().get()

        runCatching {
            sendJoinMessage(
                join.address, state.getPlayerName(), join.gameName, join.requestedRole
            )
        }.onFailure { e ->
            logger.warn("Error on sending join message", e)
        }.onSuccess {
            stateHolder.getStateEditor().clearJoinRequest()
            logger.info("Join request confirmed")
        }

    }

    private fun checkLeaveRequest(state: model.states.State) {


        if (state.getLeaveRequest().isEmpty) {
            return
        }

        val roleChange = state.getLeaveRequest().get()

//        if (roleChange.senderId == state.get)

        sendRoleChangeMessage(
            state.getGameAddress(),
            roleChange.senderId,
            roleChange.receiverId,
            roleChange.senderRole,
            roleChange.receiverRole
        )

        stateHolder.getStateEditor().clearLeaveRequest()
        logger.info("Leave request confirmed")
    }

    private fun checkDeputyListenRequest(state: model.states.State) {

        when (state.getDeputyListenTaskRequest()) {
            DeputyListenTaskRequest.RUN -> {
                if (deputyListenTaskFuture.isPresent) {
                    return
                }

                deputyListenTaskFuture = Optional.of(
                    scheduledExecutor.scheduleWithFixedDelay(
                        deputyListenersTask,
                        0,
                        stateHolder.getState().getConfig().stateDelayMs.toLong(),
                        TimeUnit.MILLISECONDS
                    )
                )

                stateHolder.getStateEditor().clearDeputyListenTaskToRun()
            }

            DeputyListenTaskRequest.STOP -> {
                if (deputyListenTaskFuture.isEmpty) {
                    return
                }

                deputyListenTaskFuture.get().cancel(true)

                stateHolder.getStateEditor().clearDeputyListenTaskToRun()
            }

            DeputyListenTaskRequest.DISABLE -> {
                // nothing
            }
        }

    }


    private fun sendSteerMessage(address: InetSocketAddress, senderId: Int, receiverId: Int, direction: Direction) {
        val message = Steer(address, senderId, receiverId, direction)

        sendMessage(message)
        waitAckOnMessage(message)
        logger.info("Steer message sent")
    }

    private fun sendErrorMessage(address: InetSocketAddress, errorMessage: String) {
        val message = Error(
            address = address, errorMessage = errorMessage
        )

        sendMessage(message)
        waitAckOnMessage(message)
        logger.info("Error message sent")
    }

    private fun sendJoinMessage(address: InetSocketAddress, playerName: String, gameName: String, role: NodeRole) {
        val message = Join(
            address = address, playerName = playerName, gameName = gameName, requestedRole = role
        )

        sendMessage(message)
        waitAckOnMessage(message)
        logger.info("Join message sent")
    }

    private fun sendRoleChangeMessage(
        address: InetSocketAddress, senderId: Int, receiverId: Int, senderRole: NodeRole, receiverRole: NodeRole
    ) {
        val message = RoleChange(address, senderId, receiverId, senderRole, receiverRole)

        sendMessage(message)
        waitAckOnMessage(message)
        logger.info("Role change message sent")
    }


    //TODO перенести проверку
    private fun sendAnnouncement(address: InetSocketAddress) {
        if (stateHolder.isNodeMaster() && stateHolder.getState().getNodeRole() == NodeRole.MASTER) {
            runCatching { stateHolder.getGameAnnouncement() }.onSuccess { announcement ->
                val message = Announcement(address, listOf(announcement))
                sendMessage(message)
                logger.info("Announcement sent")
            }.onFailure { e ->
                logger.warn("Game Announcement error", e)
            }
        }
    }

    private fun sendAck(address: InetSocketAddress, msgSeq: Long, senderId: Int, receiverId: Int) {
        val ack = Ack(
            address = address, msgSeq = msgSeq, senderId = senderId, receiverId = receiverId
        )

        sendMessage(ack)
        logger.info("Ack sent")
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
                stateEditor.setGameAddress(ack.address)
                logger.info("Join ack confirmed")
            }

            is Ping -> {
                synchronized(sentMessageTime) { sentMessageTime.remove(ack.address) }
                logger.info("Ping ack confirmed")
            }

            is RoleChange -> {
                stateHolder.getStateEditor().setNodeRole(message.senderRole)
                logger.info("Role change ack confirmed")
            }

            is State -> {}//TODO подумать как этом можно обработать
            is Steer -> {
                stateHolder.getStateEditor().updateSnakeDirection(message.senderId, message.direction)
                logger.info("Steer ack confirmed ")
            }
        }
    }

    /**
     * Обрабатывает приходящие от других нод сообщения
     */
    private fun handleMessage(message: Message) {
        when (message) {
            is Ack -> { /* за это отвечает специальная таска, никак не обрабатываем */
            }

            is Announcement -> {
                stateHolder.getStateEditor().addAnnouncement(message)
                logger.info("Announcement confirmed")
            }

            is Discover -> {
                sendAnnouncement(message.address)
                logger.info("Discover confirmed")
            }

            is Error -> {
                stateHolder.getStateEditor().addError(message.errorMessage)
                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
                logger.info("Error confirmed")
            }

            is Join -> {
                val stateEditor = stateHolder.getStateEditor()

                if (!stateHolder.isNodeMaster()) {
                    sendErrorMessage(message.address, "No game on node")
                    return
                }

                if (message.requestedRole == NodeRole.DEPUTY || message.requestedRole == NodeRole.MASTER) {
                    sendErrorMessage(message.address, "The requested role is not available to join the game with it")
                }

                val player = GamePlayer(
                    message.playerName,
                    IdSequence.getNextId(),
                    message.address,
                    message.address.port,
                    message.requestedRole,
                    message.playerType,
                    0
                )

                val curNodePlayer = stateHolder.getState().getCurNodePlayer()

                runCatching { stateEditor.addPlayerToAdding(player) }.onSuccess {
                    sendAck(message.address, message.msgSeq, curNodePlayer.id, player.id)
                    logger.info("Join confirmed")
                }.onFailure { e ->
                    sendErrorMessage(message.address, e.message ?: "error")
                    logger.warn("Error on other node join request", e)
                }
            }

            is Ping -> {
                sendAck(
                    message.address, message.msgSeq, message.senderId, message.receiverId
                )
                logger.info("Ping confirmed")
            }

            is RoleChange -> runCatching {
                stateHolder.getStateEditor().updateRole(message.address, message.senderRole, message.receiverRole)
            }.onSuccess {
                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
                logger.info("Role change confirmed")
            }.onFailure { e ->
                sendErrorMessage(message.address, e.message ?: "error")
                logger.warn("Error on role change", e)
            }

            is State -> {
                val state = stateHolder.getState()
                runCatching { state.getStateOrder() }.onSuccess { stateOrder ->
                    if (message.state.stateOrder > stateOrder) {
                        stateHolder.getStateEditor().setState(message.state)
                    }
                }.onFailure {
                    stateHolder.getStateEditor().setGameAddress(message.address)
                    stateHolder.getStateEditor().setNodeId(message.receiverId)
                    stateHolder.getStateEditor().setState(message.state)
//                    sendErrorMessage(message.address, e.message ?: "error")
//                    logger.warn("Error on state", e)
                }
                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
                logger.info("State confirmed")
            }

            is Steer -> runCatching {
                stateHolder.getStateEditor().updateSnakeDirection(message.senderId, message.direction)
            }.onSuccess {
                sendAck(message.address, message.msgSeq, message.receiverId, message.senderId)
                logger.info("Steer confirmed")
            }.onFailure { e ->
                sendErrorMessage(message.address, e.message ?: "error")
                logger.warn("Error on steer", e)
            }
        }
    }
}