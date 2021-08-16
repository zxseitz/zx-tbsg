package ch.zxseitz.tbsg.server.websocket

import ch.zxseitz.tbsg.games.exceptions.ActionException
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MessageManagerTest {
    class TestAction @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    constructor(@JsonProperty("index") val index: Int)

    companion object {
        private const val clientId = "client"
        private const val opponentId = "opponent"
    }

    @MockK(relaxed = true)
    private lateinit var client: Client
    @MockK(relaxed = true)
    private lateinit var opponent: Client

    init {
        MockKAnnotations.init(this)
    }

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun testParseClientMessage() {
        val json = """{"code": 1001, "args": {}}"""
        val message = parseClientMessage(json)
        assertEquals(1001, message.first)
        assertEquals(0, message.second.size())
    }

    @Test
    fun testParseArgs() {
        val json = """{"code": 1001, "args": {"x": 2, "y": 3}}"""
        val message = parseClientMessage(json)
        assertEquals(1001, message.first)
        assertEquals(2, message.second.size())
        assertEquals(2, message.second.get("x").asInt())
        assertEquals(3, message.second.get("y").asInt())
    }

    @Test
    fun testReadClientGameArgs() {
        val mapper = ObjectMapper()
        val node = mapper.createObjectNode()
        node.put("index", 4)
        val action = readClientGameArguments(node, TestAction::class.java)
        assertEquals(TestAction::class.java, action.javaClass)
        assertEquals(4, (action as TestAction).index)
    }

    @Test
    fun testReadClientInvalidGameArgs() {
        try {
            val mapper = ObjectMapper()
            val node = mapper.createObjectNode()
            node.put("x", 4)
            readClientGameArguments(node, TestAction::class.java)
            fail()
        } catch (ignore: ActionException) {
        }
    }

    @Test
    fun testCreateChallengeMessage() {
        every { client.id } returns clientId
        assertEquals(
                """{"code":$SERVER_CHALLENGE,"args":{"opponent":"$clientId"}}""",
                createChallengeMessage(client)
        )
    }

    @Test
    fun testCreateChallengeAbortMessage() {
        every { client.id } returns clientId
        assertEquals(
                """{"code":$SERVER_CHALLENGE_ABORT,"args":{"opponent":"$clientId"}}""",
                createChallengeAbortMessage(client)
        )
    }

    @Test
    fun testCreateChallengeAcceptMessage() {
        every { client.id } returns clientId
        assertEquals(
                """{"code":$SERVER_CHALLENGE_ACCEPT,"args":{"opponent":"$clientId"}}""",
                createChallengeAcceptMessage(client)
        )
    }

    @Test
    fun testCreateChallengeDeclineMessage() {
        every { client.id } returns clientId
        assertEquals(
                """{"code":$SERVER_CHALLENGE_DECLINE,"args":{"opponent":"$clientId"}}""",
                createChallengeDeclineMessage(client)
        )
    }

    @Test
    fun testCreateGameInitNextMessage() {
        assertEquals(
                """{"code":$SERVER_GAME_INIT_NEXT,"args":{"color":1,"board":[0,1,2,3],"preview":[{"index":1},{"index":4}]}}""",
                createGameInitNextMessage(
                        1, intArrayOf(0, 1, 2, 3),
                        listOf(TestAction(1), TestAction(4))
                )
        )
    }

    @Test
    fun testCreateGameInitMessage() {
        assertEquals(
                """{"code":$SERVER_GAME_INIT,"args":{"color":1,"board":[0,1,2,3]}}""",
                createGameInitMessage(1, intArrayOf(0, 1, 2, 3))
        )
    }

    @Test
    fun testCreateGameUpdateNextMessage() {
        assertEquals(
                """{"code":$SERVER_GAME_UPDATE_NEXT,"args":{"source":{"index":0},"board":[0,1,2,3],"preview":[{"index":1},{"index":4}]}}""",
                createGameUpdateNextMessage(
                        TestAction(0), intArrayOf(0, 1, 2, 3),
                        listOf(TestAction(1), TestAction(4))
                )
        )
    }

    @Test
    fun testCreateGameUpdateMessage() {
        assertEquals(
                """{"code":$SERVER_GAME_UPDATE,"args":{"source":{"index":0},"board":[0,1,2,3]}}""",
                createGameUpdateMessage(TestAction(0), intArrayOf(0, 1, 2, 3))
        )
    }

    @Test
    fun testCreateGameEndVictoryMessage() {
        assertEquals(
                """{"code":$SERVER_GAME_END_VICTORY,"args":{"source":{"index":0},"board":[0,1,2,3]}}""",
                createGameEndVictoryMessage(TestAction(0), intArrayOf(0, 1, 2, 3))
        )
    }

    @Test
    fun testCreateGameEndDefeatMessage() {
        assertEquals(
                """{"code":$SERVER_GAME_END_DEFEAT,"args":{"source":{"index":0},"board":[0,1,2,3]}}""",
                createGameEndDefeatMessage(TestAction(0), intArrayOf(0, 1, 2, 3))
        )
    }

    @Test
    fun testCreateGameEndTieMessage() {
        assertEquals(
                """{"code":$SERVER_GAME_END_TIE,"args":{"source":{"index":0},"board":[0,1,2,3]}}""",
                createGameEndTieMessage(TestAction(0), intArrayOf(0, 1, 2, 3))
        )
    }
}
