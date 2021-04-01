package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.*;
import ch.zxseitz.tbsg.games.annotations.ClientEvent;
import ch.zxseitz.tbsg.games.annotations.EventArg;
import ch.zxseitz.tbsg.games.annotations.ServerEvent;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;

@TbsgGame(name = "reversi", serverEvents = {
        @ServerEvent(code = 2100, args = {"color", "board", "preview"}),
        @ServerEvent(code = 2101, args = {"color", "board"}),
        @ServerEvent(code = 2110, args = {"source", "board", "preview"}),
        @ServerEvent(code = 2111, args = {"source", "board"}),
        @ServerEvent(code = 2120, args = {"source", "board"}),
        @ServerEvent(code = 2121, args = {"source", "board"}),
        @ServerEvent(code = 2122, args = {"source", "board"}),
}, colors = {Color.BLACK, Color.WHITE}, board = Board.class)
public class Reversi {
    @ClientEvent(2000)
    public void place(Board board, @EventArg("x") int x, @EventArg("y") int y) {
        board.place(x, y);
    }
}
