package ch.zxseitz.tbsg.games;

import java.util.Set;

public interface IMatch extends Comparable<IMatch> {
    String getId();
    Set<IClient> getPlayers();
    void init() throws ClientException, GameException;
    void resign(IClient client) throws ClientException, GameException;
    void invoke(IClient sender, IEvent event) throws ClientException, EventException, GameException;
}
