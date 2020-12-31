package ch.zxseitz.tbsg.games;

import java.io.IOException;
import java.io.InputStream;

public interface IGame {
    IEvent parse(IClient sender, String prefix, String body) throws EventException;
    InputStream readFile(String path) throws IOException;
    IMatch createMatch();
}
