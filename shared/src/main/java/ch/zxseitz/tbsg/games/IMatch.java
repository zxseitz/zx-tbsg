package ch.zxseitz.tbsg.games;

public interface IMatch {
    String getId();
    int getClientPos(IClient client);
    IClient getClient(int pos);
    void init() throws ClientException;
    void resign(IClient client) throws ClientException, GameException;
    void action(IClient sender, IEvent event) throws ClientException, EventException, GameException;
}
