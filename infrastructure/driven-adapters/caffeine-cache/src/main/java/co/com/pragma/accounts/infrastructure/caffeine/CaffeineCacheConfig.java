package co.com.pragma.accounts.infrastructure.caffeine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wiring del cache local Caffeine. Solo activo con cache.local.enabled=true. */
@Configuration
@ConditionalOnProperty(prefix = "cache.local", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CaffeineCacheProperties.class)
public class CaffeineCacheConfig {

    @Bean
    public CaffeineLocalCacheAdapter caffeineLocalCacheAdapter(CaffeineCacheProperties props) {
        return new CaffeineLocalCacheAdapter(
            props.maximumSizeOrDefault(), props.expireAfterSecondsOrDefault());
    }
}
