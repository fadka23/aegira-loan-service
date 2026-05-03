package com.aegira.loan.common.idempotency;

import com.aegira.loan.common.config.IdempotencyProperties;
import com.aegira.loan.common.exception.BadRequestException;
import com.aegira.loan.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final IdempotencyProperties properties;

    public void checkAndStore(String userId, String endpoint, String idempotencyKey) {
        if (!properties.isEnabled()) {
            return;
        }
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new BadRequestException("Idempotency-Key header is required");
        }
        String redisKey = buildRedisKey(userId, endpoint, idempotencyKey);
        Boolean stored = redisTemplate.opsForValue().setIfAbsent(
                redisKey,
                "LOCKED",
                Duration.ofSeconds(properties.getTtlSeconds())
        );
        if (!Boolean.TRUE.equals(stored)) {
            throw new ConflictException("Duplicate request detected");
        }
    }

    public String buildRedisKey(String userId, String endpoint, String idempotencyKey) {
        return "idempotency:" + userId + ":" + endpoint + ":" + idempotencyKey;
    }
}
