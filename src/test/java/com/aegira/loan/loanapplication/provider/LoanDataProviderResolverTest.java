package com.aegira.loan.loanapplication.provider;

import com.aegira.loan.common.config.DataSourceMode;
import com.aegira.loan.common.config.LoanDataSourceProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class LoanDataProviderResolverTest {
    @Test
    void returnsMockLoanDataProviderWhenModeIsMock() {
        LoanDataSourceProperties properties = new LoanDataSourceProperties();
        properties.setMode(DataSourceMode.MOCK);
        DatabaseLoanDataProvider databaseProvider = mock(DatabaseLoanDataProvider.class);
        MockLoanDataProvider mockProvider = new MockLoanDataProvider();

        LoanDataProviderResolver resolver = new LoanDataProviderResolver(properties, databaseProvider, mockProvider);

        assertSame(mockProvider, resolver.resolve());
    }

    @Test
    void returnsDatabaseLoanDataProviderWhenModeIsDatabase() {
        LoanDataSourceProperties properties = new LoanDataSourceProperties();
        properties.setMode(DataSourceMode.DATABASE);
        DatabaseLoanDataProvider databaseProvider = mock(DatabaseLoanDataProvider.class);
        MockLoanDataProvider mockProvider = new MockLoanDataProvider();

        LoanDataProviderResolver resolver = new LoanDataProviderResolver(properties, databaseProvider, mockProvider);

        assertSame(databaseProvider, resolver.resolve());
    }
}
