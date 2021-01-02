package ch.zxseitz.tbsg.server.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    private final Map<String, Client> clients;

    public Lobby() {
        this.clients = new ConcurrentHashMap<>();
    }

    public Map<String, Client> getClients() {
        return clients;
    }
}
