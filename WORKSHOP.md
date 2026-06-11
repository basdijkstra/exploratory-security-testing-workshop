# API Security Testing Workshop

A deliberately vulnerable banking REST API for hands-on exploration of common API security issues.

## Setup

**Prerequisites:** Java 21+, Maven 3.8+

```bash
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.  
On startup the seed credentials are printed to the console.

---

## Seed Credentials

| Username | Password | Role     |
|----------|----------|----------|
| alice    | alice123 | CUSTOMER |
| bob      | bob123   | CUSTOMER |
| admin    | admin123 | ADMIN    |

---

## Getting a Token

All endpoints except `/auth/token` require a `Bearer` token.

```
POST /auth/token
Content-Type: application/json

{
  "username": "alice",
  "password": "alice123"
}
```

Response:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 300
}
```

Use the token in subsequent requests:

```
Authorization: Bearer <jwt>
```

---

## Endpoint Reference

### Auth

| Method | Path          | Description              | Auth required |
|--------|---------------|--------------------------|---------------|
| POST   | /auth/token   | Obtain a Bearer token    | No            |

### Accounts

| Method | Path             | Description                        | Body                                          |
|--------|------------------|------------------------------------|-----------------------------------------------|
| GET    | /accounts/{id}   | Get account by ID                  | —                                             |
| POST   | /accounts        | Create a new account               | `{"type":"CHECKING"}`                         |
| DELETE | /accounts/{id}   | Delete an account                  | —                                             |

Account types: `CHECKING`, `SAVINGS`

### Transactions

| Method | Path                        | Description                             | Body / Params                                                                                  |
|--------|-----------------------------|-----------------------------------------|------------------------------------------------------------------------------------------------|
| GET    | /transactions               | List all transactions                   | —                                                                                              |
| GET    | /transactions?search={term} | Search transactions by description      | Query param                                                                                    |
| GET    | /transactions/{id}          | Get transaction by ID                   | —                                                                                              |
| POST   | /transactions/transfer      | Transfer money between two accounts     | `{"fromAccountId":"account-714","toAccountId":"account-581","amount":100.00,"description":"Payment"}` |
| POST   | /transactions/bulk-transfer | Submit multiple transfers in one request | `[{"fromAccountId":"account-714","toAccountId":"account-581","amount":10.00,"description":"t1"}, ...]` |
| GET    | /transactions/report        | Full denormalised transaction report    | —                                                                                              |

### Loans

| Method | Path                  | Description                  | Body                      |
|--------|-----------------------|------------------------------|---------------------------|
| GET    | /loans                | List all loans               | —                         |
| GET    | /loans/{id}           | Get loan by ID               | —                         |
| POST   | /loans/apply          | Apply for a loan             | `{"amount":5000.00}`      |
| POST   | /loans/{id}/approve   | Approve a loan (admin only)  | —                         |
| POST   | /loans/{id}/reject    | Reject a loan (admin only)   | —                         |

### Admin

| Method | Path          | Description              |
|--------|---------------|--------------------------|
| GET    | /admin/users  | List all users           |

### Extras

| URL                                      | Description                          |
|------------------------------------------|--------------------------------------|
| http://localhost:8080/h2-console         | H2 in-memory database console        |
| JDBC URL: `jdbc:h2:mem:bankdb` / user `sa` / no password | —                    |

---

## Scope

There are **five** security issues to find in this API, covering the following OWASP API Security Top 10 categories:

1. Broken Object Level Authorization
2. Broken Authentication
3. Unrestricted Resource Consumption
4. Unrestricted Access to Sensitive Business Flows
5. Injection (SQL)

Your facilitator has a hints sheet if you get stuck.
