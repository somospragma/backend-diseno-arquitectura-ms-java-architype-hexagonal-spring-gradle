package co.com.pragma.accounts.ports;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache LOCAL en proceso (Caffeine), complementaria al cache remoto (Redis).
 * Sirve como primer nivel de baja latencia (near-cache). El TTL es obligatorio
 * al escribir (nunca cache eterno) y el tamano esta acotado (PERF-BP-005).
 * Version imperativa: tipos planos, sin Mono/Flux.
 */
public interface ILocalCacheGateway {

    Optional<String> get(String key);

    void put(String key, String value, Duration ttl);

    void evict(String key);
}
