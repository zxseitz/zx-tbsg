package ch.zxseitz.tbsg.server.websocket;

@FunctionalInterface
public interface IBasicCommand {
    void execute(Player player, String body) throws Exception;
}
