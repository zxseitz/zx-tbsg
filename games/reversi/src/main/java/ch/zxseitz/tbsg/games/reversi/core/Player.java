package ch.zxseitz.tbsg.games.reversi.core;

import ch.zxseitz.tbsg.games.ClientException;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.games.IEvent;

public class Player implements IClient {
    private final IClient client;
    private final int color;

    public Player(IClient client, int color) {
        this.client = client;
        this.color = color;
    }

    @Override
    public String getId() {
        return client.getId();
    }

    @Override
    public String getName() {
        return client.getName();
    }

    @Override
    public void invoke(IEvent event) throws ClientException {
        client.invoke(event);
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Player) {
            var other = (Player) o;
            return client.equals(other.client);
        }
        return false;
    }
}
