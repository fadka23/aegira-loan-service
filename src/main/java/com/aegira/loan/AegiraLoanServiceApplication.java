package com.aegira.loan;

import com.aegira.loan.common.config.IdempotencyProperties;
import com.aegira.loan.common.config.LoanDataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({LoanDataSourceProperties.class, IdempotencyProperties.class})
public class AegiraLoanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AegiraLoanServiceApplication.class, args);
    }
}
