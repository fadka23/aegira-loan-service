# Aegira Loan Service

Mini Loan Origination System for agent loan submission, credit risk calculation, eligibility checking, and approval by Risk Officer or Head Office.

## Product and Technical Documentation

- PRD: https://khalidalhabibie07.atlassian.net/wiki/external/ZmY1ZGViYTQ0MGViNDUyN2IwOGZmMjhiMTc0MzFkM2E
- TRD: https://khalidalhabibie07.atlassian.net/wiki/external/OWU4NWQ0MDUyMjUwNDIxOWI1YzJmMjVkYzk0OTA5OGE

The PRD explains the product scope, user flow, business rules, and acceptance criteria.
The TRD explains the technical design, architecture, database schema, API contract, idempotency, feature flag, Redis, Docker, and implementation rules.

## Tech Stack

- Java 8
- Spring Boot 2.7.6
- Spring Framework 5.3.24
- Maven
- PostgreSQL
- HikariCP 4.0.3
- Redis 5.0.3
- Spring Data JPA
- Spring Data Redis
- Spring Security + JWT
- Flyway
- Lombok
- springdoc-openapi UI
- JUnit 5 + Mockito
- Docker and Docker Compose

## Runtime Stack

- Java 8
- Spring Framework 5.3.24
- Spring Boot 2.x compatible with Spring 5.3.24
- HikariCP 4.0.3
- Redis 5.0.3
- Docker 28.0.4
- PostgreSQL

HikariCP is pinned to `4.0.3` because it is compatible with a Java 8 runtime. HikariCP `5.0.1` is Java 11 bytecode and cannot run safely in the Java 8 Docker runtime used by this service.

## Architecture

The project uses package-by-module organization under `com.aegira.loan`.

- `auth`: login, current user, JWT generation and validation.
- `user`: internal user and role model.
- `customer`: customer CRUD and duplicate NIK checking.
- `loanproduct`: loan product CRUD for admins.
- `loanapplication`: draft, update, submit, and visibility rules.
- `calculation`: flat-interest calculation and DSR calculation using `BigDecimal`.
- `eligibility`: rule evaluation and risk level assignment.
- `approval`: Risk and HO decision workflow.
- `audit`: audit log creation and retrieval.
- `common`: API response wrapper, exceptions, security helpers, request filters.

Controllers stay thin and return DTOs. Business rules live in services. Submission and approval flows are transactional and write calculation, eligibility, approval history, and audit records as applicable.

## Run With Docker

```bash
docker compose up --build
```

## Running with Docker

```bash
docker compose up --build
```

## Running with Makefile

Run infrastructure only with Docker:

```bash
make infra-up
```

Run the Spring Boot app locally with Maven:

```bash
make run
```

Run the full Docker stack:

```bash
make docker-up
```

Useful commands:

```bash
make test
make build
make infra-logs
make docker-down
```

Run with database mode:

```bash
LOAN_DATA_SOURCE_MODE=DATABASE docker compose up --build
```

Run with mock mode:

```bash
LOAN_DATA_SOURCE_MODE=MOCK docker compose up --build
```

The API runs at `http://localhost:8080`.

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Static OpenAPI spec, usable without running the service:

```text
docs/openapi.json
```

PostgreSQL:

- Database: `aegira_loan`
- Username: `aegira`
- Password: `aegira_password`
- Port: `5432`

Redis:

- Image: `redis:5.0.3-alpine`
- Host from app container: `redis`
- Port: `6379`
- Persistence: append-only file enabled

## Run Locally

