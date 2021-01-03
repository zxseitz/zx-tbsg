package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.ClientException;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.games.IEvent;
import ch.zxseitz.tbsg.server.model.User;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements IClient, Comparable<Client> {
    private final WebSocketSession session;
    private final User user;
    private final Lock lock;

    private final Set<Client> challenges;

    public Client(WebSocketSession session) {
        this(session, null);
    }

    public Client(WebSocketSession session, User user) {
        this.session = session;
        this.user = user;
        this.lock = new ReentrantLock();
        this.challenges = new HashSet<>();
    }

    @Override
    public String getId() {
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
    public void invoke(IEvent event) throws ClientException {
        try {
            session.sendMessage(new TextMessage(EventManager.stringify(event)));
        } catch (IOException e) {
            throw new ClientException("Cannot send event " + event.toString() + " to client " + toString(), e);
        }
    }

    public Set<Client> getChallenges() {
        return challenges;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    @Override
    public int compareTo(Client client) {
        return getId().compareTo(client.getId());
    }

    @Override
    public String toString() {
        return session.getId() + "," + getName();
    }
}
