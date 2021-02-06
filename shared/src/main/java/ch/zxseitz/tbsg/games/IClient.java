package ch.zxseitz.tbsg.games;

public interface IClient {
    String getId();
    IPlayer getPlayer();
    void invoke(IEvent event) throws ClientException;
}
