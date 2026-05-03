package com.aegira.loan.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loan.data-source")
public class LoanDataSourceProperties {
    private DataSourceMode mode = DataSourceMode.DATABASE;
}
