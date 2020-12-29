package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.CommandException;
import ch.zxseitz.tbsg.games.GameException;
import ch.zxseitz.tbsg.games.IMatch;
import ch.zxseitz.tbsg.server.games.GameProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class GameSocketHandler extends TextWebSocketHandler {
    private final Logger logger;
    private final GameProxy proxy;
    private final Map<String, Player> sessions;
    private final Map<String, IMatch> matches;
    private final Map<String, IBasicCommand> commands;

    public GameSocketHandler(GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(GameSocketHandler.class.getName() + ":" + proxy.getName());
        this.proxy = proxy;
        this.sessions = new ConcurrentHashMap<>();
        this.matches = new ConcurrentHashMap<>();
        this.commands = new HashMap<>();

        commands.put("WAIT", (player, body) -> {
            criticalPlayerSection(() -> {
                if (player.getState() == Player.State.ONLINE) {
                    player.setState(Player.State.WAITING);
                } else {
                    player.send("ERROR:You are currently not online");
                }
                return null;
            }, player);
        });
        commands.put("WAITING", (player, body) -> {
            var waiting = sessions.values().stream()
                    .filter(player1 -> player1.getState() == Player.State.WAITING)
                    .map(Player::toString)
                    .reduce((left, right) -> left + ";" + right);
            player.send("WAITING:" + waiting);
        });
        commands.put("CHALLENGE", (player, body) -> {
            var opponent = sessions.get(body);
            if (opponent != null) {
                criticalPlayerSection(() -> {
                    if (player.getState() == Player.State.ONLINE && opponent.getState() == Player.State.WAITING) {
                        opponent.send("CHALLENGE:" + player.toString());
                        player.setState(Player.State.CHALLENGING);
                        opponent.setState(Player.State.CHALLENGED);
                    }
                    return null;
                }, player, opponent);
            }
        });
        commands.put("CANCEL", (player, body) -> {
            var opponent = sessions.get(body);
            if (opponent != null) {
                criticalPlayerSection(() -> {
                    if (player.getState() == Player.State.CHALLENGING && opponent.getState() == Player.State.CHALLENGED) {
                        opponent.send("CANCEL:" + player.toString());
                        player.setState(Player.State.ONLINE);
                        opponent.setState(Player.State.WAITING);
                    }
                    return null;
                }, player, opponent);
            }
        });
        commands.put("DECLINE", (player, body) -> {
            var opponent = sessions.get(body);
            if (opponent != null) {
                criticalPlayerSection(() -> {
                    if (player.getState() == Player.State.CHALLENGED && opponent.getState() == Player.State.CHALLENGING) {
                        opponent.send("DECLINE:" + player.toString());
                        player.setState(Player.State.WAITING);
                        opponent.setState(Player.State.ONLINE);
                    }
                    return null;
                }, player, opponent);
            }
        });
        commands.put("ACCEPT", (player, body) -> {
            var opponent = sessions.get(body);
            if (opponent != null) {
                criticalPlayerSection(() -> {
                    if (player.getState() == Player.State.CHALLENGED && opponent.getState() == Player.State.CHALLENGING) {
                        opponent.send("ACCEPT:" + player.toString());
                        //todo init match
                        player.setState(Player.State.INGAME);
                        opponent.setState(Player.State.INGAME);
                    }
                    return null;
                }, player, opponent);
            }
        });
    }

    private void criticalPlayerSection(Callable<Void> action, Player... players) throws Exception {
        var stream = Arrays.stream(players).sorted();
        stream.forEach(Player::lock);
        try {
            action.call();
        } finally {
            stream.forEach(Player::unlock);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            var player = sessions.get(session.getId());
            var body = message.getPayload();
            String[] seq = body.split(":");
            var basicCommand = commands.get(seq[0]);
            if (basicCommand != null) {
                basicCommand.execute(player, seq[1]);
            } else {
                var gameCommand = proxy.getInstance().parse(player, seq[0], seq[1]);
                // todo handle match id
                gameCommand.execute(null);
            }
        } catch (GameException | CommandException ge) {
            // todo send error message
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New websocket client: {}", session.getId());
        // todo: read auth infos
        var player = new Player(session);
        sessions.put(session.getId(), player);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Websocket client disconnected: {}", session.getId());
        var player = sessions.remove(session.getId());
        //todo handle disconnect
    }
}
