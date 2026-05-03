package com.aegira.loan.common.idempotency;

import com.aegira.loan.common.config.IdempotencyProperties;
import com.aegira.loan.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotencyServiceTest {
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private IdempotencyService service;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        IdempotencyProperties properties = new IdempotencyProperties();
        properties.setTtlSeconds(60);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new IdempotencyService(redisTemplate, properties);
    }

    @Test
    void storesKey() {
        when(valueOperations.setIfAbsent(eq("idempotency:user-1:POST:/submit:key-1"), eq("LOCKED"), eq(Duration.ofSeconds(60))))
                .thenReturn(true);

        service.checkAndStore("user-1", "POST:/submit", "key-1");

        verify(valueOperations).setIfAbsent("idempotency:user-1:POST:/submit:key-1", "LOCKED", Duration.ofSeconds(60));
    }

    @Test
    void detectsDuplicateKey() {
        when(valueOperations.setIfAbsent(eq("idempotency:user-1:POST:/submit:key-1"), eq("LOCKED"), eq(Duration.ofSeconds(60))))
                .thenReturn(false);

        assertThrows(ConflictException.class, () -> service.checkAndStore("user-1", "POST:/submit", "key-1"));
    }

    @Test
    void buildsExpectedRedisKey() {
        assertEquals("idempotency:user-1:POST:/submit:key-1",
                service.buildRedisKey("user-1", "POST:/submit", "key-1"));
    }

    @Test
    void disabledIdempotencySkipsRedisAndDoesNotRequireHeader() {
        IdempotencyProperties properties = new IdempotencyProperties();
        properties.setEnabled(false);
        IdempotencyService disabledService = new IdempotencyService(redisTemplate, properties);

        disabledService.checkAndStore("user-1", "POST:/submit", null);

        verify(redisTemplate, never()).opsForValue();
    }
}
