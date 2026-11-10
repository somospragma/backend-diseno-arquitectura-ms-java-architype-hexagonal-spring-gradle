package co.com.pragma.accounts.ports;

import java.time.Duration;
import java.util.Optional;

/**
 * Puerto de cache remoto (Redis). El TTL es obligatorio al escribir (nunca
 * cache eterno). Version imperativa: tipos planos, sin Mono/Flux.
 */
public interface ICacheGateway {

    Optional<String> get(String key);

    boolean put(String key, String value, Duration ttl);

    boolean evict(String key);
}
