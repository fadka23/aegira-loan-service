package com.aegira.loan.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loan.idempotency")
public class IdempotencyProperties {
    private boolean enabled = true;
    private long ttlSeconds = 86400;
}
