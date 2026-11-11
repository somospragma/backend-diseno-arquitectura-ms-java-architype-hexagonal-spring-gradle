package co.com.pragma.accounts.infrastructure.entrypoint.config;

import co.com.pragma.accounts.infrastructure.entrypoint.AccountEventsWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Wiring del entry-point WebSocket imperativo (Spring MVC). Registra el handler
 * del canal de eventos en /ws/accounts/events. Requiere spring-websocket
 * (via spring-boot-starter-websocket).
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public AccountEventsWebSocketHandler accountEventsWebSocketHandler() {
        return new AccountEventsWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(accountEventsWebSocketHandler(), "/ws/accounts/events")
            .setAllowedOrigins("*");
    }
}
