package ch.zxseitz.tbsg.games;

import org.junit.Assert;
import org.junit.Test;

public class SimpleEventTest {
    @Test
    public void testGetCode() {
        var event = new SimpleEvent(10);
        Assert.assertEquals(10, event.getCode());
        Assert.assertEquals(0, event.argumentSize());
    }

    @Test
    public void testGetArgument() {
        try {
            var event = new SimpleEvent(10);
            event.getArgument("x", Integer.class);
            Assert.fail();
        } catch (EventException e) {
            Assert.assertEquals("Argument x is missing", e.getMessage());
        }
    }

    @Test
    public void testForeachArgument() {
        var event = new SimpleEvent(10);
        event.foreachArgument((s, o) -> {
            Assert.fail();
        });
    }
}
