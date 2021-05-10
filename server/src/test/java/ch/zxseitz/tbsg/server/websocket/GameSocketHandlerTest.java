package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.server.games.GameProxy;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GameSocketHandler.class, ConcurrentHashMap.class, Client.class,
        MessageManager.class, TextMessage.class})
public class GameSocketHandlerTest {
    private final GameProxy proxy;
    private final ConcurrentHashMap<String, Client> lobby;

    @SuppressWarnings("unchecked")
    public GameSocketHandlerTest() throws Exception {
        this.proxy = mock(GameProxy.class);
        this.lobby = mock(ConcurrentHashMap.class);
        whenNew(ConcurrentHashMap.class).withNoArguments().thenReturn(this.lobby);
        mockStatic(MessageManager.class);
    }

    @Before
    public void setUp() {
        reset(proxy, lobby);
    }

    @Test
    public void testConnect() {
        String clientId = "clientId";
        String clientMessage = "client message";
        try {
            var session = mock(WebSocketSession.class);
            var client = mock(Client.class);
            doReturn(clientId).when(session).getId();
            doReturn(clientId).when(client).getId();
            whenNew(Client.class).withArguments(session).thenReturn(client);
            when(MessageManager.createIdMessage(client)).thenReturn(clientMessage);

            var handler = new GameSocketHandler(proxy);
            handler.afterConnectionEstablished(session);

            verify(lobby, times(1)).put(clientId, client);
            verify(client, times(1)).send(clientMessage);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
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
            fail();
        }
    }

    // TODO: message handler

    @Test
    public void testMessageChallenge() {
        int eventCode = MessageManager.CLIENT_CHALLENGE;
        String clientId = "clientId";
        String opponentId = "opponentId";
        String payload = "payload";
        String opponentChallengeMessage = "message";
        try {
            var message = PowerMockito.mock(TextMessage.class);  // final class
            var args = mock(JsonNode.class);
            var client = mock(Client.class);
            var opponent = mock(Client.class);
            var session = mock(WebSocketSession.class);
            var challenges = new HashSet<Client>(1);
            doReturn(clientId).when(session).getId();
            doReturn(client).when(lobby).get(clientId);
            doReturn(opponent).when(lobby).get(opponentId);
            doReturn(challenges).when(client).getChallenges();
            doReturn(payload).when(message).getPayload();
            when(MessageManager.parseClientMessage(payload)).thenReturn(Map.entry(eventCode, args));
            when(MessageManager.readClientArgument(args, "opponent", String.class)).thenReturn(opponentId);
            when(MessageManager.createChallengeMessage(client)).thenReturn(opponentChallengeMessage);

            var handler = new GameSocketHandler(proxy);
            handler.handleTextMessage(session, message);

            assertTrue(challenges.contains(opponent));
            verify(opponent).send(opponentChallengeMessage);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testMessageChallengeNoOpponent() {
        int eventCode = MessageManager.CLIENT_CHALLENGE;
        String clientId = "clientId";
        String opponentId = "opponentId";
        String payload = "payload";
        String errorMessage = "error";
        try {
            var message = PowerMockito.mock(TextMessage.class);  // final class
            var args = mock(JsonNode.class);
            var client = mock(Client.class);
            var session = mock(WebSocketSession.class);
            doReturn(clientId).when(session).getId();
            doReturn(client).when(lobby).get(clientId);
            doReturn(null).when(lobby).get(opponentId);
            doReturn(payload).when(message).getPayload();
            when(MessageManager.parseClientMessage(payload)).thenReturn(Map.entry(eventCode, args));
            when(MessageManager.readClientArgument(args, "opponent", String.class)).thenReturn(opponentId);
            when(MessageManager.createErrorMessage(anyString())).thenReturn(errorMessage);

            var handler = new GameSocketHandler(proxy);
            handler.handleTextMessage(session, message);

            verify(client).send(errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
