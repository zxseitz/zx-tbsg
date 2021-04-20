package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;
import ch.zxseitz.tbsg.games.annotations.Color;

import java.util.Collection;
import java.util.UUID;

@TbsgGame(name = "reversi", colors = {
        @Color(value = 1, name = "black"),
        @Color(value = 2, name = "white"),
})
public class Reversi implements IGame<Action> {
    private static synchronized String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public int compareTo(IGame<?> o) {
        return id.compareTo(o.getId());
    }

    private final String id;
    private final Board board;
    private int next;
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
    public int getNext() {
        return next;
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override
    public int[] getBoard() {
        return new int[0];
    }

    @Override
    public void update(Action action) {
        System.out.println("reversi: (" + next + ", " + action + ")");
    }

    @Override
    public Collection<Action> getPreview() {
        return null;
    }
}
