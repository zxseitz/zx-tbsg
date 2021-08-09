package ch.zxseitz.tbsg.server.websocket

import ch.zxseitz.tbsg.TbsgException
import ch.zxseitz.tbsg.games.GameState
import ch.zxseitz.tbsg.games.IGame
import ch.zxseitz.tbsg.games.exceptions.ClientException
import ch.zxseitz.tbsg.games.exceptions.GameException
import ch.zxseitz.tbsg.server.games.GameManager
import ch.zxseitz.tbsg.server.games.GameProxy

import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.*

class GameSocketHandler(private val proxy: GameProxy): TextWebSocketHandler() {
    private val logger: Logger = LoggerFactory.getLogger(GameSocketHandler::class.java.name + ":" + proxy.name)
    private val clients: MutableMap<String, Client> = ConcurrentHashMap()

    /**
     * Performs a critical section on several locks
     *
     * @param callable critical section
     * @param locks    list of lock
     * @return critical section return value
     * @throws Exception if an exception occurs during the critical section
     */
    private fun <T, L> safe(callable: () -> T, vararg locks: IProtectable<L>): T {
        val sorted = Arrays.stream(locks).sorted().collect(Collectors.toList())  //prevent deadlocks
        sorted.forEach(IProtectable<L>::lock)
        try {
            return callable()
        } finally {
            sorted.forEach(IProtectable<L>::unlock)
        }
    }

    /**
     * Handles client connection
     *
     * @param session client websocket session
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Connected client: ${session.id}")
        // todo: read auth infos
        val client = Client(session)
        clients[session.id] = client
        sendToClient(client, createIdMessage(client))
    }

    /**
     * Handles client disconnection
     *
     * @param session client websocket session
     * @param status  close status
     */
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("Disconnected client: ${session.id}")
        var client = clients.remove(session.id)
        // todo delete challenges and abort open matches
    }

    /**
     * Handles client events
     *
     * @param session client websocket session
     * @param message client event
     */
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val client = clients[session.id]!!

        try {
            val codeNode = parseClientMessage(message.payload)
            val messageCode = codeNode.first
            val argNode = codeNode.second
            // lobby client events
            when (messageCode) {
                CLIENT_CHALLENGE -> {
                    val opponentId = readClientArgument(argNode, "opponent", String::class.java)
                    val opponent = clients[opponentId] ?: throw TbsgException("Opponent [$opponentId] is not connected")
                    safe({
                        client.challenges.add(opponent)
                        opponent.send(createChallengeMessage(client))
                    }, client)
                }
                CLIENT_CHALLENGE_ABORT -> {
                    val opponentId = readClientArgument(argNode, "opponent", String::class.java)
                    val opponent = clients[opponentId] ?: throw TbsgException("Opponent [$opponentId] is not connected")
                    safe({
                        if (client.challenges.remove(opponent)) {
                            opponent.send(createChallengeAbortMessage(client))
                        } else {
                            throw TbsgException("Opponent [$opponentId] is not challenged by you")
                        }
                    }, client)
                }
                CLIENT_CHALLENGE_ACCEPT -> {
                    val opponentId = readClientArgument(argNode, "opponent", String::class.java)
                    val opponent = clients[opponentId] ?: throw TbsgException("Opponent [$opponentId] is not connected")
                    safe({
                        // todo queuing accepts
                        // fixme color values
                        if (client.match == null && opponent.match == null) {
                            if (opponent.challenges.remove(client)) {
                                opponent.send(createChallengeAcceptMessage(client))
                                val game = proxy.createGame()
                                val clients = TreeMap<Int, Client>()
                                clients[1] = client
                                clients[2] = opponent
                                val match = GameManager.createMatch(game, clients)
                                client.match = match
                                opponent.match = match
                                game.init()

                                sendToClient(client, createGameInitNextMessage(1, game.board, game.preview))
                                sendToClient(opponent, createGameInitMessage(2, game.board))
                            } else {
                                throw TbsgException("Opponent [$opponentId] is not challenged by you")
                            }
                        } else {
                            throw TbsgException("You or opponent [$opponentId] is currently in game")
                        }
                    }, client, opponent)
                }
                CLIENT_CHALLENGE_DECLINE -> {
                    val opponentId = readClientArgument(argNode, "opponent", String::class.java)
                    val opponent = clients[opponentId] ?: throw TbsgException("Opponent [$opponentId] is not connected")
                    safe({
                        if (opponent.challenges.remove(client)) {
                            opponent.send(createChallengeDeclineMessage(client))
                        } else {
                            throw TbsgException("Opponent [$opponentId] is not challenged by you")
                        }
                    }, opponent)
                }
                CLIENT_UPDATE -> {
                    val action = readClientGameArguments(argNode, proxy.actionClass)
                    safe({
                        val match = client.match!!
                        val gameProtector = match.game
                        val opponent = match.getOpponent(client)
                        safe({
                            val game = gameProtector.t
                            if (game.state == GameState.FINISHED) {
                                throw GameException("Game is already finished")
                            }
                            val color = match.getColor(client)
                            if (game.next != color) {
                                throw GameException("Not your turn")
                            }

                            game.update(action)

                            if (game.state == GameState.RUNNING) {
                                // game continues
                                val updateNextMessage = createGameUpdateNextMessage(action, game.board, game.preview)
                                val updateMessage = createGameUpdateMessage(action, game.board)
                                if (game.next == color) {
                                    sendToClient(client, updateNextMessage)
                                    sendToClient(opponent, updateMessage)
                                } else {
                                    sendToClient(client, updateMessage)
                                    sendToClient(opponent, updateNextMessage)
                                }
                            } else {
                                // game finished
                                val next = game.next
                                if (next == 0) {
                                    val tieMessage = createGameEndTieMessage(action, game.board)
                                    sendToClient(client, tieMessage)
                                    sendToClient(opponent, tieMessage)
                                } else {
                                    val victoryMessage = createGameEndVictoryMessage(action, game.board)
                                    val defeatMessage = createGameEndDefeatMessage(action, game.board)
                                    if (next == color) {
                                        sendToClient(client, victoryMessage)
                                        sendToClient(opponent, defeatMessage)
                                    } else {
                                        sendToClient(client, defeatMessage)
                                        sendToClient(opponent, victoryMessage)
                                    }
                                }
                            }
                        }, gameProtector)
                    }, client)
                }
                else -> {
                    sendToClient(client, createErrorMessage("unknown message code: $messageCode"))
                }
            }
        } catch (e: Exception) {
//            logger.warn(e.message, e)
            logger.warn(e.message)
            sendToClient(client, createErrorMessage(e.message!!))
        }
    }

    private fun sendToClient(client: Client, message: String) {
        try {
            //todo cache and try resending, recognize new client
            client.send(message)
        } catch (ce: ClientException) {
            logger.warn(ce.message)
        }
    }
}
