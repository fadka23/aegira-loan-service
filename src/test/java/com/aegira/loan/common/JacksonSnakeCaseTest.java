package com.aegira.loan.common;

import com.aegira.loan.loanapplication.dto.LoanApplicationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonSnakeCaseTest {
    @Test
    void jsonSerializationReturnsSnakeCase() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        LoanApplicationResponse response = LoanApplicationResponse.builder()
                .id(UUID.randomUUID())
                .applicationNumber("APP-1")
                .customerId(UUID.randomUUID())
                .requestedAmount(new BigDecimal("10000000.00"))
                .build();

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("application_number"));
        assertTrue(json.contains("customer_id"));
        assertTrue(json.contains("requested_amount"));
        assertFalse(json.contains("applicationNumber"));
    }
}
