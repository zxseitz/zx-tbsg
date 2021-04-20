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
    @TbsgGame(name = "testgame", actionClass = TestAction.class, colors = {
            @Color(value = 1, name = "black"),
            @Color(value = 2, name = "white"),
    })
    private static class TestGame implements IGame {
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

    private static class TestGameMissingAnnotation implements IGame {
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

    @TbsgGame(name = "testgame", actionClass = TestAction.class, colors = {
            @Color(value = 0, name = "black")
    })
    private static class TestGameInvalidColor implements IGame {
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

    @TbsgGame(name = "testgame", actionClass = TestAction.class, colors = {
            @Color(value = 1, name = "black"),
            @Color(value = 2, name = "white")
    })
    private static class TestGameMissingUpdateMethod implements IGame {
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

        @ClientNext
        public Collection<TestAction> getPreview() {
            return null;
        }
    }

    @Test
    public void testCreateProxyMissingUpdateMethod() {
        try {
            GameManager.createProxy(TestGameMissingUpdateMethod.class);
            Assert.fail();
        } catch (GameException ignore) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @TbsgGame(name = "testgame", actionClass = TestAction.class, colors = {
            @Color(value = 1, name = "black"),
            @Color(value = 2, name = "white")
    })
    private static class TestGameInvalidUpdateMethod implements IGame {
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
        public int update(Object object) {
            return 0;
        }

        @ClientNext
        public Collection<TestAction> getPreview() {
            return null;
        }
    }

    @Test
    public void testCreateProxyInvalidUpdateMethod() {
        try {
            GameManager.createProxy(TestGameInvalidUpdateMethod.class);
            Assert.fail();
        } catch (GameException ignore) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @TbsgGame(name = "testgame", actionClass = TestAction.class, colors = {
            @Color(value = 1, name = "black"),
            @Color(value = 2, name = "white")
    })
    private static class TestGameInvalidMissingNextMethod implements IGame {
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
    }

    @Test
    public void testCreateProxyMissingNextMethod() {
        try {
            GameManager.createProxy(TestGameInvalidMissingNextMethod.class);
            Assert.fail();
        } catch (GameException ignore) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @TbsgGame(name = "testgame", actionClass = TestAction.class, colors = {
            @Color(value = 1, name = "black"),
            @Color(value = 2, name = "white")
    })
    private static class TestGameInvalidNextMethod implements IGame {
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
        public void update(TestAction object) {}

        @ClientNext
        public Collection<Object> getPreview() {
            return null;
        }
    }

    @Test
    public void testCreateProxyInvalidNextMethod() {
        try {
            GameManager.createProxy(TestGameInvalidNextMethod.class);
            Assert.fail();
        } catch (GameException ignore) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}

//            var gameAnnotationMock = mock(TbsgGame.class);
//            var blackAnnotationMock = mock(Color.class);
//            var whiteAnnotationMock = mock(Color.class);
//            var gameClassMock = mock(Class.class);
//            var actionClassMock = mock(Class.class);
//            var updateMethod = mock(Method.class);
//            var nextMethod = mock(Method.class);
//            var updateParam = mock(Parameter.class);
//            doReturn("testgame").when(gameAnnotationMock).name();
//            doReturn(actionClassMock).when(gameAnnotationMock).actionClass();
//            doReturn(new Color[]{
//                    blackAnnotationMock, whiteAnnotationMock
//            }).when(gameAnnotationMock).colors();
//            doReturn(1).when(blackAnnotationMock).value();
//            doReturn("black").when(blackAnnotationMock).name();
//            doReturn(2).when(whiteAnnotationMock).value();
//            doReturn("white").when(whiteAnnotationMock).name();
//            doReturn(true).when(IGame.class.isAssignableFrom(gameClassMock));
//            doReturn(new Method[]{
//                    updateMethod, nextMethod
//            }).when(gameClassMock).getMethods();
//            doReturn(true).when(updateMethod).isAnnotationPresent(ClientUpdate.class);
//            doReturn(void.class).when(updateMethod).getReturnType();
//            doReturn(1).when(updateMethod).getParameterCount();
//            doReturn(new Class[] {actionClassMock}).when(updateMethod).getParameterTypes();
//            doReturn(true).when(nextMethod).isAnnotationPresent(ClientNext.class);
//            doReturn(Collection.class).when(updateMethod).getReturnType();
//            doReturn(0).when(updateMethod).getParameterCount();
