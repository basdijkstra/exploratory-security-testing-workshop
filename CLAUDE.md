# CLAUDE.md — Project Context

## Purpose

This is a deliberately vulnerable Spring Boot banking REST API built for an exploratory API security testing workshop. It is intentionally insecure in specific, documented ways. Do not fix the vulnerabilities — they are the point of the project.

## Technology Stack

- Java 21 (runs on Java 25 in this environment)
- Spring Boot 3.4.6
- Spring Security + OAuth2 Resource Server (JWT via Nimbus)
- Spring Data JPA + H2 in-memory database
- Lombok
- Maven

## Package

`com.ontestautomation.apisecurity`

## Project Structure

```
src/main/java/com/ontestautomation/apisecurity/
├── ApiSecurityApplication.java
├── config/
│   ├── DataSeeder.java          — seeds DB on startup via ApplicationRunner
│   ├── JwtProperties.java       — @ConfigurationProperties("bank.jwt")
│   ├── PasswordConfig.java      — BCryptPasswordEncoder bean (isolated to avoid circular deps)
│   └── SecurityConfig.java      — filter chain, broken JwtDecoder, JwtEncoder, AuthenticationManager
├── controller/
│   ├── AccountController.java
│   ├── AdminController.java
│   ├── AuthController.java      — POST /auth/token
│   ├── GlobalExceptionHandler.java
│   ├── LoanController.java
│   └── TransactionController.java
├── dto/
│   ├── AccountResponse.java
│   ├── CreateAccountRequest.java
│   ├── LoanApplicationRequest.java
│   ├── LoanResponse.java
│   ├── TokenRequest.java
│   ├── TokenResponse.java
│   ├── TransactionReport.java
│   ├── TransactionResponse.java
│   ├── TransferRequest.java
│   └── UserResponse.java
├── model/
│   ├── Account.java
│   ├── AccountType.java         — enum: CHECKING, SAVINGS
│   ├── Loan.java
│   ├── LoanStatus.java          — enum: PENDING, APPROVED, REJECTED
│   ├── Role.java                — enum: CUSTOMER, ADMIN
│   ├── Transaction.java
│   └── User.java
├── repository/
│   ├── AccountRepository.java
│   ├── LoanRepository.java
│   ├── TransactionRepository.java
│   └── UserRepository.java
└── service/
    ├── AccountService.java
    ├── LoanService.java
    ├── TransactionService.java
    ├── UserDetailsServiceImpl.java
    └── UserService.java
```

## Seed Data

`DataSeeder` (ApplicationRunner) creates on every startup:

| Username | Password | Role     | Account IDs |
|----------|----------|----------|-------------|
| alice    | alice123 | CUSTOMER | 1, 2        |
| bob      | bob123   | CUSTOMER | 3, 4        |
| admin    | admin123 | ADMIN    | —           |

Account IDs are sequential and predictable by design — this makes BOLA immediately discoverable.
10 seeded transactions and 2 loans (alice PENDING, bob APPROVED) are also created.

## Authentication

`POST /auth/token` with `{"username":"...","password":"..."}` returns a JWT.
All other endpoints require `Authorization: Bearer <token>`.

JWT claims: `sub` (username), `roles` (e.g. `["ROLE_CUSTOMER"]`), `iat`, `exp`.
The `JwtAuthenticationConverter` reads authorities from the `roles` claim with no prefix (roles already contain `ROLE_`).

## Intentional Vulnerabilities

These are features, not bugs. Do not remove or fix them.

### 1. SQL Injection
- **Location:** `TransactionService.searchTransactions(String keyword)`
- **How:** `EntityManager.createNativeQuery()` with direct string concatenation into the SQL.
- **Endpoint:** `GET /transactions?search={keyword}`
- **Payload:** `%' OR '1'='1`

### 2. Broken Object Level Authorization (BOLA)
- **Location:** Every list and get-by-ID method in `AccountService`, `TransactionService`, `LoanService`.
- **How:** Resources are fetched by ID only — no check that the resource belongs to the authenticated user.
- **`GET /accounts`** returns all accounts in the database, not just the caller's.

### 3. Broken Authentication
- **Location:** `SecurityConfig.jwtDecoder()`
- **How (expiry):** `JwtTimestampValidator` is intentionally omitted from the validator chain — only the issuer is validated. Expired tokens remain accepted indefinitely.
- **How (weak secret):** The HMAC signing key is `workshop-secret-key-1234` (from `application.yml`), padded to 32 bytes with null bytes. It is brute-forceable with `jwt_tool` or `hashcat`. With the known secret, participants can forge tokens with arbitrary `sub` and `roles` claims.

### 4. Unrestricted Resource Consumption
- **Location:** `GET /transactions/report`, `POST /transactions/bulk-transfer`, `POST /auth/token`
- **How:** No rate limiting anywhere. `/report` returns the full denormalised history with no pagination. Bulk transfer accepts an array of unlimited size.

### 5. Unrestricted Access to Sensitive Business Flows
- **Location:** `TransactionService.transfer()` and `LoanController.approveLoan()`
- **How (transfer):** No maximum amount, no daily limit, no balance check (accounts can go negative), no ownership check on the source account.
- **How (loan self-approval):** `POST /loans/{id}/approve` checks for `ROLE_ADMIN` in the token but does not check that the approver is not the same person as the applicant. Requires chaining with the weak-secret vulnerability to obtain a forged admin token.

## Key Design Decisions

- **No Spring Authorization Server:** Removed in favour of a simple `POST /auth/token` endpoint. The full AS added complexity without adding workshop value.
- **Self-contained JWT (not opaque):** Participants can decode the token at jwt.io and inspect `exp` and `alg` claims — essential for the broken auth discovery.
- **HMAC (not RSA):** A symmetric key can be brute-forced; an RSA private key cannot. The weak HMAC secret is the intended attack vector.
- **`PasswordConfig` isolated:** `BCryptPasswordEncoder` is in its own `@Configuration` class to avoid a circular dependency between `SecurityConfig` and `DataSeeder`.
- **No `@PreAuthorize`:** Role checks are done manually in controllers with `auth.getAuthorities().contains(...)`. This makes the code readable without annotation magic.
- **Sequential account IDs:** IDs 1–4 assigned in seed order (1–2 alice, 3–4 bob) so participants can discover BOLA by incrementing the ID by one.

## Build Notes

- Lombok requires an explicit `annotationProcessorPaths` entry in `maven-compiler-plugin` when running on Java 25 (default annotation processor discovery does not work).
- `H2Dialect` should not be specified explicitly in `application.yml` — Hibernate auto-detects it and logs a deprecation warning if you do.
- `JwtEncoder` must use `OctetSequenceKey.Builder(key).algorithm(HS256)` + `ImmutableJWKSet` — using `ImmutableSecret` alone leaves the algorithm unset and causes a runtime 500 when encoding tokens.

## Running

```bash
mvn spring-boot:run
```

H2 console: http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:mem:bankdb` / user `sa` / no password

## Workshop Documentation

- `WORKSHOP.md` — participant-facing: setup, credentials, endpoint reference, vulnerability categories (no locations).
- `HINTS.md` — facilitator-facing: progressive hints per vulnerability, full spoiler table for debrief.
