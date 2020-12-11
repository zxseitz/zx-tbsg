package ch.zxseitz.tbsg.games.reversi;

import ch.zxseitz.tbsg.games.IGame;
import ch.zxseitz.tbsg.games.TbsgGame;
import ch.zxseitz.tbsg.games.TbsgWebHook;

import java.io.InputStream;

@TbsgGame("reversi")
public class Reversi implements IGame {
    @Override
    public InputStream readFile(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    @Override
    public void invoke(String message) {

    }

    @TbsgWebHook(path = "index", method = TbsgWebHook.Method.GET)
    public String test(String json) {
        return "reversi test: " + json;
    }
}
