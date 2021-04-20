package ch.zxseitz.tbsg.games;

import ch.zxseitz.tbsg.games.exceptions.GameException;

import java.util.Collection;

public interface IGame<T> extends Comparable<IGame<?>> {
    String getId();
    int getNext();
    GameState getState();
    int[] getBoard();
    Collection<T> getPreview();
    void update(T action) throws GameException;
}
