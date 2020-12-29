package ch.zxseitz.tbsg.games;

public class SimpleCommand implements ICommand {
    protected IPlayer sender;

    public SimpleCommand(IPlayer sender) {
        this.sender = sender;
    }

    @Override
    public IPlayer getSender() {
        return sender;
    }

    @Override
    public void execute(IMatch match) throws GameException {

    }
}
