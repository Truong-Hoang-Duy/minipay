# ADR-0003: Flyway owns the database schema

- Status: Accepted
- Date: 2026-10-09

## Context
`spring.jpa.hibernate.ddl-auto=update` lets Hibernate change tables at startup. It never drops
columns, cannot rename, cannot backfill data, gives no review step and no history. That is not
acceptable for a database holding money.

## Decision
Every schema change is a versioned SQL file in `src/main/resources/db/migration`
(`V<n>__<description>.sql`). Hibernate runs with `ddl-auto=validate`, so startup fails if the
entities and the schema disagree.

## Consequences
- Schema changes go through code review as plain SQL. We learn the SQL we would write in production.
- An applied migration is **never edited**: Flyway stores a checksum and refuses to start if it
  changes. Fixes are new migrations.
- Later, large-table changes must follow expand/contract (add column, backfill, switch reads,
  drop old column in a later release) so they stay compatible with the running version.
