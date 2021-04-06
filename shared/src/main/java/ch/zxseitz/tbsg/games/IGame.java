package ch.zxseitz.tbsg.games;

public interface IGame extends Comparable<IGame> {
    String getId();
    Color getNext();
    GameState getState();
//    int[] getBoard();
//    int[] getPreview();
}
