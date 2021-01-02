package ch.zxseitz.tbsg.games;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface IGame {
    InputStream readFile(String path) throws IOException;
    IMatch createMatch(List<IClient> clients);
}
