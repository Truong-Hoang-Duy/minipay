# ADR-0005: No Lombok

- Status: Accepted
- Date: 2026-10-09

## Context
Lombok generates code by hooking into the compiler's internal AST. It hides what a class really
contains, and `@Data` on JPA entities is a known source of bugs: `equals`/`hashCode` over
lazy associations, `toString` triggering lazy loads or infinite recursion.

## Decision
Use Java records for value objects, commands and DTOs. Write entity constructors and accessors by
hand (or let the IDE generate them).

## Consequences
- More lines in entities, but every method is visible and deliberate. This matters because
  entity `equals`/`hashCode` is an interview topic in its own right.
- One less annotation processor in the build.
