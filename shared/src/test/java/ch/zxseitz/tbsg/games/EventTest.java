package ch.zxseitz.tbsg.games;

import org.junit.Assert;
import org.junit.Test;

public class EventTest {
    @Test
    public void testGetCode() {
        var event = new Event(10);
        Assert.assertEquals(10, event.getCode());
        Assert.assertEquals(0, event.argumentSize());
    }

    @Test
    public void testGetArgument() {
        try {
            var event = new Event(10);
            event.addArgument("x", 20);
            Assert.assertEquals(1, event.argumentSize());
            Assert.assertEquals(Integer.valueOf(20), event.getArgument("x", Integer.class));
        } catch (EventException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetArgumentUnknown() {
        try {
            var event = new Event(10);
            event.addArgument("x", 20);
            event.getArgument("y", Integer.class);
            Assert.fail();
        } catch (EventException e) {
            Assert.assertEquals("Argument y is missing", e.getMessage());
        }
    }

    @Test
    public void testGetArgumentInvalidType() {
        try {
            var event = new Event(10);
            event.addArgument("x", 20);
            event.getArgument("x", Float.class);
            Assert.fail();
        } catch (EventException e) {
            Assert.assertEquals("Argument x is not of type java.lang.Float", e.getMessage());
        }
    }

    @Test
    public void testForeachArgument() {
        var event = new Event(10);
        event.addArgument("x", 20);
        event.addArgument("y", "test");
        event.foreachArgument((s, o) -> {
            if (s.equals("x")) {
                Assert.assertEquals(20, o);
            } else if (s.equals("y")) {
                Assert.assertEquals("test", o);
            } else {
                Assert.fail("unknown argument " + s);
            }
        });
    }
}
