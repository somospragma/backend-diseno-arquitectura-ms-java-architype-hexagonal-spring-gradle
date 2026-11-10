package co.com.pragma.accounts.infrastructure.redis;

import co.com.pragma.accounts.ports.ICacheGateway;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Cache remoto imperativo con Redis (Lettuce sincrono). Reglas:
 *  - TTL SIEMPRE al escribir (nunca cache eterno).
 *  - DEGRADACION: si Redis falla, el cache no rompe el flujo de negocio
 *    (get -&gt; empty en error; put -&gt; false en error). El caller sigue a la fuente.
 */
@Component
@ConditionalOnProperty(prefix = "redis", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisCacheAdapter implements ICacheGateway {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheAdapter.class);

    private final StringRedisTemplate redisTemplate;

    @Override
    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (RuntimeException e) {
            log.warn("Cache get fallo para {}; se degrada a miss", key, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean put(String key, String value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL is mandatory and must be positive");
        }
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            return true;
        } catch (RuntimeException e) {
            log.warn("Cache put fallo para {}; se ignora (degradacion)", key, e);
            return false;
        }
    }

    @Override
    public boolean evict(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
