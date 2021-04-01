package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.Color;
import ch.zxseitz.tbsg.games.IBoard;

public class Match {
    private final Protector<IBoard> board;
    private final Client black;
    private final Client white;

    public Match(Protector<IBoard> board, Client black, Client white) {
        this.board = board;
        this.black = black;
        this.white = white;
    }

    public Protector<IBoard> getBoard() {
        return board;
    }

    public Color getColor(Client client) {
        return black.equals(client) ? Color.BLACK : white.equals(client) ? Color.WHITE : Color.UNDEFINED;
    }

    public Client getOpponent(Client client) {
        return black.equals(client) ? white : white.equals(client) ? black : null;
    }
}
