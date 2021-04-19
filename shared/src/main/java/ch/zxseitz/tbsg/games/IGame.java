package ch.zxseitz.tbsg.games;

import java.util.Collection;

public interface IGame extends Comparable<IGame> {
    String getId();
    int getNext();
    GameState getState();
    int[] getBoard();
//    Collection<A> getPreview();
//    void update(A action);
}
