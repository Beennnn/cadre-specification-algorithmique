# Test cases — SPEC-SPD-001

*One document per case. Each says what the case exists to catch, and — just as
usefully — what it would let through.*

A case is worth its line in the reference set only if some **wrong** implementation
fails it. A case that every plausible implementation passes costs maintenance and
buys nothing. Two of the six here are in that situation, and they are kept anyway:
the documents say why.

| Case | What it catches | What it lets through |
|---|---|---|
| [CT-01](CT-01.md) | The mean of the leg speeds, in place of the real average | The rounding mode; a leg-by-leg rounding |
| [CT-02](CT-02.md) | Nothing — **kept on purpose** as the counter-example | Everything CT-01 catches |
| [CT-03](CT-03.md) | An empty-sum or divide-by-zero on the degenerate journey | Any weighting error: one leg cannot be mis-weighted |
| [CT-04](CT-04.md) | A working precision too short for non-terminating durations | The rounding mode: the quotient is exact |
| [CT-05](CT-05.md) | A division by zero where a business rejection is required | Everything about the nominal path |
| [CT-06](CT-06.md) | `HALF_UP` in place of `HALF_EVEN`, and a wrong number of decimals | Nothing the others catch is weakened |

## How to read one

Each document has the same five parts:

| | |
|---|---|
| **Where it comes from** | the anchor in the specification, and the line of reference data |
| **What it covers** | the rules, invariants, parameters and error cases it exercises |
| **The data** | inputs and the expected result, recomputed by hand |
| **Why the case exists** | the wrong implementation it is aimed at |
| **What it does not test** | the honest half — what stays uncovered if this is the only case you run |

## Running them

```bash
cd code && javac -encoding UTF-8 -d /tmp/as $(find src -name '*.java')
cd .. && java -cp /tmp/as method.averagespeed.AverageSpeedTest \
    code/src/test/resources/reference-data.csv \
    spec/SPEC-SPD-001.en.md \
    reports/TEST-REPORT.md
```

The harness holds **no expected value of its own**: it replays
[`reference-data.csv`](../code/src/test/resources/reference-data.csv), which is section 10
of the specification transcribed without interpretation.
