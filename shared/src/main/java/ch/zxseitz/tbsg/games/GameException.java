package ch.zxseitz.tbsg.games;

public class GameException extends Exception {
    private String game;

    public GameException(String game, String message) {
        super(message);
        this.game = game;
    }

    public String getGame() {
        return game;
    }
}
