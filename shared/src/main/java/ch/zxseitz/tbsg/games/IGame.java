package ch.zxseitz.tbsg.games;

import java.io.InputStream;

public interface IGame {
    InputStream readFile(String path);
    String invoke(String message);
}
