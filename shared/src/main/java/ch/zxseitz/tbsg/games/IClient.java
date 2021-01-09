package ch.zxseitz.tbsg.games;

public interface IClient {
    String getId();
    String getName();
    void invoke(IEvent event) throws ClientException;
}
