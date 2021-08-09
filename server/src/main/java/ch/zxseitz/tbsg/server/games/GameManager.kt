package ch.zxseitz.tbsg.server.games

import ch.zxseitz.tbsg.games.IGame
import ch.zxseitz.tbsg.games.annotations.Color
import ch.zxseitz.tbsg.games.annotations.TbsgGame
import ch.zxseitz.tbsg.games.exceptions.GameException
import ch.zxseitz.tbsg.server.websocket.Client
import ch.zxseitz.tbsg.server.websocket.Match
import ch.zxseitz.tbsg.server.websocket.Protector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Component

import java.lang.reflect.ParameterizedType
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.collections.HashMap

@Component
class GameManager {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GameManager::class.java)
        private const val gameBasePackage: String = "ch.zxseitz.tbsg.games"
//    private val gameClassManifest: String = "Game-Class"

        // separate method for testing
        fun createProxy(gameClass: Class<out IGame<*>>): GameProxy {
            val interfaceType = Arrays.stream(gameClass.genericInterfaces)
                .map{type -> type as ParameterizedType}
                .filter{type -> type.rawType.equals(IGame::class.java)}
                .findFirst().orElseThrow{GameException("Missing interface IGame")}
            val actionClass = interfaceType.actualTypeArguments[0] as Class<*>
            val gameAnnotation = gameClass.getAnnotation(TbsgGame::class.java)
                ?: throw GameException("Missing @TbsgGame annotation in game class ${gameClass.simpleName}")
            val gameName = gameAnnotation.name
            val colors = Arrays.stream(gameAnnotation.colors)
                .collect(Collectors.toMap(Color::value, Color::name))
            if (colors[0] != null) {
                throw GameException("Color ${colors[0]} cannot have value 0.")
            }
            return GameProxy(gameName, colors, gameClass as Class<IGame<Any>>, actionClass)
        }

        fun createMatch(game: IGame<Any>, clients: Map<Int, Client>): Match {
            return Match(Protector(game), clients)
        }
    }

    private var proxies: MutableMap<String, GameProxy> = HashMap()

    init {
        proxies = HashMap(10)
        val provider = ClassPathScanningCandidateComponentProvider(false)
        provider.addIncludeFilter(AnnotationTypeFilter(TbsgGame::class.java))
        val classes = provider.findCandidateComponents(gameBasePackage)
        for (bean in classes) {
            val className = bean.beanClassName
            logger.info("scanning game class $className") //todo debug
            try {
                val gameClass = Class.forName(className) as Class<IGame<Any>>
                val gameProxy = createProxy(gameClass)
                proxies[gameProxy.name] = gameProxy
                logger.info("registered game {}", gameProxy.name)
            } catch (e: Exception) {
                logger.error(e.message)
            }
        }
    }

    val gameNames: Set<String>
        get() =  proxies.keys

    fun foreachGame(action: Consumer<GameProxy>) {
        proxies.values.forEach(action)
    }
}
