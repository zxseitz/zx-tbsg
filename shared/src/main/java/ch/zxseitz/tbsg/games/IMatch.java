package ch.zxseitz.tbsg.games;

import java.util.function.Consumer;

public interface IMatch {
    String getId();
    void init();
    boolean validatePlayer(int player);
    void connect(int player, Consumer<IEvent> handler);
    void disconnect(int player);
    void action(int player, IEvent event) throws EventException, GameException;
}
