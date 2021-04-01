package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.exceptions.ClientException;
import ch.zxseitz.tbsg.server.model.User;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Client implements Comparable<Client>, IProtectable<Client> {
    private final WebSocketSession session;
    private final User user;
    private final Lock lock;

    private final Set<Client> challenges;
    private Match match;

    public Client(WebSocketSession session) {
        this(session, null);
    }

    public Client(WebSocketSession session, User user) {
        this.session = session;
        this.user = user;
        this.lock = new ReentrantLock();
        this.challenges = new HashSet<>();
    }

    public String getId() {
        return session.getId();
    }

    public IPlayer getPlayer() {
        return user;
    }

    public void send(String message) throws ClientException {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            throw new ClientException("Cannot send message " + message + " to client " + toString(), e);
        }
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    Set<Client> getChallenges() {
        return challenges;
    }

    Match getMatch() {
        return match;
    }

    void setMatch(Match match) {
        this.match = match;
    }

    @Override
    public int compareTo(Client client) {
        return getId().compareTo(getId());
    }

    @Override
    public String toString() {
        return session.getId() + "," + (user != null ? user.getUsername() : "guest");
    }
}
