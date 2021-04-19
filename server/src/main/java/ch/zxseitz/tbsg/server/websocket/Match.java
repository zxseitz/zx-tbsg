package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.Color;
import ch.zxseitz.tbsg.games.IGame;

import java.util.Map;

// 1v1 match
public class Match {
    private final Protector<IGame> board;
    private final Map<Integer, Client> clients;

    public Match(Protector<IGame> board, Map<Integer, Client> clients) {
        this.board = board;
        this.clients = clients;
    }

    public Protector<IGame> getGame() {
        return board;
    }

    public int getColor(Client client) {
        return clients.entrySet().stream()
                .filter(entry -> entry.getValue().equals(client))
                .mapToInt(Map.Entry::getKey)
                .findFirst().orElse(0);
    }

    public Client getOpponent(Client client) {
        return clients.values().stream()
                .filter(client1 -> !client1.equals(client))
                .findFirst().orElse(null);
    }
}
