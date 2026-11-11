package co.com.pragma.accounts.infrastructure.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

class AccountEventsWebSocketHandlerTest {

    private WebSocketSession openSession(String id) {
        WebSocketSession s = mock(WebSocketSession.class);
        when(s.getId()).thenReturn(id);
        when(s.isOpen()).thenReturn(true);
        return s;
    }

    @Test
    void registersSessionAndSendsWelcomeOnConnect() throws Exception {
        AccountEventsWebSocketHandler handler = new AccountEventsWebSocketHandler();
        WebSocketSession s = openSession("s1");

        handler.afterConnectionEstablished(s);

        assertThat(handler.activeSessions()).isEqualTo(1);
        verify(s).sendMessage(any(TextMessage.class)); // mensaje CONNECTED
    }

    @Test
    void removesSessionOnClose() throws Exception {
        AccountEventsWebSocketHandler handler = new AccountEventsWebSocketHandler();
        WebSocketSession s = openSession("s1");
        handler.afterConnectionEstablished(s);

        handler.afterConnectionClosed(s, CloseStatus.NORMAL);

        assertThat(handler.activeSessions()).isZero();
    }

    @Test
    void broadcastSendsToAllOpenSessions() throws Exception {
        AccountEventsWebSocketHandler handler = new AccountEventsWebSocketHandler();
        WebSocketSession s1 = openSession("s1");
        WebSocketSession s2 = openSession("s2");
        handler.afterConnectionEstablished(s1);
        handler.afterConnectionEstablished(s2);

        handler.broadcast("{\"type\":\"ACCOUNT_CREATED\",\"accountId\":\"acc-1\"}");

        // 1 welcome + 1 broadcast por sesion = 2 envios cada una
        verify(s1, times(2)).sendMessage(any(TextMessage.class));
        verify(s2, times(2)).sendMessage(any(TextMessage.class));
    }

    @Test
    void doesNotSendToClosedSession() throws Exception {
        AccountEventsWebSocketHandler handler = new AccountEventsWebSocketHandler();
        WebSocketSession s = openSession("s1");
        handler.afterConnectionEstablished(s);
        when(s.isOpen()).thenReturn(false); // se cierra antes del broadcast

        handler.broadcast("{\"type\":\"X\"}");

        // solo el welcome inicial (cuando estaba abierta), no el broadcast
        verify(s, times(1)).sendMessage(any(TextMessage.class));
    }
}
