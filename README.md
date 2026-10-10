# MiniPay

A mini e-wallet that recreates the core flows of a real payment app (top-up, P2P transfer,
merchant QR payment, refund, notification, promotion). It is a learning project about
correctness and performance in high-load fintech backends.

## Stack
Java 21 · Spring Boot 3.5 · Maven · MySQL 8.4 · Redis 7.4 · Kafka 3.9 (KRaft) · Flyway ·
JUnit 5 · Testcontainers · GitHub Actions

## Prerequisites
- JDK 21, with `JAVA_HOME` pointing to it (the build fails fast on an older JDK)
- Docker (Docker Desktop on Windows/macOS)

## Run locally
```bash
docker compose up -d              # MySQL :3306, Redis :6379, Kafka :9092
./mvnw spring-boot:run            # profile "local" is the default
curl localhost:8080/actuator/health

docker compose --profile tools up -d   # optional: Kafka UI on http://localhost:8090
```

## Build and test
```bash
./mvnw test              # unit tests only (*Test), no Docker needed
./mvnw verify            # + integration tests (*IT) on real containers + format check
./mvnw spotless:apply    # format code (palantir-java-format)
```

## Project layout
```
src/main/java/com/minipay/
  common/            shared building blocks (Money, ...)
  <feature>/         one package per feature: wallet, payment, ... (see ADR-0001)
src/main/resources/
  application*.yml   common config + per-profile overrides (local, prod)
  db/migration/      Flyway migrations (V<n>__<description>.sql)
docs/
  adr/               architecture decision records
  phases/            per-phase exercises and interview questions
  LEARNING_LOG.md    what was learned, decisions, trade-offs
```

## Conventions
- Money is `Money` (a `long` count of VND), never `double` (ADR-0002).
- Schema changes only through new Flyway migrations, never by editing an applied one (ADR-0003).
- `*Test` = unit test, `*IT` = integration test with Testcontainers (ADR-0004).
- Code, comments and commit messages in English. Commits follow Conventional Commits
  (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`).

## Benchmarks
Filled in from Phase 3 onwards.
