package ch.zxseitz.tbsg.games;

public interface ICommand {
    IPlayer getSender();
    void execute(IMatch match) throws GameException;
}
