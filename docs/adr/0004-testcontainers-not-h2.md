# ADR-0004: Integration tests run on real infrastructure (Testcontainers), not H2

- Status: Accepted
- Date: 2026-10-09

## Context
The interesting bugs in this project (lost updates, deadlocks, isolation anomalies, gap locks,
Kafka redelivery) depend on the real engine's behaviour. H2 in "MySQL mode" only imitates the
syntax; its locking, isolation and optimizer are different.

## Decision
- `*Test` classes: plain unit tests, no Spring context, no Docker. Run by Surefire in `mvn test`.
- `*IT` classes: Spring context plus MySQL 8.4, Redis 7.4 and Kafka 3.9 in Docker via
  Testcontainers, with the same images and server flags as `docker-compose.yml`.
  Run by Failsafe in `mvn verify`.

## Consequences
- Tests prove behaviour on the engine we ship on. CI needs Docker (GitHub's Ubuntu runners have it).
- Integration tests are slower (first run pulls images). Keep most logic testable in unit tests.
- Containers are shared through Spring's test context cache. Every IT should use the same
  configuration so the context (and containers) is reused.
