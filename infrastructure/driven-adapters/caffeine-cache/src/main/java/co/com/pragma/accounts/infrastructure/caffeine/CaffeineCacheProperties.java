package co.com.pragma.accounts.infrastructure.caffeine;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config del cache local Caffeine. Se activa con cache.local.enabled=true.
 * maximumSize acota la memoria; expireAfterSeconds es el TTL maximo por defecto.
 */
@ConfigurationProperties(prefix = "cache.local")
public record CaffeineCacheProperties(Long maximumSize, Long expireAfterSeconds) {

    public long maximumSizeOrDefault() {
        return maximumSize == null ? 10_000L : maximumSize;
    }

    public long expireAfterSecondsOrDefault() {
        return expireAfterSeconds == null ? 300L : expireAfterSeconds;
    }
}
