package ch.zxseitz.tbsg.games;

public class SimpleCommand implements ICommand {
    protected IClient sender;

    public SimpleCommand(IClient sender) {
        this.sender = sender;
    }

    @Override
    public IClient getSender() {
        return sender;
    }

    @Override
    public void execute(IMatch match) throws GameException {

    }
}
