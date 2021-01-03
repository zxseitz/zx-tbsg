package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.ClientException;
import ch.zxseitz.tbsg.games.IClient;
import ch.zxseitz.tbsg.games.IEvent;
import ch.zxseitz.tbsg.games.IMatch;
import ch.zxseitz.tbsg.server.model.User;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Client implements IClient, Comparable<Client> {
    private final WebSocketSession session;
    private final User user;
    private final Lock lock;

    private final Set<Locker<Client>> challenges;
    private Locker<IMatch> matchLocker;

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

//    void lock() {
//        lock.lock();
//    }
//
//    void unlock() {
//        lock.unlock();
//    }

    Set<Locker<Client>> getChallenges() {
        return challenges;
    }

    Locker<IMatch> getMatch() {
        return matchLocker;
    }

    void setMatch(Locker<IMatch> match) {
        this.matchLocker = match;
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
