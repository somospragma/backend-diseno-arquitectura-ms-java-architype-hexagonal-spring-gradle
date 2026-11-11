package co.com.pragma.accounts.infrastructure.caffeine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class CaffeineLocalCacheAdapterTest {

    private final CaffeineLocalCacheAdapter cache = new CaffeineLocalCacheAdapter(100, 300);

    @Test
    void getMissReturnsEmpty() {
        assertThat(cache.get("absent")).isEmpty();
    }

    @Test
    void putThenGetReturnsValue() {
        cache.put("k", "v", Duration.ofMinutes(1));
        assertThat(cache.get("k")).contains("v");
    }

    @Test
    void putWithoutTtlIsRejected() {
        assertThatThrownBy(() -> cache.put("k", "v", Duration.ZERO))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void expiredEntryReturnsEmpty() throws InterruptedException {
        cache.put("k", "v", Duration.ofMillis(10));
        Thread.sleep(30);
        assertThat(cache.get("k")).isEmpty();
    }

    @Test
    void evictRemovesEntry() {
        cache.put("k", "v", Duration.ofMinutes(1));
        cache.evict("k");
        assertThat(cache.get("k")).isEmpty();
    }
}
