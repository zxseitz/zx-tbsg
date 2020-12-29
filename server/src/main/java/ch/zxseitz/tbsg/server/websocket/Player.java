package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.IPlayer;
import ch.zxseitz.tbsg.server.model.User;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Player implements IPlayer, Comparable<Player> {
    public enum State {
        ONLINE,
        WAITING,
        CHALLENGING,
        CHALLENGED,
        INGAME
    }

    private final WebSocketSession session;
    private final User user;
    private final Lock lock;

    private volatile State state;

    public Player(WebSocketSession session) {
        this(session, null);
    }

    public Player(WebSocketSession session, User user) {
        this.session = session;
        this.user = user;
        this.lock = new ReentrantLock();
        this.state = State.ONLINE;
    }

    @Override
    public String getID() {
        return session.getId();
    }

    @Override
    public String getName() {
        return user != null ? user.getUsername() : "guest";
    }

    public User getUser() {
        return user;
    }

    @Override
    public void send(String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    public void setState(State state) {
        this.state = state;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public State getState() {
        return state;
    }

    @Override
    public int compareTo(Player o) {
        return session.getId().compareTo(o.session.getId());
    }

    @Override
    public String toString() {
        return session.getId() + "," + getName();
    }
}
