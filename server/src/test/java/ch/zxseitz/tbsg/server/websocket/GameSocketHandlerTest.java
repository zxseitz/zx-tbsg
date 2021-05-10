package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.server.games.GameProxy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GameSocketHandler.class, ConcurrentHashMap.class, Client.class})
public class GameSocketHandlerTest {
    private final GameProxy proxy;
    private final ConcurrentHashMap<String, Client> lobby;

    @SuppressWarnings("unchecked")
    public GameSocketHandlerTest() throws Exception {
        this.proxy = mock(GameProxy.class);
        this.lobby = mock(ConcurrentHashMap.class);
        whenNew(ConcurrentHashMap.class).withNoArguments().thenReturn(this.lobby);
    }

    @Before
    public void setUp() {
        reset(proxy, lobby);
    }

    @Test
    public void testConnect() {
        String clientId = "clientId";
        try {
            var session = mock(WebSocketSession.class);
            doReturn(clientId).when(session).getId();
            var client = mock(Client.class);
            whenNew(Client.class).withArguments(session).thenReturn(client);
            doReturn(clientId).when(client).getId();
            var handler = new GameSocketHandler(proxy);
            handler.afterConnectionEstablished(session);

            verify(lobby, times(1)).put(clientId, client);
            verify(client, times(1)).send(anyString());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testDisconnect() {
        String clientId = "clientId";
        try {
            var client = mock(Client.class);
            doReturn(clientId).when(client).getId();
            var session = mock(WebSocketSession.class);
            doReturn(clientId).when(session).getId();
//            doReturn(client).when(lobby).remove(clientId);
            var handler = new GameSocketHandler(proxy);

            handler.afterConnectionClosed(session, CloseStatus.NORMAL);

            verify(lobby, times(1)).remove(clientId);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    // TODO: message handler
}
