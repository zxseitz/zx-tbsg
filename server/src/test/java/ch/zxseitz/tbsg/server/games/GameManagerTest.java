package ch.zxseitz.tbsg.server.games;

import ch.zxseitz.tbsg.games.GameState;
import ch.zxseitz.tbsg.games.IGame;
import ch.zxseitz.tbsg.games.annotations.ClientNext;
import ch.zxseitz.tbsg.games.annotations.ClientUpdate;
import ch.zxseitz.tbsg.games.annotations.Color;
import ch.zxseitz.tbsg.games.annotations.TbsgGame;
import ch.zxseitz.tbsg.games.exceptions.GameException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;


public class GameManagerTest {
    private static class TestAction {}
    @TbsgGame(name = "testgame", colors = {
            @Color(value = 1, name = "black"),
            @Color(value = 2, name = "white"),
    })
    private static class TestGame implements IGame<TestAction> {
        @Override
        public String getId() {
            return null;
        }

        @Override
        public int getNext() {
            return 0;
        }

        @Override
        public GameState getState() {
            return null;
        }

        @Override
        public int[] getBoard() {
            return new int[0];
        }

        @Override
        public int compareTo(IGame o) {
            return 0;
        }

        @ClientUpdate
        public void update(TestAction action) {

        }

        @ClientNext
        public Collection<TestAction> getPreview() {
            return null;
        }
    }

    @Test
    public void testCreateProxy() {
        try {
            var proxy = GameManager.createProxy(TestGame.class);
            Assert.assertEquals("testgame", proxy.getName());
            Assert.assertEquals("black", proxy.getColor(1));
            Assert.assertEquals("white", proxy.getColor(2));
            Assert.assertEquals(TestAction.class, proxy.getActionClass());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private static class TestGameMissingAnnotation implements IGame<TestAction> {
        @Override
        public String getId() {
            return null;
        }

        @Override
        public int getNext() {
            return 0;
        }

        @Override
        public GameState getState() {
            return null;
        }

        @Override
        public int[] getBoard() {
            return new int[0];
        }

        @Override
        public int compareTo(IGame o) {
            return 0;
        }

        @ClientUpdate
        public void update(TestAction action) {

        }

        @ClientNext
        public Collection<TestAction> getPreview() {
            return null;
        }
    }

    @Test
    public void testCreateProxyMissingAnnotation() {
        try {
            GameManager.createProxy(TestGameMissingAnnotation.class);
            Assert.fail();
        } catch (GameException ignore) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @TbsgGame(name = "testgame", colors = {
            @Color(value = 0, name = "black")
    })
    private static class TestGameInvalidColor implements IGame<TestAction> {
        @Override
        public String getId() {
            return null;
        }

        @Override
        public int getNext() {
            return 0;
        }

        @Override
        public GameState getState() {
            return null;
        }

        @Override
        public int[] getBoard() {
            return new int[0];
        }

        @Override
        public int compareTo(IGame o) {
            return 0;
        }

        @ClientUpdate
        public void update(TestAction action) {

        }

        @ClientNext
        public Collection<TestAction> getPreview() {
            return null;
        }
    }

    @Test
    public void testCreateProxyInvalidColor() {
        try {
            GameManager.createProxy(TestGameInvalidColor.class);
            Assert.fail();
        } catch (GameException ignore) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
