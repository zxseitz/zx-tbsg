package ch.zxseitz.tbsg.server.websocket

import ch.zxseitz.tbsg.server.games.GameManager

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Configuration
@EnableWebSocket
open class WebSocketConfig @Autowired constructor(
    private val gameManager: GameManager
) : WebSocketConfigurer {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(WebSocketConfig::class.java)
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        gameManager.foreachGame { proxy ->
            val clients = Lobby()
            val path = "/socket/v1/games/${proxy.name}"
            registry.addHandler(GameSocketHandler(proxy, clients), path)
                .setAllowedOrigins("*")  // handles unexpected response code 403
                .addInterceptors(object : HttpSessionHandshakeInterceptor() {
                    override fun afterHandshake(
                        request: ServerHttpRequest,
                        response: ServerHttpResponse,
                        wsHandler: WebSocketHandler,
                        ex: Exception?
                    ) {
                        super.afterHandshake(request, response, wsHandler, ex)
                    }

                    override fun beforeHandshake(
                        request: ServerHttpRequest,
                        response: ServerHttpResponse,
                        wsHandler: WebSocketHandler,
                        attributes: Map<String, *>
                    ): Boolean {
                        //todo attach auth infos
                        return true
                    }
                })
            logger.info("Registered new websocket connection for game {}: {}", proxy.name, path)
        }
    }
}
