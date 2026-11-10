package co.com.pragma.accounts.infrastructure.entrypoint;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Entry-point WebSocket imperativo (Spring MVC) para el canal de eventos de
 * cuenta en tiempo real sobre /ws/accounts/events.
 *
 * El contrato del canal (mensajes, esquema) esta documentado con AsyncAPI en
 * infrastructure/entry-points/rest/src/main/resources/asyncapi.yaml.
 *
 * Mantiene el registro de sesiones abiertas para poder hacer push (broadcast)
 * de eventos de dominio. Como demostracion, al conectarse envia un mensaje de
 * bienvenida; en un caso real, un listener de eventos invocaria broadcast(...).
 */
public class AccountEventsWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AccountEventsWebSocketHandler.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        send(session, "{\"type\":\"CONNECTED\"}");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    /** Push de un evento (JSON) a todas las sesiones abiertas (broadcast). */
    public void broadcast(String jsonEvent) {
        sessions.values().forEach(s -> send(s, jsonEvent));
    }

    /** Numero de sesiones activas (visible para test). */
    int activeSessions() {
        return sessions.size();
    }

    private void send(WebSocketSession session, String payload) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (IOException e) {
            log.warn("No se pudo enviar mensaje WebSocket a la sesion {}", session.getId(), e);
            throw new UncheckedIOException(e);
        }
    }
}
