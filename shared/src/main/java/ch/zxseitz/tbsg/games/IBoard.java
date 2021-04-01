package ch.zxseitz.tbsg.games;

public interface IBoard extends Comparable<IBoard> {
    String getId();
    Color getNext();
    GameState getState();
}
