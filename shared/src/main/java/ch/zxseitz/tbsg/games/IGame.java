package ch.zxseitz.tbsg.games;

import java.io.IOException;
import java.io.InputStream;

public interface IGame {
    ICommand parse(IClient sender, String prefix, String body) throws CommandException;
    InputStream readFile(String path) throws IOException;
    IMatch newMatch(String id, IClient... players);
}
