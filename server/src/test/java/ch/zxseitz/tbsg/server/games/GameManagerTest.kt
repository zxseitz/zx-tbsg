package ch.zxseitz.tbsg.server.games

import ch.zxseitz.tbsg.games.GameState
import ch.zxseitz.tbsg.games.IGame
import ch.zxseitz.tbsg.games.annotations.Color
import ch.zxseitz.tbsg.games.annotations.TbsgGame
import ch.zxseitz.tbsg.games.exceptions.GameException

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class GameManagerTest {
    @Test
    fun test() {
        assertEquals(1, 1)
    }

//    class TestAction
//
//    @TbsgGame(
//        name = "testgame", colors = [
//            Color(value = 1, name = "black"),
//            Color(value = 2, name = "white"),
//        ]
//    )
//    class TestGame() : IGame<TestAction> {
//        override fun compareTo(other: IGame<*>): Int {
//            return 0
//        }
//
//        override val id: String = "id"
//        override val next: Int = -1
//        override val state: GameState = GameState.CREATED
//        override val board: IntArray = intArrayOf()
//        override val preview: Collection<TestAction> = arrayListOf()
//
//        override fun init() {
//        }
//
//        override fun update(action: TestAction) {
//        }
//    }
//
//    @Test
//    fun testCreateProxy() {
//        val proxy = GameManager.createProxy(TestGame::class.java)
//        assertEquals("testgame", proxy.name)
//        assertEquals("black", proxy.colors[1])
//        assertEquals("white", proxy.colors[2])
//        assertEquals(TestAction::class.java, proxy.actionClass)
//    }
//
//    class TestGameMissingAnnotation : IGame<TestAction> {
//        override fun compareTo(other: IGame<*>): Int {
//            return 0
//        }
//
//        override val id: String = "id"
//        override val next: Int = -1
//        override val state: GameState = GameState.CREATED
//        override val board: IntArray = intArrayOf()
//        override val preview: Collection<TestAction> = arrayListOf()
//
//        override fun init() {
//        }
//
//        override fun update(action: TestAction) {
//        }
//    }
//
//    @Test
//    fun testCreateProxyMissingAnnotation() {
//        try {
//            GameManager.createProxy(TestGameMissingAnnotation::class.java)
//            fail()
//        } catch (ignore: GameException) {
//        }
//    }
//
//    @TbsgGame(
//        name = "testgame", colors = [
//            Color(value = 0, name = "black")
//        ]
//    )
//    class TestGameInvalidColor : IGame<TestAction> {
//        override fun compareTo(other: IGame<*>): Int {
//            return 0
//        }
//
//        override val id: String = "id"
//        override val next: Int = -1
//        override val state: GameState = GameState.CREATED
//        override val board: IntArray = intArrayOf()
//        override val preview: Collection<TestAction> = arrayListOf()
//
//        override fun init() {
//        }
//
//        override fun update(action: TestAction) {
//        }
//    }
//
//    @Test
//    fun testCreateProxyInvalidColor() {
//        try {
//            GameManager.createProxy(TestGameInvalidColor::class.java)
//            fail()
//        } catch (ignore: GameException) {
//        }
//    }
}
