package ch.zxseitz.tbsg.games;

public interface IMatch {
    String getId();
    void init();
    void connect(IClient client, int pos) throws GameException;
    void disconnect(IClient client) throws GameException;
    int getClientPos(IClient client);
    IClient getClient(int pos);
    void action(IClient sender, IEvent event) throws EventException, GameException;
}
