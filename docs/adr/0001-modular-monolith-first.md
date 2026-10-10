# ADR-0001: Start as a modular monolith, packaged by feature

- Status: Accepted
- Date: 2026-10-09

## Context
MiniPay will be split into wallet, payment and notification services in Phase 5. Starting with
microservices on day one means paying for network calls, distributed transactions and deployment
complexity before the domain is understood.

## Decision
One Spring Boot application and one Maven module until Phase 5. Code is packaged **by feature**
(`com.minipay.wallet`, `com.minipay.payment`, ...) rather than by layer (`controller`, `service`,
`repository`). Shared building blocks live in `com.minipay.common`.

## Consequences
- A future service = one feature package moved out. Boundaries are visible early.
- Rule: a feature must not reach into another feature's repository or entities; it calls the
  other feature's public service. Not enforced by tooling yet (candidate: ArchUnit test).
- One database for now, which makes Phase 2-4 transactions simple. Phase 5 must deal with the
  loss of that simplicity (saga, outbox).
