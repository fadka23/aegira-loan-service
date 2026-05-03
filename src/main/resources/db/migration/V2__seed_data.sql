INSERT INTO users (id, email, password_hash, role, active)
VALUES
('00000000-0000-0000-0000-000000000001', 'admin@aegira.com', '$2b$10$abcdefghijklmnopqrstuu0SYq8twpcthS10uxQ26rv0s3Obj8Ufu', 'ADMIN', TRUE),
('00000000-0000-0000-0000-000000000002', 'agent@aegira.com', '$2b$10$abcdefghijklmnopqrstuu0SYq8twpcthS10uxQ26rv0s3Obj8Ufu', 'AGENT', TRUE),
('00000000-0000-0000-0000-000000000003', 'risk@aegira.com', '$2b$10$abcdefghijklmnopqrstuu0SYq8twpcthS10uxQ26rv0s3Obj8Ufu', 'RISK', TRUE),
('00000000-0000-0000-0000-000000000004', 'ho@aegira.com', '$2b$10$abcdefghijklmnopqrstuu0SYq8twpcthS10uxQ26rv0s3Obj8Ufu', 'HO', TRUE);

INSERT INTO loan_products (
    id, name, min_amount, max_amount, min_tenure, max_tenure,
    annual_interest_rate, minimum_income, maximum_dsr, need_collateral, active
) VALUES (
    '10000000-0000-0000-0000-000000000001',
    'Personal Loan',
    5000000.00,
    100000000.00,
    6,
    36,
    0.1200,
    3000000.00,
    40.0000,
    FALSE,
    TRUE
);
