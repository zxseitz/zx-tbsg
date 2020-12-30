package ch.zxseitz.tbsg.games;

import java.io.IOException;

public interface IClient {
    String getID();
    String getName();
    void send(String message) throws IOException;
}