Start PostgreSQL first, then set environment variables if needed:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/aegira_loan
export SPRING_DATASOURCE_USERNAME=aegira
export SPRING_DATASOURCE_PASSWORD=aegira_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=change-this-secret
export JWT_EXPIRATION=3600000
export LOAN_DATA_SOURCE_MODE=DATABASE
export IDEMPOTENCY_ENABLED=true
export IDEMPOTENCY_TTL_SECONDS=86400
mvn spring-boot:run
```

Run tests:

```bash
mvn test
```

## Default Users

All seeded users use password `password123`.

| Email | Role |
| --- | --- |
| `admin@aegira.com` | `ADMIN` |
| `agent@aegira.com` | `AGENT` |
| `risk@aegira.com` | `RISK` |
| `ho@aegira.com` | `HO` |

## API Flow Example

1. Login as agent and capture the JWT.
2. Create a customer.
3. Create a loan application for the customer and seeded Personal Loan product.
4. Submit the application. The service stores loan calculation and eligibility results, sets risk level, and moves the status to `WAITING_RISK_REVIEW`.
5. Login as Risk Officer and approve, reject, or request revision.
6. If Risk approves more than `50000000`, status becomes `WAITING_HO_APPROVAL`; otherwise it becomes `HO_APPROVED`.
7. Login as HO and approve or reject applications waiting for HO approval.
8. Review approval histories and audit logs.

The loan application flow uses the customer ID as `correlationId` in logs and audit records. Each HTTP request also receives an `X-Request-Id`.

## Redis Usage

Redis is used for idempotency handling in submit and approval endpoints.

Protected endpoints require this header:

```text
Idempotency-Key: 4b65f9d8-78aa-4b94-b4e7-33c36deaa001
```

This prevents duplicate loan submission or duplicate approval when users double-click buttons or clients retry requests. The service stores the idempotency key in Redis using this format:

```text
idempotency:{userId}:{endpoint}:{idempotencyKey}
```

The TTL is controlled by `IDEMPOTENCY_TTL_SECONDS`, defaulting to `86400` seconds. Reusing the same key for the same user and endpoint returns `409 CONFLICT` with message `Duplicate request detected`.

Idempotency can be disabled for local development:

```bash
IDEMPOTENCY_ENABLED=false make run
```

When disabled, protected endpoints do not require the `Idempotency-Key` header and Redis idempotency checks are skipped.

## Application Flow

1. Admin configures loan product.
2. Agent creates customer data.
3. Agent creates loan application.
4. Agent submits the application.
5. Backend calculates credit risk using Debt Service Ratio.
6. Backend calculates:
   - Current DSR = existing monthly installment / monthly income * 100
   - Projected DSR = (existing monthly installment + new monthly installment) / monthly income * 100
7. Backend runs eligibility checking.
8. Application moves to Risk Review.
9. Risk Officer approves, rejects, or requests revision.
10. If approved amount is greater than 50,000,000, application goes to HO approval.
11. HO approves or rejects.
12. All important actions are stored in approval history and audit log.

## Feature Flag: Loan Data Source Mode

The system supports a feature flag to switch the data source used during loan submission and eligibility checking.

Configuration:

```yaml
loan:
  data-source:
    mode: DATABASE
```

Environment variable:

```bash
LOAN_DATA_SOURCE_MODE=DATABASE
```

Available modes:

- `DATABASE`: Uses PostgreSQL data from `customers`, `loan_products`, and `loan_applications` tables.
- `MOCK`: Uses internal mock data for testing and demo purposes.

Use `MOCK` mode when:

- running demo without preparing database data
- testing calculation and eligibility flow quickly
- developing frontend integration before real data is ready

Use `DATABASE` mode when:

- running normal application flow
- testing real customer and product data
- running production-like scenarios

## Debugging and Correlation ID

Use the `X-Correlation-Id` request header for technical request tracing. If absent, the service falls back to `X-Request-Id` and then generates an ID. Error responses include `correlation_id`.

CustomerID remains a separate business correlation value in the loan application flow and audit records.

Run the test suite with `mvn test`. Do not log raw PII, tokens, or passwords, and use [CODE_REVIEW_CHECKLIST.md](CODE_REVIEW_CHECKLIST.md) before opening a PR.

## API JSON Naming Convention

All API request and response fields use `snake_case`. Java code still uses `camelCase` internally.

Example:

```text
customerId in Java becomes customer_id in JSON.
```

Enum values stay `UPPER_SNAKE_CASE`, for example `WAITING_RISK_REVIEW`.

## Frontend Consumption APIs

The backend provides UI-friendly APIs to reduce frontend complexity:

- dashboard summary APIs: `/api/v1/dashboard/agent/summary`, `/api/v1/dashboard/risk/summary`, `/api/v1/dashboard/ho/summary`
- application list API with filters: `/api/v1/loan-applications`
- application detail aggregate API: `/api/v1/loan-applications/{id}/detail`
- options APIs for dropdowns: `/api/v1/options/loan-products`, `/api/v1/options/customers`
- approval task list API: `/api/v1/approval-tasks`

Frontend detail screens should use `/api/v1/loan-applications/{id}/detail` to avoid calling customer, product, calculation, eligibility, and approval history APIs separately.

## Environment Variables

| Variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |
| `REDIS_HOST` | Redis host |
| `REDIS_PORT` | Redis port |
| `JWT_SECRET` | JWT signing secret input |
| `JWT_EXPIRATION` | JWT expiration in milliseconds |
| `LOAN_DATA_SOURCE_MODE` | `DATABASE` or `MOCK` |
| `IDEMPOTENCY_ENABLED` | Enable or disable Redis idempotency checks |
| `IDEMPOTENCY_TTL_SECONDS` | Redis TTL for idempotency keys |
| `DB_MAX_POOL_SIZE` | Hikari maximum pool size |
| `DB_MIN_IDLE` | Hikari minimum idle connections |
| `DB_IDLE_TIMEOUT` | Hikari idle timeout in milliseconds |
| `DB_CONNECTION_TIMEOUT` | Hikari connection timeout in milliseconds |
| `DB_MAX_LIFETIME` | Hikari max lifetime in milliseconds |

## Sample Curl Commands

Login:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"agent@aegira.com","password":"password123"}'
```

