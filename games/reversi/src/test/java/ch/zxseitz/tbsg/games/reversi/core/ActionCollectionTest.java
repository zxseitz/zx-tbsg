package ch.zxseitz.tbsg.games.reversi.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ActionCollectionTest {
    @Test
    public void testAnyIndicesEmpty() {
        var ac = new ActionCollection();
        Assert.assertFalse(ac.anyIndices());
    }

    @Test
    public void testAnyIndices() {
        var ac = new ActionCollection(Map.of(
                2, Set.of(1, 2, 3),
                3, Set.of(2, 4, 6)
        ));
        Assert.assertTrue(ac.anyIndices());
    }

    @Test
    public void testContainsIndex() {
        var ac = new ActionCollection(Map.of(
                2, Set.of(1, 2, 3),
                3, Set.of(2, 4, 6)
        ));
        Assert.assertTrue(ac.containsIndex(2));
        Assert.assertFalse(ac.containsIndex(5));
    }

    @Test
    public void testClear() {
        var ac = new ActionCollection(Map.of(
                2, Set.of(1, 2, 3),
                3, Set.of(2, 4, 6)
        ));
        ac.clear();
        Assert.assertFalse(ac.containsIndex(2));
        Assert.assertFalse(ac.containsIndex(3));
    }

    @Test
    public void testAddGet() {
        var ac = new ActionCollection();
        ac.add(2, Set.of(2, 3));
        Assert.assertEquals(Set.of(2, 3), ac.get(2));
    }

    @Test
    public void testForeach() {
        try {
            var ac = new ActionCollection(Map.of(
                    2, Set.of(1, 2, 3),
                    3, Set.of(2, 4, 6)
            ));
            var set = new TreeSet<Integer>();
            ac.foreach(2, set::add);
            Assert.assertEquals(Set.of(1, 2, 3), set);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testForeachInvalid() {
        try {
            var ac = new ActionCollection();
            var set = new TreeSet<Integer>();
            ac.foreach(2, set::add);
            Assert.fail();
        } catch (IllegalArgumentException iae) {
            Assert.assertEquals("collection does not contain index 2", iae.getMessage());
        }
    }
}
