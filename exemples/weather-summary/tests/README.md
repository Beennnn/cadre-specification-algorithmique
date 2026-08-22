# Test cases — SPEC-WTH-001

*One document per case. Each says what the case exists to catch, and — just as
usefully — what it would let through.*

Seven cases: four nominal, three rejections. Four of them were **built on purpose**,
and the documents say what each was built for.

| Case | What it catches | What it lets through |
|---|---|---|
| [CT-01](CT-01.md) | An outlier the acquisition chain called `VALID`; a frost run ended at exactly 0.0 °C | The rounding mode; the loop's exit |
| [CT-02](CT-02.md) | Nothing on its own — it is the **control** that proves the rejection worked | Everything CT-01 catches |
| [CT-03](CT-03.md) | A summary published over two readings | The whole nominal path |
| [CT-04](CT-04.md) | **A loop that runs out and says nothing** | The frost episodes; the rounding mode |
| [CT-05](CT-05.md) | Two readings at one instant, leaving `RG-050` undefined | The nominal path |
| [CT-06](CT-06.md) | A summary silently describing two days | The nominal path |
| [CT-07](CT-07.md) | **`HALF_UP` where `P-04` says `HALF_EVEN` — and a mean summed in double** | A tie that rounds upward |

## The pair that carries the most

`CT-01` and `CT-02` are the same day, one reading apart. CT-01 has a 45.0 °C reading the
sensor flagged `VALID`; CT-02 does not. **Both publish a mean of 0.5 °C.** That equality is
the proof that `RG-030` did its job: an implementation that skipped the outlier rejection
would publish 6.1 °C for CT-01 and still pass every other case in the set.

## Covering a rule is not deciding a parameter

Six parameters govern this calculation. A case that runs through `RG-040` "covers" it in
the bookkeeping sense while deciding nothing about `P-04`: if the mean is 0.5 either way,
the rounding mode was never consulted.

The harness answers the real question by **re-running every case with one parameter
changed** and reporting which cases move:

| Parameter | Perturbation | Decided by |
|---|---|---|
| `P-01` rejection factor | 2.0 → 2.5 | CT-01, CT-04 |
| `P-02` maximum rounds | 5 → 10 | CT-04 |
| `P-03` minimum readings | 6 → 2 | CT-03 |
| `P-04` rounding mode | `HALF_EVEN` → `HALF_UP` | **CT-07 alone** |
| `P-05` published decimals | 1 → 2 | CT-01, CT-02, CT-04, CT-07 |
| `P-06` frost threshold | 0.0 → 0.5 °C | CT-01, CT-02, CT-04, CT-07 |

Take CT-07 out and `P-04` goes back to what it was before v1.1.0: approved, dated,
implemented, and arbitrated by nothing.

## How to read one

| | |
|---|---|
| **Where it comes from** | the anchor in the specification, and the line of reference data |
| **What it covers** | the rules, invariants, parameters and error cases it exercises |
| **The data** | inputs and the expected result, recomputed by hand |
| **Why the case exists** | the wrong implementation it is aimed at |
| **What it does not test** | the honest half — what stays uncovered if this is the only case you run |

## Running them

```bash
cd code && javac -encoding UTF-8 -d /tmp/wth $(find src -name '*.java')
cd .. && java -cp /tmp/wth method.weathersummary.WeatherSummaryTest \
    code/src/test/resources/reference-data.csv \
    spec/SPEC-WTH-001.en.md \
    reports/TEST-REPORT.md
```

The harness holds **no expected value of its own**: it replays
[`reference-data.csv`](../code/src/test/resources/reference-data.csv), which is section 10
of the specification transcribed without interpretation.
