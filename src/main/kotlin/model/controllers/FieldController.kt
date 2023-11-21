package model.controllers

import model.models.contexts.Context
import model.models.core.*
import model.utils.IdSequence
import mu.KotlinLogging
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.random.Random

class FieldController(
    context: Context
) : Closeable {
    private data class FieldSize(
        val width: Int,
        val height: Int,
    )

    companion object {
        private const val SCHEDULED_PULL_SIZE = 4
        private const val INITIAL_DELAY = 0L
        private const val SCAN_FIELD_TASK_DELAY = 200L
        private const val CREATING_SNAKES_TASK_DELAY = 300L
        private const val SPAWN_FOOD_TASK_DELAY = 1000L
        private const val CREATE_GAME_TASK_DELAY = 300L
    }

    private val stateHolder = context.stateHolder

    private var fieldSize = FieldSize(0, 0)

    private var isNewGame = AtomicBoolean(false)

    private val schedulerExecutor = Executors.newScheduledThreadPool(SCHEDULED_PULL_SIZE)

    private val logger = KotlinLogging.logger {}

    private val scanFieldTask = {
        if (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()
            val config = state.getConfig()

            // TODO посмотреть, надо ли тут это вообще
            if (isNewGame.get()) {
                fieldSize = FieldSize(config.width, config.height)
            }

            val snakeCoords = state.getSnakes().stream().flatMap { snake -> snake.points.stream() }.toList()
            val foodCoords = state.getFoods()

            val availableCoords = findAvailableCoords(snakeCoords, foodCoords)
            //TODO сделать проверку на то когда у нас четыре еды вокруг одной клетки
            stateHolder.getStateEditor().updateAvailableCoords(availableCoords)
            logger.info("Available coords updated")
        }
    }

    private val spawnFoodTask = {
        if (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()
            val config = state.getConfig()
            val foods = state.getFoods()

            if (foods.size != config.foodStatic) {
                val newFoods = mutableListOf<Coord>()

                for (i in 0..config.foodStatic - foods.size) {
                    var coord = Coord((0..config.width).random(), (0..config.height).random())
                    while (foods.contains(coord)) {
                        coord = Coord((0..config.width).random(), (0..config.height).random())
                    }
                    newFoods.add(coord)
                }

                stateHolder.getStateEditor().addFoods(newFoods)
                logger.info("Food spawn")
            }
        }
    }

    private val moveSnakesTask = {
        if (stateHolder.isNodeMaster()) {
            val state = stateHolder.getState()

            val newCoords = mutableListOf<Coord>()
            val snakes = state.getSnakes().toMutableList()
            val foods = state.getFoods().toMutableList()
            val players = state.getPlayers().toMutableList()

            for (snake in snakes) {
                val direction = snake.headDirection
                val curCoord = snake.points[0]

                val newCoord: Coord

                when (direction) {
                    Direction.UP -> {
                        newCoord = if (curCoord.y == 0) {
                            Coord(curCoord.x, fieldSize.height - 1)
                        } else {
                            Coord(curCoord.x, curCoord.y - 1)
                        }

                    }

                    Direction.DOWN -> {
                        newCoord = if (curCoord.y == fieldSize.height - 1) {
                            Coord(curCoord.x, 0)
                        } else {
                            Coord(curCoord.x, curCoord.y + 1)
                        }
                    }

                    Direction.LEFT -> {
                        newCoord = if (curCoord.x == 0) {
                            Coord(fieldSize.width - 1, curCoord.y)
                        } else {
                            Coord(curCoord.x - 1, curCoord.y)
                        }
                    }

                    Direction.RIGHT -> {
                        newCoord = if (curCoord.x == fieldSize.width - 1) {
                            Coord(0, curCoord.y)
                        } else {
                            Coord(curCoord.x + 1, curCoord.y)
                        }
                    }
                }

                newCoords.add(newCoord)
                //TODO дальше итерируемся по змейкам и смотрим их координаты. - DONE
                //TODO обработатать случай, когда сразу несколько змеек врезаются в одну клетку - DONE
                //TODO то есть Сначала мы выситываем новые координаты змеек, а уже потом только смотрим врезались ли они или нет

                //TODO просмотреть случай когда следующая клетка в направлении пустая, еда, другая змейка
            }

            val snakesToDelete = mutableListOf<Snake>()
            val coordsToDelete = mutableListOf<Coord>()
            val deadPlayers = mutableListOf<GamePlayer>()


            for (i in 0 until newCoords.size) {
                val coord = newCoords[i]

                for (j in i + 1 until newCoords.size) {
                    if (coord == newCoords[j]) {
                        val otherSnake = snakes[j]
                        if (otherSnake !in snakesToDelete)
                            snakesToDelete.add(otherSnake)

                        coordsToDelete.add(coord)
                        //TODO нужно у игроков поменять + Ловить исключение
                        val player = players.stream().filter { p -> p.id == otherSnake.playerId }.findFirst().get()
                        players[j] = player.copy(role = NodeRole.VIEWER, score = 0)
                        deadPlayers.add(players[j])
                    }
                }
            }

            //
            //TODO нужно ли как то уведомлять игроков?
            players.removeAll(deadPlayers)
            snakes.removeAll(snakesToDelete)
            newCoords.removeAll(coordsToDelete)

            for (i in 0..newCoords.size) {
                val snake = snakes[i]
                val coord = newCoords[i]
                val newSnakePoints = mutableListOf<Coord>()

                newSnakePoints.add(coord)
                newSnakePoints.addAll(snake.points)

                if (coord in foods) {
                    foods.remove(coord)
                    //TODO ловить исключение
                    val player = players.stream().filter { p -> p.id == snake.playerId }.findFirst().get()
                    players[i] = player.copy(score = player.score + 1)

                } else {
                    //TODO ловить исключение
                    newSnakePoints.removeLast()
                }

                snakes[i] = snake.copy(points = newSnakePoints)
            }

            stateHolder.getStateEditor().setFoods(foods)
            stateHolder.getStateEditor().setSnakes(snakes)
            stateHolder.getStateEditor().updatePlayers(players)
            stateHolder.getStateEditor().updatePlayers(deadPlayers)

        }
    }

    private val creatingSnakesTask = {
        if (stateHolder.isNodeMaster()) {
            logger.info("Create snake task run")
            val state = stateHolder.getState()
            val playersToAdding = state.getPlayersToAdding()
            val availableCoords = state.getAvailableCoords().toMutableList()
            for (player in playersToAdding) {
                if (availableCoords.isEmpty()) {
                    break
                }
                val headCoord = availableCoords.first()
                val bodyCoord = getRandomSecondSnakeCoord(headCoord)
                val snake = Snake(
                    player.id,
                    listOf(headCoord, bodyCoord),
                    SnakeState.ALIVE,
                    getHeadDirection(headCoord, bodyCoord)
                )

                availableCoords.remove(headCoord)
                stateHolder.getStateEditor().removePlayerToAdding(player)
                stateHolder.getStateEditor().updateAvailableCoords(availableCoords)
                stateHolder.getStateEditor().addSnake(snake)
                logger.info("Snake for player ${player.id} created")
            }
        }
    }

    private val createGameTask = {
        val state = stateHolder.getState()
        logger.warn("$state")
        val gameCreateRequestOpt = state.getGameCreateRequest()
        if (gameCreateRequestOpt.isPresent) {
            val gameCreateRequest = gameCreateRequestOpt.get()

            stateHolder.getStateEditor().setGameConfig(gameCreateRequest.gameConfig)
            stateHolder.getStateEditor().setGameName(gameCreateRequest.gameName)

            val player = GamePlayer(
                name = stateHolder.getState().getPlayerName(),
                id = IdSequence.getNextId(),
                role = NodeRole.MASTER,
            )

            fieldSize = FieldSize(gameCreateRequest.gameConfig.width, gameCreateRequest.gameConfig.height)

            val availableCoords = findAvailableCoords(emptyList(), state.getFoods())
            stateHolder.getStateEditor().setNodeRole(NodeRole.MASTER)
            stateHolder.getStateEditor().updateAvailableCoords(availableCoords)
            stateHolder.getStateEditor().addPlayerToAdding(player)
            stateHolder.getStateEditor().clearGameCreateRequest()
            logger.info("Game created")
        }
    }

    init {
        schedulerExecutor.scheduleWithFixedDelay(
            scanFieldTask,
            INITIAL_DELAY,
            SCAN_FIELD_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )
        schedulerExecutor.scheduleWithFixedDelay(
            creatingSnakesTask,
            INITIAL_DELAY,
            CREATING_SNAKES_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )
        schedulerExecutor.scheduleWithFixedDelay(
            spawnFoodTask,
            INITIAL_DELAY,
            SPAWN_FOOD_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )
        schedulerExecutor.scheduleWithFixedDelay(
            createGameTask,
            INITIAL_DELAY,
            CREATE_GAME_TASK_DELAY,
            TimeUnit.MILLISECONDS
        )

        logger.info("FieldController tasks running")
    }

    private fun findAvailableCoords(snakesCoords: List<Coord>, foodCoords: List<Coord>): List<Coord> {
        val allCoords = (0 until fieldSize.width).flatMap { x ->
            (0 until fieldSize.height).map { y -> Coord(x, y) }
        }.toMutableList()

        for (snakeCoord in snakesCoords) {
            allCoords.removeAll { coord ->
                (abs(coord.x - snakeCoord.x) <= 2 && abs(coord.y - snakeCoord.y) <= 2)
            }
        }

        allCoords.removeAll { coord -> foodCoords.contains(coord) }

        return allCoords
    }

    //TODO сделать проверку на координаты соседние
    private fun getRandomSecondSnakeCoord(headCoord: Coord): Coord {
        return when (Random.nextInt(0, 4)) {
            0 -> Coord(headCoord.x - 1, headCoord.y)
            1 -> Coord(headCoord.x + 1, headCoord.y)
            2 -> Coord(headCoord.x, headCoord.y + 1)
            3 -> Coord(headCoord.x, headCoord.y - 1)
            else -> Coord(-1, -1)
        }
    }

    private fun getHeadDirection(headCoord: Coord, bodyCoord: Coord): Direction {
        return if (bodyCoord.x > headCoord.x) {
            Direction.LEFT
        } else if (bodyCoord.x < headCoord.x) {
            Direction.RIGHT
        } else if (bodyCoord.y > headCoord.y) {
            Direction.DOWN
        } else {
            Direction.UP
        }
    }

    override fun close() {
        schedulerExecutor.shutdown()
        logger.info("Executor closed")
    }
}