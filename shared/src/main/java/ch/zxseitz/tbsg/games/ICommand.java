package ch.zxseitz.tbsg.games;

public interface ICommand {
    IClient getSender();
    void execute(IMatch match) throws GameException;
}
