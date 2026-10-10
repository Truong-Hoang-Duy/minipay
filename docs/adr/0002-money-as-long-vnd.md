# ADR-0002: Represent money as a `long` count of VND

- Status: Accepted
- Date: 2026-10-09

## Context
Floating-point types (`double`, `float`) are binary fractions and cannot represent most decimal
amounts exactly (`0.1 + 0.2 != 0.3`). Above 2^53 a `double` cannot even hold every integer.
The options are `BigDecimal` or an integer count of the smallest currency unit.

## Decision
`Money` is a record wrapping a `long` number of dong. MiniPay is single-currency (VND), and VND
has no minor unit in practice. Arithmetic uses `Math.addExact`/`subtractExact` so overflow throws.
In MySQL, amounts are stored as `BIGINT`.

## Consequences
- Fast (primitive arithmetic, 8 bytes), exact, and trivially comparable.
- `long` max is about 9.2 * 10^18 VND, far beyond any realistic balance.
- Percentages (fees, cashback) need an explicit rounding rule when converting back to whole dong.
  Do that in one place, with `BigDecimal` or integer math on basis points, never `double`.
- Multi-currency would need a currency field plus per-currency scale. Out of scope; revisit if needed.
