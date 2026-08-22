# Test cases — SPEC-MAS-001

*One document per case. Each says what the case exists to catch, and — just as
usefully — what it would let through.*

Ten cases: six nominal, four rejections. Three of them were **built on purpose** to expose
something no other case reached, and the documents say which.

| Case | What it catches | What it lets through |
|---|---|---|
| [CT-01](CT-01.md) | A residual dropped instead of allocated | The tie-break; the sign of the residual |
| [CT-02](CT-02.md) | A residual taken as an absolute value | The tie-break |
| [CT-03](CT-03.md) | **A missing tie-break in `RG-050`** | The residual bound |
| [CT-04](CT-04.md) | An allocation that fires when there is nothing to allocate | Everything about non-zero residuals |
| [CT-05](CT-05.md) | `≥` written where `RG-040` says `>` | The rejection above the bound |
| [CT-06](CT-06.md) | Fractions accepted without summing to 1 | The nominal path |
| [CT-07](CT-07.md) | Duplicate identifiers accepted, making `RG-050` undefined | The nominal path |
| [CT-08](CT-08.md) | A target mass that cannot be split into whole steps | The nominal path |
| [CT-09](CT-09.md) | **A residual never checked against `P-02`** | The value of the bound itself |
| [CT-10](CT-10.md) | **`HALF_UP` written where `P-01` says `HALF_EVEN`** | A tie that rounds upward |

## The two cases the invariants cannot replace

`CT-03` and `CT-10` are both passed by *every* invariant in the specification while the
weighed masses are wrong. `CT-03` catches an order-dependent allocation, `CT-10` a wrong
rounding mode — and in each, `INV-01` to `INV-04` hold either way. What catches them is the
list of **approved masses**. That is the difference between a reference set and a property
test, and both are in this suite for that reason.

## The pair that carries the most

`CT-05` and `CT-09` are the same input shape at two settings: a residual **exactly on**
the bound, which is accepted, and one **just past it**, which is refused. Either one alone
proves little; the pair pins down both the value of `P-02` and the strictness of the
comparison.

## How to read one

Each document has the same five parts:

| | |
|---|---|
| **Where it comes from** | the anchor in the specification, and the line of reference data |
| **What it covers** | the rules, invariants and error cases it exercises |
| **The data** | inputs and the expected result, recomputed by hand |
| **Why the case exists** | the wrong implementation it is aimed at |
| **What it does not test** | the honest half — what stays uncovered if this is the only case you run |

## Running them

```bash
cd code && javac -encoding UTF-8 -d /tmp/mb $(find src -name '*.java')
cd .. && java -cp /tmp/mb method.massbalance.MassBalanceTest \
    code/src/test/resources/reference-data.csv \
    spec/SPEC-MAS-001.en.md \
    reports/TEST-REPORT.md
```

The harness holds **no expected value of its own**: it replays
[`reference-data.csv`](../code/src/test/resources/reference-data.csv), which is section 10
of the specification transcribed without interpretation.
