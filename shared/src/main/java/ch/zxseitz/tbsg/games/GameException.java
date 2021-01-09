package ch.zxseitz.tbsg.games;

import ch.zxseitz.tbsg.TbsgException;

public class GameException extends TbsgException {
    private String game;

    public GameException(String game, String message) {
        super(message);
        this.game = game;
    }

    public String getGame() {
        return game;
    }
}
