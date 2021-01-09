package ch.zxseitz.tbsg.games.reversi.exceptions;

import ch.zxseitz.tbsg.games.GameException;
import ch.zxseitz.tbsg.games.reversi.Reversi;

public class ReversiException extends GameException {
    public ReversiException(String message) {
        super("reversi", message);
    }
}
