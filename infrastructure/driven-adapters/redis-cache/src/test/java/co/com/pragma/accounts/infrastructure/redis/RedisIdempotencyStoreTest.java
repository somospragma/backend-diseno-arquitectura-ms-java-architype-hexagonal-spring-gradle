package co.com.pragma.accounts.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisIdempotencyStoreTest {

    @Test
    @SuppressWarnings("unchecked")
    void markIfFirstDelegatesToSetIfAbsentWithTtl() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(eq("k"), any(), eq(Duration.ofHours(1)))).thenReturn(true);

        RedisIdempotencyStore store = new RedisIdempotencyStore(template);

        assertThat(store.markIfFirst("k", Duration.ofHours(1))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void markIfFirstReturnsFalseOnDuplicate() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

        RedisIdempotencyStore store = new RedisIdempotencyStore(template);

        assertThat(store.markIfFirst("dup", Duration.ofHours(1))).isFalse();
    }
}
