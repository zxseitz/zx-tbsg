package ch.zxseitz.tbsg.games;

import java.util.function.Consumer;

public interface IMatch {
    String getId();
    void init();
    boolean validateId(int clientId);
    void connect(int clientId, Consumer<IEvent> handler);
    void disconnect(int clientId);
    void action(int clientId, IEvent event) throws EventException, GameException;
}
