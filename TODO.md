# PaymeshLedger — TODO / Roadmap

A review-driven backlog to take this from a tutorial project to a resume-ready,
production-shaped distributed payment ledger. Items are grouped by priority.

---

## 🔴 Tier 0 — Correctness bugs (fix first)

- [ ] **Prevent overdraft on debit.** `WalletService.debit()` bypasses
      `Wallet.hasSufficientBalance()` and writes a raw `balance.subtract(amount)`.
      Add a balance check before debiting. (`service/WalletService.java:42`)
- [ ] **Fix lost-update race.** `credit`/`debit` and `DebitSourceWalletStep` do
      read-modify-write with an absolute write. Switch to an atomic DB update:
      `UPDATE wallet SET balance = balance - :amount WHERE user_id = :id`
      (and `+ :amount` for credit). (`repository/WalletRepository.java`,
      `saga/step/DebitSourceWalletStep.java:39`)
- [ ] **Validate amounts.** Reject `null`, zero, and negative amounts on credit,
      debit, and transfer. Add Bean Validation (`@Valid`, `@Positive`,
      `@NotNull`) on the DTOs.
- [ ] **Harden `getWalletByUserId().getFirst()`** — throws on zero wallets and
      silently ignores duplicates. Return a proper not-found or enforce
      one-wallet-per-user. (`service/WalletService.java:38`)

## 🔴 Tier 1 — The core story (highest resume value)

- [ ] **Expose saga status.** Add `GET /transactions/saga/{sagaInstanceId}`
      returning the saga state (PENDING / COMPLETED / COMPENSATED / FAILED) so
      the outcome of a transfer is observable. Transfer is currently
      fire-and-forget.
- [ ] **Expose transaction history.** Wire the existing (but unused)
      `TransactionService` reads into endpoints:
      `GET /wallets/{id}/transactions`, `GET /transactions/{id}`,
      `GET /transactions/saga/{id}`. Add pagination.
- [ ] **Frontend: poll saga status** after a transfer and show the real
      outcome (success / failed / reversed) instead of an optimistic toast.
- [ ] **Frontend: real transaction history** sourced from the new endpoints
      (replaces the current session-only "Recent activity").
- [ ] **Tests — the headline bullet.**
  - [ ] JUnit + Mockito unit tests for `WalletService` / `TransactionService`.
  - [ ] **Testcontainers** integration test that spins up MySQL and verifies
        the saga's **compensation path**: force the credit step to fail and
        assert the source-wallet debit is rolled back.
- [ ] **Authentication & authorization.** Spring Security + JWT login; wallets
      owned by an authenticated user (stop trusting wallet id from the client /
      `localStorage`).

## 🟠 Tier 2 — Production polish

- [ ] **Global error handling.** `@ControllerAdvice` with a consistent error
      body and proper HTTP codes (404 not-found, 400 validation, 409 conflict).
      Replace raw `RuntimeException` and the `catch(Exception) -> 500 null`.
- [ ] **Idempotency keys** on transfer to make retries safe (no double-spend).
- [ ] **Remove committed secret.** `sharding.yml` has a hardcoded DB password —
      move to env vars / externalized config and rotate the credential.
- [ ] **Docker Compose** that brings up the two MySQL shards + backend +
      Next.js client with one command.
- [ ] **DB migrations** with Flyway/Liquibase; stop relying on
      `ddl-auto: update`.
- [ ] **CI** via GitHub Actions (build + test on every push).
- [ ] **CORS config** on the backend (frontend currently relies on a Next.js
      proxy rewrite).

## 🟡 Tier 3 — Senior signal

- [ ] **Observability.** Spring Boot Actuator + Prometheus/Grafana; distributed
      tracing across saga steps.
- [ ] **Rate limiting** on money-movement endpoints.
- [ ] **README with architecture diagram** explaining the saga orchestration +
      compensation and the user-id sharding strategy (and *why*).
- [ ] **Money modeling.** Add a currency field and enforce decimal scale.

## Frontend backlog (smaller items)

- [ ] Loading skeletons + error boundary + retry.
- [ ] Accessibility pass (focus traps in modal, ARIA labels, keyboard nav).
- [ ] Make currency configurable (currently hardcoded to INR).
- [ ] `.env.example` documenting `BACKEND_URL`.

---

### Suggested first pass (tightly related, do together)
Balance/validation bug fixes **+** saga-status & transaction-history endpoints
**+** wire both into the UI. This fixes the correctness issues and makes the
saga's outcome actually visible end-to-end.
