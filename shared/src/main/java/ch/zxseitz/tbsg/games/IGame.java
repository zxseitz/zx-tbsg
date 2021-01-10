package ch.zxseitz.tbsg.games;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface IGame {
    IMatch createMatch(List<IClient> clients);
    InputStream readFile(Path path) throws IOException;
    Set<String> listStyles();
    Set<String> listScripts();
}
