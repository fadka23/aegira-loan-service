package com.aegira.loan.loanapplication.provider;

import com.aegira.loan.common.config.DataSourceMode;
import com.aegira.loan.common.config.LoanDataSourceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanDataProviderResolver {
    private final LoanDataSourceProperties properties;
    private final DatabaseLoanDataProvider databaseLoanDataProvider;
    private final MockLoanDataProvider mockLoanDataProvider;

    public LoanDataProvider resolve() {
        DataSourceMode mode = properties.getMode() == null ? DataSourceMode.DATABASE : properties.getMode();
        log.info("Loan data source mode: {}", mode);
        if (mode == DataSourceMode.MOCK) {
            return mockLoanDataProvider;
        }
        return databaseLoanDataProvider;
    }
}
