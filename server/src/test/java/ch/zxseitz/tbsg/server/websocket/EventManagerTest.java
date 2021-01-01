package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.IEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.mockito.Mockito.*;

public class EventManagerTest {
    private final EventManager eventManager;
    private final IEvent event;

    public EventManagerTest() {
        eventManager = new EventManager();
        event = mock(IEvent.class);
    }

    @Before
    public void setUp() {
        reset(event);
    }

    @Test
    public void testParseNoArgs() {
        try {
            var json = "{\"code\": 1001, \"args\": {}}";
            var event = eventManager.parse(json);
            Assert.assertEquals(1001, event.getCode());
            Assert.assertEquals(0, event.argumentSize());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testParseArgs() {
        try {
            var json = "{\"code\": 1001, \"args\": {\"x\": 2, \"y\": 3}}";
            var event = eventManager.parse(json);
            Assert.assertEquals(1001, event.getCode());
            Assert.assertEquals(2, event.argumentSize());
            Assert.assertEquals(Integer.valueOf(2), event.getArgument("x", Integer.class));
            Assert.assertEquals(Integer.valueOf(3), event.getArgument("y", Integer.class));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testStringifyNoArgs() {
        try {
            var args = Map.of();
            doReturn(1001).when(event).getCode();
            doAnswer(invocation -> {
                args.forEach(invocation.getArgument(0));
                return null;
            }).when(event).foreachArgument(Mockito.any());

            Assert.assertEquals("{\"code\":1001,\"args\":{}}",
                    eventManager.stringify(event));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testStringifyArgs() {
        try {
            var args = Map.of("x", 2, "y", 3);
            doReturn(1001).when(event).getCode();
            doAnswer(invocation -> {
                args.forEach(invocation.getArgument(0));
                return null;
            }).when(event).foreachArgument(Mockito.any());

            var json = eventManager.stringify(event);
            Assert.assertTrue(json.equals("{\"code\":1001,\"args\":{\"x\":2,\"y\":3}}")
                    || json.equals("{\"code\":1001,\"args\":{\"y\":3,\"x\":2}}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlainStringifyNoArgs() {
        try {
            Assert.assertEquals("{\"code\":1001,\"args\":{}}",
                    eventManager.stringify(1001));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testPlainStringifyArgs() {
        try {
            Assert.assertEquals("{\"code\":1001,\"args\":{\"x\":2,\"y\":3}}",
                    eventManager.stringify(1001, Map.entry("x", 2), Map.entry("y", 3)));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
