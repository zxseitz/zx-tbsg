package ch.zxseitz.tbsg.server.websocket

import ch.zxseitz.tbsg.games.IGame
import ch.zxseitz.tbsg.server.games.GameManager
import ch.zxseitz.tbsg.server.games.GameProxy
import com.fasterxml.jackson.databind.JsonNode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

import java.util.concurrent.ConcurrentHashMap

import org.junit.Assert.*
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.api.mockito.PowerMockito.whenNew
import kotlin.collections.HashSet

@RunWith(PowerMockRunner::class)
@PrepareForTest(GameSocketHandler::class, ConcurrentHashMap::class,
    Client::class, GameManager::class, TextMessage::class)
class GameSocketHandlerTest {
    private val proxy: GameProxy
    private val lobby: ConcurrentHashMap<String, Client>

    init {
        this.proxy = mock(GameProxy::class.java)
        this.lobby = mock(ConcurrentHashMap::class.java) as ConcurrentHashMap<String, Client>
        whenNew(ConcurrentHashMap::class.java).withNoArguments().thenReturn(this.lobby)
        mockStatic(GameManager::class.java)
    }

    @Before
    fun setUp() {
        reset(proxy, lobby)
    }

    @Test
    fun testConnect() {
        val clientId = "clientId"
        val clientMessage = "client message"
        val session = mock(WebSocketSession::class.java)
        val client = mock(Client::class.java)
        doReturn(clientId).`when`(session).id
        doReturn(clientId).`when`(client).id
        whenNew(Client::class.java).withArguments(session).thenReturn(client)
        `when`(createIdMessage(client)).thenReturn(clientMessage)

        val handler = GameSocketHandler(proxy)
        handler.afterConnectionEstablished(session)

        verify(lobby, times(1))[clientId] = client
        verify(client, times(1)).send(clientMessage)
    }

    @Test
    fun testDisconnect() {
        val clientId = "clientId"
        val client = mock(Client::class.java)
        doReturn(clientId).`when`(client).id
        val session = mock(WebSocketSession::class.java)
        doReturn(clientId).`when`(session).id
//            doReturn(client).`when`(lobby).remove(clientId)
        val handler = GameSocketHandler(proxy)

        handler.afterConnectionClosed(session, CloseStatus.NORMAL)

        verify(lobby, times(1)).remove(clientId)
    }

    // TODO: message handler

    @Test
    fun testMessageChallenge() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val opponentChallengeMessage = "message"
        val message = PowerMockito.mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)
        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(opponent).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(client).challenges
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createChallengeMessage(client)).thenReturn(opponentChallengeMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        assertTrue(challenges.contains(opponent))
        verify(opponent, times(1)).send(opponentChallengeMessage)
    }

    @Test
    fun testMessageChallengeNoOpponent() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val errorMessage = "error"
        val message = PowerMockito.mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
