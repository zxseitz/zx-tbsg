package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.server.games.GameManager;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final GameManager gameManager;

    @Autowired
    public WebSocketConfig(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        gameManager.foreachGame(proxy -> {
            var path = String.join("/", "/socket/v1/games", proxy.getName());
            registry.addHandler(new GameSocketHandler(proxy), path)
                    .setAllowedOrigins("*")  // handles unexpected response code 403
                    .addInterceptors(
                            new HttpSessionHandshakeInterceptor() {
                                @Override
                                public void afterHandshake(ServerHttpRequest request,
                                                           ServerHttpResponse response,
                                                           WebSocketHandler wsHandler,
                                                           @Nullable Exception ex) {
                                    super.afterHandshake(request, response, wsHandler, ex);
                                }

                                @Override
                                public boolean beforeHandshake(ServerHttpRequest request,
                                                               ServerHttpResponse response,
                                                               WebSocketHandler wsHandler,
                                                               Map<String, Object> attributes) {
                                    //todo attach auth infos
                                    return true;
                                }
                            }
                    );
            logger.info("Registered new websocket connection for game {}: {}", proxy.getName(), path);
        });
    }
}
