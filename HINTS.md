# Hints — Facilitator Reference

Share these one at a time, in order, when a group is stuck on a particular vulnerability.
Each hint progressively narrows the search space without giving the full answer.

---

## 1 — Broken Object Level Authorization (BOLA)

**Hint 1:** After retrieving your own accounts, try changing the ID in the URL. What happens?

**Hint 2:** The list endpoints return more records than they should for the authenticated user. Are
all of those records yours?

**Hint 3:** Every `GET` endpoint that accepts an `{id}` path parameter is affected. The server
never checks whether the resource belongs to the caller. Try account IDs 1–4 while authenticated
as alice (who owns only 1 and 2) and as bob (who owns only 3 and 4). The same pattern applies to
`/transactions/{id}` and `/loans/{id}`.

---

## 2 — Broken Authentication

**Hint 1:** Decode the JWT you received from `/auth/token` at jwt.io. Look at the payload claims
carefully. What does `exp` mean, and what time does it show?

**Hint 2:** Wait for the token to expire (5 minutes), then use it again. Does the API reject it?

**Hint 3 (second sub-issue):** Look at the `alg` claim in the JWT header. It uses a symmetric
signing algorithm. If you knew the secret, you could sign any token you wanted. How strong is the
secret? Try a short wordlist containing obvious values like `workshop-secret-key-1234`.

**Hint 4:** Tools like `jwt_tool` (`python jwt_tool.py <token> -C -d wordlist.txt`) or `hashcat`
can brute-force HMAC-signed JWTs. Once you have the secret, forge a token with `"sub":"alice"` and
`"roles":["ROLE_ADMIN"]`, sign it with the same key, and see what you can access.

---

## 3 — Unrestricted Resource Consumption

**Hint 1:** Are there any endpoints that could return a very large response regardless of how much
data is in the system?

**Hint 2:** Look at `GET /transactions/report` and `POST /transactions/bulk-transfer`. Neither
enforces a page size or a request size limit. What would happen if the database had millions of
records, or if you sent a bulk-transfer array with thousands of entries?

**Hint 3:** There is also no rate limiting on any endpoint — including `/auth/token`. A credential-
stuffing or brute-force attack against the login endpoint will not be throttled.

---

## 4 — Unrestricted Access to Sensitive Business Flows

**Hint 1:** Try transferring an amount larger than the source account's balance. What does the API
do?

**Hint 2:** Is there any upper limit on how much money can be transferred in a single request?
What about a daily total? What about who is allowed to initiate a transfer from a given account?

**Hint 3 (loan self-approval):** Apply for a loan as alice. Now try to approve it. You will be
blocked — approval requires an admin token. But what if you had one? See the Broken Authentication
hints for how to obtain a forged admin token, then call `POST /loans/{id}/approve` with alice's
loan ID. The server checks that your token has the admin role, but it never checks whether you are
the same person as the applicant.

---

## 5 — Injection (SQL)

**Hint 1:** The `GET /transactions?search={term}` endpoint accepts a free-text search parameter.
Try a normal search first (e.g., `?search=salary`) to see how it works.

**Hint 2:** Now think about what happens when the search value is embedded into a SQL query. What
character would break out of a string literal?

**Hint 3:** Try the payload `%' OR '1'='1`. What does the response contain now? From there, a
UNION-based attack can be used to read data from any table in the H2 database — including the
`users` table.

---

## Vulnerability Locations (full spoiler — for debrief only)

| # | Category                            | Exact location                                                              |
|---|-------------------------------------|-----------------------------------------------------------------------------|
| 1 | BOLA                                | `AccountService.listAccounts/getAccount/deleteAccount`, same pattern in `TransactionService` and `LoanService` |
| 2 | Broken Authentication               | `SecurityConfig.jwtDecoder()` — `JwtTimestampValidator` omitted; `application.yml` — 24-char HMAC secret padded to 32 bytes |
| 3 | Unrestricted Resource Consumption   | `GET /transactions/report` (no pagination), `POST /transactions/bulk-transfer` (no array size limit), `/auth/token` (no rate limiting) |
| 4 | Sensitive Business Flow             | `TransactionService.transfer()` — no balance check, no amount cap, no ownership check on source; `LoanController.approveLoan()` — no self-approval check |
| 5 | SQL Injection                       | `TransactionService.searchTransactions()` — native query with direct string concatenation |
