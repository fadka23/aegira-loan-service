package com.aegira.loan.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI aegiraOpenApi() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080").description("Local development"))
                .info(new Info()
                        .title("Aegira Loan Service API")
                        .version("v1")
                        .description("Mini Loan Origination System flow: 1. Agent creates customer. "
                                + "2. Agent creates loan application. 3. Agent submits loan application. "
                                + "4. Backend calculates installment and DSR. 5. Backend checks eligibility. "
                                + "6. Risk Officer reviews application. 7. HO approves if required. "
                                + "DSR calculation uses current DSR = existing monthly installment / monthly income * 100 "
                                + "and projected DSR = (existing monthly installment + new monthly installment) / monthly income * 100. "
                                + "Loan submission can use DATABASE or MOCK data through feature flag loan.data-source.mode. "
                                + "Submit and approval endpoints require Idempotency-Key and use Redis-based idempotency. "
                                + "CustomerID is used as correlationId for debugging and audit traceability."));
    }

    @Bean
    public OpenApiCustomiser snakeCaseSchemaProperties() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
                applySnakeCase(schema);
            }
        };
    }

    private void applySnakeCase(Schema<?> schema) {
        if (schema == null) {
            return;
        }
        Map<String, Schema> properties = schema.getProperties();
        if (properties != null && !properties.isEmpty()) {
            Map<String, Schema> renamed = new LinkedHashMap<String, Schema>();
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                renamed.put(toSnakeCase(entry.getKey()), entry.getValue());
                applySnakeCase(entry.getValue());
            }
            schema.setProperties(renamed);
        }
        List<String> required = schema.getRequired();
        if (required != null && !required.isEmpty()) {
            List<String> renamedRequired = new ArrayList<String>();
            for (String field : required) {
                renamedRequired.add(toSnakeCase(field));
            }
            schema.setRequired(renamedRequired);
        }
        applySnakeCase(schema.getItems());
    }

    private String toSnakeCase(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isUpperCase(current)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(current));
            } else {
                result.append(current);
            }
        }
        return result.toString().toLowerCase(Locale.ENGLISH);
    }
}
