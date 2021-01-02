package ch.zxseitz.tbsg.games;

import java.io.IOException;

public interface IClient extends Comparable<IClient> {
    String getID();
    String getName();
    void invoke(IEvent event);
//    void send(String message) throws IOException;
}
