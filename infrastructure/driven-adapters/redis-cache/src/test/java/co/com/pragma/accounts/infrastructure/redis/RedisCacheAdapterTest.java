package co.com.pragma.accounts.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisCacheAdapterTest {

    @Test
    void putMustRejectMissingTtl() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        RedisCacheAdapter adapter = new RedisCacheAdapter(template);

        assertThatThrownBy(() -> adapter.put("k", "v", Duration.ZERO))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void putMustStoreWithTtl() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);

        RedisCacheAdapter adapter = new RedisCacheAdapter(template);

        assertThat(adapter.put("k", "v", Duration.ofMinutes(5))).isTrue();
    }

    @Test
    void getMustReturnValueOnHit() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        when(ops.get("k")).thenReturn("v");

        assertThat(new RedisCacheAdapter(template).get("k")).contains("v");
    }

    @Test
    void evictMustReturnTrueWhenDeleted() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        when(template.delete("k")).thenReturn(true);

        assertThat(new RedisCacheAdapter(template).evict("k")).isTrue();
    }

    @Test
    void evictMustDegradeOnError() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        when(template.delete("k")).thenThrow(new RuntimeException("down"));

        assertThat(new RedisCacheAdapter(template).evict("k")).isFalse();
    }

    @Test
    void putMustDegradeOnError() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        org.mockito.Mockito.doThrow(new RuntimeException("down"))
            .when(ops).set(any(), any(), any(Duration.class));

        assertThat(new RedisCacheAdapter(template).put("k", "v", Duration.ofSeconds(1))).isFalse();
    }

    @Test
    void getMustDegradeToMissOnRedisError() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        when(ops.get("k")).thenThrow(new RuntimeException("redis down"));

        assertThat(new RedisCacheAdapter(template).get("k")).isEmpty();
    }
}
