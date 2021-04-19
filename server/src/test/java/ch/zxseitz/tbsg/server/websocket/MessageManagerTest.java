package ch.zxseitz.tbsg.server.websocket;

import ch.zxseitz.tbsg.games.exceptions.EventException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class MessageManagerTest {
    public static class TestAction {
        private final int index;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public TestAction(@JsonProperty("index") int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private static final String clientId = "client";
    private static final String opponentId = "opponent";

    private final Client client;
    private final Client opponent;

    public MessageManagerTest() {
        this.client = mock(Client.class);
        this.opponent = mock(Client.class);
    }

    @Before
    public void setUp() {
        reset(client, opponent);
    }

    @Test
    public void testParseClientMessage() {
        try {
            var json = "{\"code\": 1001, \"args\": {}}";
            var message = MessageManager.parseClientMessage(json);
            Assert.assertEquals(1001, message.getKey().intValue());
            Assert.assertEquals(0, message.getValue().size());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testParseArgs() {
        try {
            var json = "{\"code\": 1001, \"args\": {\"x\": 2, \"y\": 3}}";
            var message = MessageManager.parseClientMessage(json);
            Assert.assertEquals(1001, message.getKey().intValue());
            Assert.assertEquals(2,  message.getValue().size());
            Assert.assertEquals(2, message.getValue().get("x").asInt());
            Assert.assertEquals(3, message.getValue().get("y").asInt());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testReadClientGameArgs() {
        try {
            var mapper =  new ObjectMapper();
            var node = mapper.createObjectNode();
            node.put("index", 4);
            var action = MessageManager.readClientGameArguments(node, TestAction.class);
            Assert.assertEquals(TestAction.class, action.getClass());
            Assert.assertEquals(4, ((TestAction) action).getIndex());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testReadClientInvalidGameArgs() {
        try {
            var mapper =  new ObjectMapper();
            var node = mapper.createObjectNode();
            node.put("x", 4);
            MessageManager.readClientGameArguments(node, TestAction.class);
            Assert.fail();
        } catch (EventException ignore) {
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateChallengeMessage() {
        try {
            doReturn(clientId).when(client).getId();
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_CHALLENGE
                            + ",\"args\":{\"opponent\":\"" + clientId + "\"}}",
                    MessageManager.createChallengeMessage(client));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateChallengeAbortMessage() {
        try {
            doReturn(clientId).when(client).getId();
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_CHALLENGE_ABORT
                            + ",\"args\":{\"opponent\":\"" + clientId + "\"}}",
                    MessageManager.createChallengeAbortMessage(client));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateChallengeAcceptMessage() {
        try {
            doReturn(clientId).when(client).getId();
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_CHALLENGE_ACCEPT
                            + ",\"args\":{\"opponent\":\"" + clientId + "\"}}",
                    MessageManager.createChallengeAcceptMessage(client));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateChallengeDeclineMessage() {
        try {
            doReturn(clientId).when(client).getId();
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_CHALLENGE_DECLINE
                            + ",\"args\":{\"opponent\":\"" + clientId + "\"}}",
                    MessageManager.createChallengeDeclineMessage(client));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateGameInitNextMessage() {
        try {
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_GAME_INIT_NEXT
                            + ",\"args\":{\"color\":1,\"board\":[0,1,2,3],"
                            + "\"preview\":[{\"index\":1},{\"index\":4}]}}",
                    MessageManager.createGameInitNextMessage(1, new int[] {0, 1, 2, 3},
                            Arrays.asList(new TestAction(1), new TestAction(4))));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateGameInitMessage() {
        try {
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_GAME_INIT
                            + ",\"args\":{\"color\":1,\"board\":[0,1,2,3]}}",
                    MessageManager.createGameInitMessage(1, new int[] {0, 1, 2, 3}));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateGameUpdateNextMessage() {
        try {
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_GAME_UPDATE_NEXT
                            + ",\"args\":{\"source\":{\"index\":0},\"board\":[0,1,2,3],"
                            + "\"preview\":[{\"index\":1},{\"index\":4}]}}",
                    MessageManager.createGameUpdateNextMessage(new TestAction(0), new int[] {0, 1, 2, 3},
                            Arrays.asList(new TestAction(1), new TestAction(4))));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateGameUpdateMessage() {
        try {
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_GAME_UPDATE
                            + ",\"args\":{\"source\":{\"index\":0},\"board\":[0,1,2,3]}}",
                    MessageManager.createGameUpdateMessage(new TestAction(0), new int[] {0, 1, 2, 3}));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateGameEndVictoryMessage() {
        try {
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_GAME_END_VICTORY
                            + ",\"args\":{\"source\":{\"index\":0},\"board\":[0,1,2,3]}}",
                    MessageManager.createGameEndVictoryMessage(new TestAction(0), new int[] {0, 1, 2, 3}));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateGameEndDefeatMessage() {
        try {
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_GAME_END_DEFEAT
                            + ",\"args\":{\"source\":{\"index\":0},\"board\":[0,1,2,3]}}",
                    MessageManager.createGameEndDefeatMessage(new TestAction(0), new int[] {0, 1, 2, 3}));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCreateGameEndTieMessage() {
        try {
            Assert.assertEquals("{\"code\":" + MessageManager.SERVER_GAME_END_TIE
                            + ",\"args\":{\"source\":{\"index\":0},\"board\":[0,1,2,3]}}",
                    MessageManager.createGameEndTieMessage(new TestAction(0), new int[] {0, 1, 2, 3}));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
