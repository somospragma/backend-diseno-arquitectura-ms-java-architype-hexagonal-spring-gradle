package co.com.pragma.accounts.infrastructure.caffeine;

import co.com.pragma.accounts.ports.ILocalCacheGateway;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Cache LOCAL en proceso con Caffeine (PERF-BP-005), near-cache de baja latencia.
 * Version imperativa (bloqueante). Reglas:
 *  - TTL obligatorio y positivo al escribir (nunca cache eterno).
 *  - Tamano acotado por maximumSize (Caffeine evicta por tamano) y expireAfterWrite
 *    global como tope de seguridad.
 *  - TTL POR ENTRADA: como el expireAfterWrite de Caffeine es global, se guarda el
 *    instante de expiracion junto al valor y se valida en la lectura, de modo que
 *    cada put respeta su propio TTL.
 */
public class CaffeineLocalCacheAdapter implements ILocalCacheGateway {

    /** Valor + expiracion para soportar TTL por entrada. */
    private record Entry(String value, Instant expiresAt) {
        boolean expired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Cache<String, Entry> cache;

    public CaffeineLocalCacheAdapter(long maximumSize, long maxExpireAfterSeconds) {
        this.cache = Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterWrite(Duration.ofSeconds(maxExpireAfterSeconds))
            .build();
    }

    @Override
    public Optional<String> get(String key) {
        Entry entry = cache.getIfPresent(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expired()) {
            cache.invalidate(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL is mandatory and must be positive");
        }
        cache.put(key, new Entry(value, Instant.now().plus(ttl)));
    }

    @Override
    public void evict(String key) {
        cache.invalidate(key);
    }
}
