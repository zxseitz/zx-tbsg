package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.annotations.ClientUpdate;
import ch.zxseitz.tbsg.games.annotations.UpdateArg;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;

import java.util.UUID;

@TbsgGame(name = "reversi", colors = {Color.BLACK, Color.WHITE})
public class Reversi implements IGame {
    private static synchronized String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public int compareTo(IGame o) {
        return id.compareTo(o.getId());
    }

    private final String id;
    private final Board board;
    private Color next;
    private GameState state;

    public Reversi() {
        this.id = generateId();
        this.board = new Board();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Color getNext() {
        return next;
    }

    @Override
    public GameState getState() {
        return state;
    }

    @ClientUpdate
    public void update(@UpdateArg("index") int index) {
        System.out.println("reversi: (" + next.name() + ", " + index + ")");
    }
}
