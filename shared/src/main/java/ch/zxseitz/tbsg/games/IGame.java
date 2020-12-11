package ch.zxseitz.tbsg.games;

import java.io.InputStream;

public interface IGame {
    InputStream readFile(String path);
    void invoke(String message);
}
