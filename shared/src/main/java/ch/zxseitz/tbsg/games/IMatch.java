package ch.zxseitz.tbsg.games;

public interface IMatch {
    String getId();
    void init();
    void resign(IClient client) throws GameException;
    int getClientPos(IClient client);
    IClient getClient(int pos);
    void action(IClient sender, IEvent event) throws EventException, GameException;
}