Set token:

```bash
export TOKEN='paste-token-here'
```

Create customer:

```bash
curl -s -X POST http://localhost:8080/api/v1/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "nik":"3173000000000001",
    "name":"Rina Saputra",
    "phone_number":"081234567890",
    "date_of_birth":"1990-01-15",
    "address":"Jakarta",
    "marital_status":"MARRIED",
    "job_type":"EMPLOYEE",
    "monthly_income":8000000,
    "monthly_expense":2500000,
    "existing_installment":500000
  }'
```

Create application using seeded Personal Loan product:

```bash
curl -s -X POST http://localhost:8080/api/v1/loan-applications \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "customer_id":"paste-customer-id",
    "loan_product_id":"10000000-0000-0000-0000-000000000001",
    "requested_amount":25000000,
    "requested_tenure":12,
    "loan_purpose":"Home renovation"
  }'
```

Submit:

```bash
curl -s -X POST http://localhost:8080/api/v1/loan-applications/paste-application-id/submit \
  -H 'Idempotency-Key: 4b65f9d8-78aa-4b94-b4e7-33c36deaa001' \
  -H "Authorization: Bearer $TOKEN"
```

Risk approve:

```bash
export RISK_TOKEN='paste-risk-token-here'
curl -s -X POST http://localhost:8080/api/v1/loan-applications/paste-application-id/risk/approve \
  -H "Authorization: Bearer $RISK_TOKEN" \
  -H 'Idempotency-Key: 52a2f396-9272-4f1b-aa83-f38c85ac7981' \
  -H 'Content-Type: application/json' \
  -d '{"approved_amount":25000000,"notes":"Within policy"}'
```

Get agent dashboard summary:

```bash
curl -s http://localhost:8080/api/v1/dashboard/agent/summary \
  -H "Authorization: Bearer $TOKEN"
```

Get application list:

```bash
curl -s 'http://localhost:8080/api/v1/loan-applications?page=0&size=10&status=WAITING_RISK_REVIEW&risk_level=MEDIUM' \
  -H "Authorization: Bearer $TOKEN"
```

Get application detail aggregate:

```bash
curl -s http://localhost:8080/api/v1/loan-applications/paste-application-id/detail \
  -H "Authorization: Bearer $TOKEN"
```

Get loan product options:

```bash
curl -s http://localhost:8080/api/v1/options/loan-products \
  -H "Authorization: Bearer $TOKEN"
```

Get approval task list:

```bash
curl -s 'http://localhost:8080/api/v1/approval-tasks?page=0&size=10' \
  -H "Authorization: Bearer $RISK_TOKEN"
```

View calculation, eligibility, histories, and audit logs:

```bash
curl -s http://localhost:8080/api/v1/loan-applications/paste-application-id/calculation -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8080/api/v1/loan-applications/paste-application-id/eligibility -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8080/api/v1/loan-applications/paste-application-id/approval-histories -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8080/api/v1/loan-applications/paste-application-id/audit-logs -H "Authorization: Bearer $TOKEN"
```