//        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(null).`when`(lobby)[opponentId]
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createErrorMessage(anyString())).thenReturn(errorMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        verify(client, times(1)).send(errorMessage)
    }

    @Test
    fun testMessageChallengeAbort() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val opponentChallengeAbortMessage = "message"
        val message = PowerMockito.mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)
        challenges.add(opponent)
        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(opponent).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(client).challenges
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_ABORT, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createChallengeAbortMessage(client)).thenReturn(opponentChallengeAbortMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        assertFalse(challenges.contains(opponent))
        verify(opponent, times(1)).send(opponentChallengeAbortMessage)
    }

    @Test
    fun testMessageChallengeAbortNoOpponent() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val errorMessage = "error"
        val message = PowerMockito.mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)
        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(null).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(client).challenges
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_ABORT, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createErrorMessage(anyString())).thenReturn(errorMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        verify(client, times(1)).send(errorMessage)
    }

    @Test
    fun testMessageChallengeAbortNoChallenge() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val opponentChallengeAbortMessage = "message"
        val errorMessage = "error"
        val message = PowerMockito.mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)
        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(opponent).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(client).challenges
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_ABORT, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createChallengeAbortMessage(client)).thenReturn(opponentChallengeAbortMessage)
        `when`(createErrorMessage(anyString())).thenReturn(errorMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        assertFalse(challenges.contains(opponent))
        verify(opponent, never()).send(opponentChallengeAbortMessage)
        verify(client, times(1)).send(errorMessage)
    }

    @Test
    fun testMessageChallengeAccept() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val challengeAcceptMessage = "message"
        val gameInitMessageNext = "init1"
        val gameInitMessage = "init2"
        val board = intArrayOf()
        val preview: Collection<*> = emptyList<Any>()
        val message = PowerMockito.mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)
        val game = mock(IGame::class.java) as IGame<Any>
        val match = mock(Match::class.java)

        challenges.add(opponent)
        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(opponent).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(client).challenges
        doReturn(payload).`when`(message).payload
        doReturn(board).`when`(game).board
        doReturn(preview).`when`(game).preview
        doReturn(game).`when`(proxy).createGame()
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_ACCEPT, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createChallengeAcceptMessage(client)).thenReturn(challengeAcceptMessage)
        `when`(createGameInitNextMessage(1, board, preview)).thenReturn(gameInitMessageNext)
        `when`(createGameInitMessage(2, board)).thenReturn(gameInitMessage)
        `when`(GameManager.createMatch(eq(game), any())).thenReturn(match)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        assertFalse(challenges.contains(opponent))
        verify(opponent, times(1)).send(challengeAcceptMessage)
        verify(opponent, times(1)).send(gameInitMessageNext)
        verify(client, times(1)).send(gameInitMessage)
        verify(opponent, times(1)).match = match
        verify(client, times(1)).match = match
    }

    @Test
    fun testMessageChallengeAcceptNoOpponent() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val gameInitMessage = "init2"
        val errorMessage = "error"
        val board = intArrayOf()
        val preview: Collection<*> = emptyList<Any>()
        val message = PowerMockito.mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)
        val game = mock(IGame::class.java) as IGame<Any>
        val match = mock(Match::class.java)

        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(null).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(client).challenges
        doReturn(payload).`when`(message).payload
        doReturn(board).`when`(game).board
        doReturn(preview).`when`(game).preview
        doReturn(game).`when`(proxy).createGame()
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_ACCEPT, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createGameInitMessage(2, board)).thenReturn(gameInitMessage)
        `when`(createErrorMessage(anyString())).thenReturn(errorMessage)
        `when`(GameManager.createMatch(eq(game), any())).thenReturn(match)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        verify(client, never()).send(gameInitMessage)
        verify(client, never()).match = match
        verify(client, times(1)).send(errorMessage)
    }

    @Test
    fun testMessageChallengeAcceptNotChallenged() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val errorMessage = "message"
        val message = mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)

        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(opponent).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(opponent).challenges
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_ACCEPT, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createErrorMessage(anyString())).thenReturn(errorMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        assertFalse(challenges.contains(client))
        verify(client, times(1)).send(errorMessage)
    }

    @Test
    fun testMessageChallengeDecline() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val challengeDeclineMessage = "message"
        val message = mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)

        challenges.add(client)
        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(opponent).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(opponent).challenges
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_DECLINE, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createChallengeDeclineMessage(client)).thenReturn(challengeDeclineMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        assertFalse(challenges.contains(client))
        verify(opponent, times(1)).send(challengeDeclineMessage)
    }

    @Test
    fun testMessageChallengeDeclineNoOpponent() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val errorMessage = "message"
        val message = mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)

        challenges.add(client)
        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(null).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(opponent).challenges
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_DECLINE, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createErrorMessage(anyString())).thenReturn(errorMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        assertTrue(challenges.contains(client))
        verify(client, times(1)).send(errorMessage)
    }

    @Test
    fun testMessageChallengeDeclineNotChallenged() {
        val clientId = "clientId"
        val opponentId = "opponentId"
        val payload = "payload"
        val errorMessage = "message"
        val message = mock(TextMessage::class.java)
        val args = mock(JsonNode::class.java)
        val client = mock(Client::class.java)
        val opponent = mock(Client::class.java)
        val session = mock(WebSocketSession::class.java)
        val challenges = HashSet<Client>(1)

        doReturn(clientId).`when`(session).id
        doReturn(client).`when`(lobby)[clientId]
        doReturn(opponent).`when`(lobby)[opponentId]
        doReturn(challenges).`when`(opponent).challenges
        doReturn(payload).`when`(message).payload
        `when`(parseClientMessage(payload)).thenReturn(Pair(CLIENT_CHALLENGE_DECLINE, args))
        `when`(readClientArgument(args, "opponent", String::class.java)).thenReturn(opponentId)
        `when`(createErrorMessage(anyString())).thenReturn(errorMessage)

        val handler = GameSocketHandler(proxy)
        handler.handleTextMessage(session, message)

        assertFalse(challenges.contains(client))
        verify(client, times(1)).send(errorMessage)
    }
}
