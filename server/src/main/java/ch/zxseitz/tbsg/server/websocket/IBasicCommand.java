package ch.zxseitz.tbsg.server.websocket;

@FunctionalInterface
public interface IBasicCommand {
    void execute(Client client, String body) throws Exception;
}
