# Test cases — SPEC-SPD-001

*One short page per case: what it catches, and what it lets through.*

| Case | What it catches | What it lets through |
|---|---|---|
| [CT-01](CT-01.md) | The average of the leg speeds, in place of the real one | The rounding mode |
| [CT-02](CT-02.md) | Nothing — **kept on purpose** as the counter-example | Everything CT-01 catches |
| [CT-03](CT-03.md) | A broken accumulator on a one-leg journey | Any weighting error |
| [CT-04](CT-04.md) | Durations with no finite decimal form | The rounding mode |
| [CT-05](CT-05.md) | A division by zero where a rejection is required | The whole nominal path |
| [CT-06](CT-06.md) | `HALF_UP` in place of `HALF_EVEN` | A tie that rounds upward |

**Two of the six catch nothing on their own**, and they say so. A case that every plausible
implementation passes costs maintenance and buys nothing — unless, like CT-02, it is there
to show exactly that.

```bash
cd ../code && javac -encoding UTF-8 -d /tmp/as $(find src -name '*.java')
cd .. && java -cp /tmp/as method.averagespeed.AverageSpeedTest \
    code/src/test/resources/reference-data.csv spec/SPEC-SPD-001.en.md reports/TEST-REPORT.md
```

The harness holds **no expected value of its own**: it replays
[`reference-data.csv`](../code/src/test/resources/reference-data.csv), which is section 10
of the specification transcribed without interpretation.
