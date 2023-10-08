package model.mappers

import me.ippolitov.fit.snakes.SnakesProto
import model.dto.core.*
import model.dto.messages.*
import model.exceptions.UndefinedMessageTypeError
import java.net.InetAddress

object ProtoMapper {

    fun toMessage(message: SnakesProto.GameMessage, address: InetAddress): Message {
        return if (message.hasAck())
            toAck(message, address)
        else if (message.hasAnnouncement())
            toAnnouncement(message.announcement, address)
        else if (message.hasDiscover())
            toDiscover(address)
        else if (message.hasJoin())
            toJoin(message.join, address)
        else if (message.hasError())
            toError(message.error, address)
        else if (message.hasPing())
            toPing(address)
        else if (message.hasRoleChange())
            toRoleChange(message, address)
        else if (message.hasState())
            toState(message.state, address)
        else if (message.hasSteer())
            toSteer(message.steer, address)
        else
            throw UndefinedMessageTypeError(message = "No match type of message")
    }

    private fun toAck(proto: SnakesProto.GameMessage, address: InetAddress): Ack {
        if (!proto.hasAck()) {
            throw UndefinedMessageTypeError(message = "Passed argument is not ack message")
        }

        return Ack(
            address = address,
            msgSeq = proto.msgSeq,
            receiverId = proto.receiverId,
            senderId = proto.senderId
        )
    }

    private fun toAnnouncement(proto: SnakesProto.GameMessage.AnnouncementMsg, address: InetAddress) = Announcement(
        address = address,
        games = proto.gamesList.map { game -> toGameAnnouncement(game) }
    )

    private fun toDiscover(address: InetAddress) = Discover(
        address = address,
    )

    private fun toJoin(proto: SnakesProto.GameMessage.JoinMsg, address: InetAddress) = Join(
        address = address,
        playerType = toPlayerType(proto.playerType),
        playerName = proto.playerName,
        gameName = proto.gameName,
        requestedRole = toNodeRole(proto.requestedRole),
    )

    private fun toError(proto: SnakesProto.GameMessage.ErrorMsg, address: InetAddress) = Error(
        address = address,
        errorMessage = proto.errorMessage
    )

    private fun toPing(address: InetAddress) = Ping(
        address = address
    )

    private fun toRoleChange(proto: SnakesProto.GameMessage, address: InetAddress): RoleChange {
        if (!proto.hasRoleChange()) {
            throw UndefinedMessageTypeError(message = "Passed argument is nor role change message")
        }

        return RoleChange(
            address = address,
            senderId = proto.senderId,
            receiverId = proto.receiverId,
            //TODO check
            senderRole = toNodeRole(proto.roleChange.senderRole),
            receiverRole = toNodeRole(proto.roleChange.receiverRole)
        )
    }

    private fun toState(proto: SnakesProto.GameMessage.StateMsg, address: InetAddress) = State(
        address = address,
        state = toGameState(proto.state)
    )

    private fun toSteer(proto: SnakesProto.GameMessage.SteerMsg, address: InetAddress) = Steer(
        address = address,
        direction = toDirection(proto.direction)
    )

    private fun toGameAnnouncement(proto: SnakesProto.GameAnnouncement) = GameAnnouncement(
        players = toGamePlayers(proto.players),
        config = toGameConfig(proto.config),
        canJoin = proto.canJoin,
        gameName = proto.gameName
    )


    private fun toGamePlayers(proto: SnakesProto.GamePlayers) = GamePlayers(
        players = proto.playersList.map { player -> toGamePlayer(player) }
    )

    private fun toGamePlayer(proto: SnakesProto.GamePlayer) = GamePlayer(
        name = proto.name,
        id = proto.id,
        ip = proto.ipAddress ?: GamePlayer.DEFAULT_IP,
        port = if (proto.hasPort()) proto.port else GamePlayer.DEFAULT_PORT,
        role = toNodeRole(proto.role),
        type = if (proto.hasType()) toPlayerType(proto.type) else GamePlayer.DEFAULT_PLAYER_TYPE,
        score = proto.score
    )

    private fun toSnake(proto: SnakesProto.GameState.Snake) = Snake(
        playerId = proto.playerId,
        points = proto.pointsList.map { point -> toCoord(point) },
        snakeState = toSnakeState(proto.state),
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
        snakes = proto.snakesList.map { snake -> toSnake(snake) },
        foods = proto.foodsList.map { food -> toCoord(food) },
        players = toGamePlayers(proto.players)
    )
}