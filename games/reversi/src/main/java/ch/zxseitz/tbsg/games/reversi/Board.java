package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Board implements IBoard {
    private static synchronized String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public int compareTo(IBoard o) {
        return id.compareTo(o.getId());
    }

    private final String id;
    private int[][] fields;
    protected List<IAudit> audits;
    private Color next;
    private GameState state;

    public Board() {
        this.id = generateId();
        this.audits = new ArrayList<>(60);
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

    public List<IAudit> audits() {
        return audits;
    }

    void place(int x, int y) {

    }
}
