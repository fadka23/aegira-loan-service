CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nik VARCHAR(16) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    date_of_birth DATE NOT NULL,
    address VARCHAR(500) NOT NULL,
    marital_status VARCHAR(50),
    job_type VARCHAR(100),
    monthly_income NUMERIC(19,2) NOT NULL,
    monthly_expense NUMERIC(19,2) NOT NULL,
    existing_installment NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE loan_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    min_amount NUMERIC(19,2) NOT NULL,
    max_amount NUMERIC(19,2) NOT NULL,
    min_tenure INTEGER NOT NULL,
    max_tenure INTEGER NOT NULL,
    annual_interest_rate NUMERIC(9,4) NOT NULL,
    minimum_income NUMERIC(19,2) NOT NULL,
    maximum_dsr NUMERIC(9,4) NOT NULL,
    need_collateral BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_number VARCHAR(100) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id),
    agent_id UUID NOT NULL REFERENCES users(id),
    loan_product_id UUID NOT NULL REFERENCES loan_products(id),
    requested_amount NUMERIC(19,2) NOT NULL,
    requested_tenure INTEGER NOT NULL,
    loan_purpose VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    risk_level VARCHAR(20),
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT
);

CREATE TABLE loan_calculations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_application_id UUID NOT NULL REFERENCES loan_applications(id),
    loan_amount NUMERIC(19,2) NOT NULL,
    annual_interest_rate NUMERIC(9,4) NOT NULL,
    tenure_month INTEGER NOT NULL,
    total_interest NUMERIC(19,2) NOT NULL,
    total_payment NUMERIC(19,2) NOT NULL,
    monthly_installment NUMERIC(19,2) NOT NULL,
    existing_installment NUMERIC(19,2) NOT NULL,
    monthly_income NUMERIC(19,2) NOT NULL,
    current_dsr NUMERIC(9,4) NOT NULL,
    projected_dsr NUMERIC(9,4) NOT NULL,
    maximum_dsr NUMERIC(9,4) NOT NULL,
    eligible BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE eligibility_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_application_id UUID NOT NULL REFERENCES loan_applications(id),
    rule_name VARCHAR(80) NOT NULL,
    passed BOOLEAN NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE approval_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loan_application_id UUID NOT NULL REFERENCES loan_applications(id),
    performed_by UUID NOT NULL REFERENCES users(id),
    decision VARCHAR(50) NOT NULL,
    approved_amount NUMERIC(19,2),
    notes VARCHAR(1000) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    performed_by UUID REFERENCES users(id),
    old_value TEXT,
    new_value TEXT,
    notes TEXT,
    correlation_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_loan_applications_customer_id ON loan_applications(customer_id);
CREATE INDEX idx_loan_applications_agent_id ON loan_applications(agent_id);
CREATE INDEX idx_loan_calculations_application_id ON loan_calculations(loan_application_id);
CREATE INDEX idx_eligibility_results_application_id ON eligibility_results(loan_application_id);
CREATE INDEX idx_approval_histories_application_id ON approval_histories(loan_application_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
