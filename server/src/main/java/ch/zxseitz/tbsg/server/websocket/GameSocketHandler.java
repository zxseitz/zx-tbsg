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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class GameSocketHandler extends TextWebSocketHandler {
    private final Logger logger;
    private final GameProxy proxy;
    private final Map<String, Client> clients;
    private final Map<String, IMatch> matches;
    private final Map<String, IBasicCommand> commands;

    public GameSocketHandler(GameProxy proxy) {
        this.logger = LoggerFactory.getLogger(GameSocketHandler.class.getName() + ":" + proxy.getName());
        this.proxy = proxy;
        this.clients = new ConcurrentHashMap<>();
        this.matches = new ConcurrentHashMap<>();
        this.commands = new HashMap<>();

        commands.put("WAIT", (client, body) -> {
            criticalPlayerSection(() -> {
                if (client.getState() == Client.State.ONLINE) {
                    client.setState(Client.State.WAITING);
                } else {
                    client.send("ERROR:You are currently not online");
                }
                return null;
            }, client);
        });
        commands.put("WAITING", (client, body) -> {
            var waiting = clients.values().stream()
                    .filter(client1 -> client1.getState() == Client.State.WAITING)
                    .map(Client::toString)
                    .reduce((left, right) -> left + ";" + right);
            client.send("WAITING:" + waiting);
        });
        commands.put("CHALLENGE", (client, body) -> {
            var opponent = clients.get(body);
            if (opponent != null) {
                criticalPlayerSection(() -> {
                    if (client.getState() == Client.State.ONLINE && opponent.getState() == Client.State.WAITING) {
                        opponent.send("CHALLENGE:" + client.toString());
                        client.setState(Client.State.CHALLENGING);
                        opponent.setState(Client.State.CHALLENGED);
                    }
                    return null;
                }, client, opponent);
            }
        });
        commands.put("CANCEL", (client, body) -> {
            var opponent = clients.get(body);
            if (opponent != null) {
                criticalPlayerSection(() -> {
                    if (client.getState() == Client.State.CHALLENGING && opponent.getState() == Client.State.CHALLENGED) {
                        opponent.send("CANCEL:" + client.toString());
                        client.setState(Client.State.ONLINE);
                        opponent.setState(Client.State.WAITING);
                    }
                    return null;
                }, client, opponent);
            }
        });
        commands.put("DECLINE", (client, body) -> {
            var opponent = clients.get(body);
            if (opponent != null) {
                criticalPlayerSection(() -> {
                    if (client.getState() == Client.State.CHALLENGED && opponent.getState() == Client.State.CHALLENGING) {
                        opponent.send("DECLINE:" + client.toString());
                        client.setState(Client.State.WAITING);
                        opponent.setState(Client.State.ONLINE);
                    }
                    return null;
                }, client, opponent);
            }
        });
        commands.put("ACCEPT", (client, body) -> {
            var opponent = clients.get(body);
            if (opponent != null) {
                criticalPlayerSection(() -> {
                    if (client.getState() == Client.State.CHALLENGED && opponent.getState() == Client.State.CHALLENGING) {
                        opponent.send("ACCEPT:" + client.toString());
                        //todo init match
                        client.setState(Client.State.INGAME);
                        opponent.setState(Client.State.INGAME);
                    }
                    return null;
                }, client, opponent);
            }
        });
    }

    private void criticalPlayerSection(Callable<Void> action, Client... clients) throws Exception {
        var stream = Arrays.stream(clients).sorted();  // prevent deadlocks
        stream.forEach(Client::lock);
        try {
            action.call();
        } finally {
            stream.forEach(Client::unlock);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            var player = clients.get(session.getId());
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
        var player = new Client(session);
        clients.put(session.getId(), player);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("Websocket client disconnected: {}", session.getId());
        var player = clients.remove(session.getId());
        //todo handle disconnect
    }
}
