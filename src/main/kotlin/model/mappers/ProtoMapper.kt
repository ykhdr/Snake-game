package model.mappers

import me.ippolitov.fit.snakes.SnakesProto
import model.dto.messages.*
import model.exceptions.UndefinedMessageTypeError
import model.models.core.*
import java.net.InetSocketAddress

object ProtoMapper {

    fun toProtoMessage(message: Message): SnakesProto.GameMessage {
        return when (message) {
            is Ack -> toProtoAck(message)
            is Announcement -> toProtoAnnouncement(message)
            is Discover -> toProtoDiscover(message)
            is Error -> toProtoError(message)
            is Join -> toProtoJoin(message)
            is Ping -> toProtoPing(message)
            is RoleChange -> toProtoRoleChange(message)
            is State -> toProtoState(message)
            is Steer -> toProtoSteer(message)
        }
    }

    private fun toProtoAck(ack: Ack): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setAck(SnakesProto.GameMessage.AckMsg.newBuilder().build())
            .setSenderId(ack.senderId)
            .setReceiverId(ack.receiverId)
            .setMsgSeq(ack.msgSeq)
            .build()
    }

    private fun toProtoAnnouncement(announcement: Announcement): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setAnnouncement(
                SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                    .addAllGames(announcement.games.map { game -> toProtoGameAnnouncement(game) }.toList())
                    .build()
            )
            .setMsgSeq(announcement.msgSeq)
            .build()
    }

    private fun toProtoError(error: Error): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setError(
                SnakesProto.GameMessage.ErrorMsg.newBuilder()
                    .setErrorMessage(error.errorMessage)
                    .build()
            )
            .setMsgSeq(error.msgSeq)
            .build()
    }

    private fun toProtoJoin(join: Join): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setJoin(
                SnakesProto.GameMessage.JoinMsg.newBuilder()
                    .setPlayerType(toProtoPlayerType(join.playerType))
                    .setPlayerName(join.playerName)
                    .setGameName(join.gameName)
                    .setRequestedRole(toProtoNodeRole(join.requestedRole))
                    .build()
            )
            .setMsgSeq(join.msgSeq)
            .build()
    }

    private fun toProtoPing(ping: Ping): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setPing(SnakesProto.GameMessage.PingMsg.newBuilder().build())
            .setMsgSeq(ping.msgSeq)
            .build()
    }

    private fun toProtoRoleChange(message: RoleChange): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setRoleChange(
                SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                    .setSenderRole(toProtoNodeRole(message.senderRole))
                    .setReceiverRole(toProtoNodeRole(message.receiverRole))
                    .build()
            )
            .setReceiverId(message.receiverId)
            .setSenderId(message.senderId)
            .setMsgSeq(message.msgSeq)
            .build()
    }

    private fun toProtoDiscover(discover: Discover): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setDiscover(
                SnakesProto.GameMessage.DiscoverMsg.newBuilder()
                    .build()
            )
            .setMsgSeq(discover.msgSeq)
            .build()
    }

    private fun toProtoState(state: State): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setState(
                SnakesProto.GameMessage.StateMsg.newBuilder()
                    .setState(toProtoGameState(state.state))
                    .build()
            )
            .setMsgSeq(state.msgSeq)
            .setSenderId(state.senderId)
            .setReceiverId(state.receiverId)
            .build()
    }

    private fun toProtoSteer(steer: Steer): SnakesProto.GameMessage {
        return SnakesProto.GameMessage.newBuilder()
            .setSteer(
                SnakesProto.GameMessage.SteerMsg.newBuilder()
                    .setDirection(toProtoDirection(steer.direction))
                    .build()
            )
            .setMsgSeq(steer.msgSeq)
            .setSenderId(steer.senderId)
            .setReceiverId(steer.receiverId)
            .build()
    }

    private fun toProtoGameState(gameState: GameState): SnakesProto.GameState {
        return SnakesProto.GameState.newBuilder()
            .setStateOrder(gameState.stateOrder)
            .addAllSnakes(gameState.snakes.map { snake -> toProtoSnake(snake) })
            .addAllFoods(gameState.foods.map { food -> toProtoCoord(food) })
            .setPlayers(toProtoGamePlayers(gameState.players))
            .build()
    }

    private fun toProtoCoord(coord: Coord): SnakesProto.GameState.Coord {
        return SnakesProto.GameState.Coord.newBuilder()
            .setX(coord.x)
            .setY(coord.y)
            .build()
    }

    private fun toProtoSnake(snake: Snake): SnakesProto.GameState.Snake {
        return SnakesProto.GameState.Snake.newBuilder()
            .setPlayerId(snake.playerId)
            .addAllPoints(snake.points.map { coord -> toProtoCoord(coord) })
            .setState(toProtoSnakeState(snake.state))
            .setHeadDirection(toProtoDirection(snake.headDirection))
            .build()
    }

    private fun toProtoDirection(direction: Direction): SnakesProto.Direction {
        return SnakesProto.Direction.valueOf(direction.name)
    }

    private fun toProtoSnakeState(snakeState: SnakeState): SnakesProto.GameState.Snake.SnakeState {
        return SnakesProto.GameState.Snake.SnakeState.valueOf(snakeState.name)
    }

    private fun toProtoGameAnnouncement(announcement: GameAnnouncement): SnakesProto.GameAnnouncement {
        return SnakesProto.GameAnnouncement.newBuilder()
            .setPlayers(toProtoGamePlayers(announcement.players))
            .setConfig(toProtoConfig(announcement.config))
            .setCanJoin(announcement.canJoin)
            .setGameName(announcement.gameName)
            .build()
    }


    private fun toProtoConfig(config: GameConfig): SnakesProto.GameConfig {
        return SnakesProto.GameConfig.newBuilder()
            .setWidth(config.width)
            .setHeight(config.height)
            .setFoodStatic(config.foodStatic)
            .setStateDelayMs(config.stateDelayMs)
            .build()
    }

    private fun toProtoGamePlayers(gamePlayers: List<GamePlayer>): SnakesProto.GamePlayers {
        return SnakesProto.GamePlayers.newBuilder()
            .addAllPlayers(gamePlayers.map { player -> toProtoPlayer(player) })
            .build()
    }

    private fun toProtoPlayer(player: GamePlayer): SnakesProto.GamePlayer {
        return SnakesProto.GamePlayer.newBuilder()
            .setName(player.name)
            .setId(player.id)
            .setIpAddress(if (player.ip.address == null) GamePlayer.DEFAULT_STR_IP else player.ip.address.toString())
            .setPort(player.port)
            .setRole(toProtoNodeRole(player.role))
            .setType(toProtoPlayerType(player.type))
            .setScore(player.score)
            .build()
    }

    private fun toProtoNodeRole(nodeRole: NodeRole): SnakesProto.NodeRole {
        return SnakesProto.NodeRole.valueOf(nodeRole.name)
    }

    private fun toProtoPlayerType(playerType: PlayerType): SnakesProto.PlayerType {
        return SnakesProto.PlayerType.valueOf(playerType.name)
    }


    /**
     * @throws UndefinedMessageTypeError если переданное сообщение является неизвестным
     */
    fun toMessage(message: SnakesProto.GameMessage, address: InetSocketAddress): Message {
        return if (message.hasAck())
            toAck(message, address)
        else if (message.hasAnnouncement())
            toAnnouncement(message.announcement, message.msgSeq, address)
        else if (message.hasDiscover())
            toDiscover(address)
        else if (message.hasJoin())
            toJoin(message.join, message.msgSeq, address)
        else if (message.hasError())
            toError(message.error, message.msgSeq, address)
        else if (message.hasPing())
            toPing(address, message.msgSeq)
        else if (message.hasRoleChange())
            toRoleChange(message, message.msgSeq, address)
        else if (message.hasState())
            toState(message.state, address, message.msgSeq, message.senderId, message.receiverId)
        else if (message.hasSteer())
            toSteer(message.steer, address, message.msgSeq, message.senderId, message.receiverId)
        else
            throw UndefinedMessageTypeError(message = "No match type of message")
    }

    private fun toAck(proto: SnakesProto.GameMessage, address: InetSocketAddress) =
        Ack(
            address = address,
            msgSeq = proto.msgSeq,
            receiverId = proto.receiverId,
            senderId = proto.senderId
        )


    private fun toAnnouncement(
        proto: SnakesProto.GameMessage.AnnouncementMsg,
        msgSeq: Long,
        address: InetSocketAddress
    ) =
        Announcement(
            address = address,
            msgSeq = msgSeq,
            games = proto.gamesList.map { game -> toGameAnnouncement(game) }
        )

    //TODO добавить msgSeq
    private fun toDiscover(address: InetSocketAddress) = Discover(
        address = address,
    )

    private fun toJoin(proto: SnakesProto.GameMessage.JoinMsg, msgSeq: Long, address: InetSocketAddress) = Join(
        address = address,
        playerType = toPlayerType(proto.playerType),
        playerName = proto.playerName,
        gameName = proto.gameName,
        msgSeq = msgSeq,
        requestedRole = toNodeRole(proto.requestedRole),
    )

    private fun toError(proto: SnakesProto.GameMessage.ErrorMsg, msgSeq: Long, address: InetSocketAddress) = Error(
        address = address,
        msgSeq = msgSeq,
        errorMessage = proto.errorMessage
    )

    private fun toPing(address: InetSocketAddress, msgSeq: Long) = Ping(
        address = address,
        msgSeq = msgSeq
    )

    private fun toRoleChange(proto: SnakesProto.GameMessage, msgSeq: Long, address: InetSocketAddress): RoleChange {
        if (!proto.hasRoleChange()) {
            throw UndefinedMessageTypeError(message = "Passed argument is nor role change message")
        }

        return RoleChange(
            address = address,
            senderId = proto.senderId,
            receiverId = proto.receiverId,
            //TODO check
            senderRole = toNodeRole(proto.roleChange.senderRole),
            receiverRole = toNodeRole(proto.roleChange.receiverRole),
            msgSeq = msgSeq
        )
    }

    private fun toState(
        proto: SnakesProto.GameMessage.StateMsg,
        address: InetSocketAddress,
        msgSeq: Long,
        senderId: Int,
        receiverId: Int
    ) = State(
        address = address,
        senderId = senderId,
        receiverId = receiverId,
        state = toGameState(proto.state),
        msgSeq = msgSeq
    )

    private fun toSteer(
        proto: SnakesProto.GameMessage.SteerMsg,
        address: InetSocketAddress,
        msgSeq: Long,
        senderId: Int,
        receiverId: Int
    ) = Steer(
        address = address,
        senderId = senderId,
        receiverId = receiverId,
        direction = toDirection(proto.direction),
        msgSeq = msgSeq
    )

    private fun toGameAnnouncement(proto: SnakesProto.GameAnnouncement) = GameAnnouncement(
        players = proto.players.playersList.map { player -> toGamePlayer(player) }.toMutableList(),
        config = toGameConfig(proto.config),
        canJoin = proto.canJoin,
        gameName = proto.gameName
    )

    private fun toGamePlayer(proto: SnakesProto.GamePlayer) = GamePlayer(
        name = proto.name,
        id = proto.id,
        ip = InetSocketAddress(
            proto.ipAddress ?: GamePlayer.DEFAULT_STR_IP,
            if (proto.hasPort()) proto.port else GamePlayer.DEFAULT_PORT
        ),
        port = if (proto.hasPort()) proto.port else GamePlayer.DEFAULT_PORT,
        role = toNodeRole(proto.role),
        type = if (proto.hasType()) toPlayerType(proto.type) else GamePlayer.DEFAULT_PLAYER_TYPE,
        score = proto.score
    )

    private fun toSnake(proto: SnakesProto.GameState.Snake) = Snake(
        playerId = proto.playerId,
        points = proto.pointsList.map { point -> toCoord(point) },
        state = toSnakeState(proto.state),
        headDirection = toDirection(proto.headDirection)
    )

    private fun toCoord(proto: SnakesProto.GameState.Coord) = Coord(
        x = proto.x,
        y = proto.y
    )

    private fun toDirection(proto: SnakesProto.Direction) = Direction.valueOf(proto.name)

    private fun toSnakeState(proto: SnakesProto.GameState.Snake.SnakeState) = SnakeState.valueOf(proto.name)

    private fun toNodeRole(proto: SnakesProto.NodeRole): NodeRole {
        //TODO проверить
        return NodeRole.valueOf(proto.name)
    }

    private fun toPlayerType(proto: SnakesProto.PlayerType): PlayerType {
        //TODO проверить
        return PlayerType.valueOf(proto.name)
    }

    private fun toGameConfig(proto: SnakesProto.GameConfig): GameConfig {
        return GameConfig(
            width = if (proto.hasWidth()) proto.width else GameConfig.DEFAULT_WIDTH,
            height = if (proto.hasHeight()) proto.height else GameConfig.DEFAULT_HEIGHT,
            foodStatic = if (proto.hasFoodStatic()) proto.foodStatic else GameConfig.DEFAULT_FOOD_STATIC,
            stateDelayMs = if (proto.hasStateDelayMs()) proto.stateDelayMs else GameConfig.DEFAULT_STATE_DELAY_MS
        )
    }

    private fun toGameState(proto: SnakesProto.GameState) = GameState(
        stateOrder = proto.stateOrder,
        snakes = proto.snakesList.map { snake -> toSnake(snake) }.toMutableList(),
        foods = proto.foodsList.map { food -> toCoord(food) }.toMutableList(),
        players = proto.players.playersList.map { player -> toGamePlayer(player) }.toMutableList()
    )
}