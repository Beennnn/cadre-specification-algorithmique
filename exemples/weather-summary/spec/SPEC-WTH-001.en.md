# SPEC-WTH-001 — Daily summary of a weather station

| | |
|---|---|
| **Identifier** | SPEC-WTH-001 |
| **Version** | 1.0.0 |
| **Status** | Approved |
| **Business approver** | Observation network manager |
| **Technical co-author** | Climate data team |
| **Reference glossary** | [GLOSSAIRE.md](../../../GLOSSAIRE.md) v1.0.0 |

> **Why this example.** It is the one that exercises **the whole pseudo-language**. Not to
> show off: the calculation genuinely needs it — discarding readings, grouping them,
> ordering them, rejecting outliers **iteratively**, then labelling the result through a
> decision table.
>
> It sits between [the average speed](../../average-speed/) — three rules, read in five
> minutes — and [the vehicle range](../../fil-rouge/), which spreads over a dozen
> functions.

---

## 1. Purpose and context

A weather station records temperature readings during the day. At the end of the day we
publish a summary: mean, minimum, maximum, and the frost episodes.

Two things make it less simple than it looks. Sensors **fail**: a reading can be missing,
flagged invalid, or plainly absurd — a probe in the sun reads 45 °C in January. And a
summary computed on bad readings is worse than no summary at all, because nobody can see it
is wrong.

## 2. Scope

**In scope.** For one station and one day: filtering the readings, rejecting outliers,
statistics, frost episodes, and a quality label on the result.

**Out of scope.** Sensor calibration, gap filling between stations, forecasting, and any
multi-day aggregation.

## 3. Glossary

| Term | Definition |
|---|---|
| **Reading** | One temperature measured at one instant, with a quality flag from the acquisition chain. |
| **Retained reading** | A reading that survived both filtering (`RG-010`) and outlier rejection (`RG-030`). Only these enter the statistics. |
| **Outlier** | A retained-so-far reading too far from the mean of its own group. "Too far" is `P-01` standard deviations — a business decision, not a statistical constant. |
| **Frost episode** | A maximal run of consecutive readings **strictly below** `P-06`. Strictly: a reading at exactly 0.0 °C is not frost (`RG-050`). |
| **Quality label** | What the summary is worth: `GOOD`, `ACCEPTABLE` or `SUSPECT`. It travels with the result and is never dropped. |

---

## 4. Inputs

```
request :
    station_id       : Identifier(text, 3 to 12 characters)      [CharacterString]
    observation_date : Date(Gregorian calendar)                  [DateAndTime]
    readings         : Sequence[Reading](0 .. 1440)

Reading :
    recorded_at  : DateAndTime(UTC, to the minute)               [DateAndTime]
    temperature  : Temperature(°C, −90.0 .. +60.0, 1 decimal)    [Real] — may be ABSENT
    quality_flag : Enumerated { VALID, SUSPECT, FAULTY }          [Enumerated]
```

> **`ABSENT` is not `−999`.** Acquisition chains traditionally encode a missing reading
> with an impossible number. That convention has produced enough winters at −999 °C to be
> banned here: a missing temperature is `ABSENT`, and `RG-010` says what happens to it.

## 5. Outputs

```
result :
    station_id      : Identifier(text, 3 to 12 characters)       [CharacterString]
    retained_count  : Integer(≥ 0)                               [Integer]
    rejected_count  : Integer(≥ 0)                               [Integer]
    mean_temperature: Temperature(°C, P-05 decimals)             [Real]
    min_temperature : Temperature(°C, 1 decimal)                 [Real]
    max_temperature : Temperature(°C, 1 decimal)                 [Real]
    frost_episodes  : Sequence[FrostEpisode]
    quality_label   : Enumerated { GOOD, ACCEPTABLE, SUSPECT }   [Enumerated]

FrostEpisode :
    started_at    : DateAndTime(UTC, to the minute)              [DateAndTime]
    ended_at      : DateAndTime(UTC, to the minute)              [DateAndTime]
    reading_count : Integer(≥ 1)                                 [Integer]
```

## 6. Parameters

| Id | Name | Value | Unit | Who may change it | Effective date |
|---|---|---|---|---|---|
| <a id="p-01"></a>`P-01` | Outlier rejection factor | 2.0 | standard deviations | Observation network manager | 2026-01-01 |
| <a id="p-02"></a>`P-02` | Maximum rejection rounds | 5 | rounds | Observation network manager | 2026-01-01 |
| <a id="p-03"></a>`P-03` | Minimum readings for a summary | 6 | readings | Observation network manager | 2026-01-01 |
| <a id="p-04"></a>`P-04` | Rounding mode of the published mean | `HALF_EVEN` | — | Observation network manager | 2026-01-01 |
| <a id="p-05"></a>`P-05` | Decimals of the published mean | 1 | — | Observation network manager | 2026-01-01 |
| <a id="p-06"></a>`P-06` | Frost threshold | 0.0 | °C | Observation network manager | 2026-01-01 |

---

## 7. Rules

### 7.1 The algorithm, in one piece

This is the whole calculation, in the order the business states it. Every keyword used
here belongs to the closed lexicon of [CADRE §2.2](../../../CADRE.md).

```
DEFINE summarise_station_day(request) : result

    PRECONDITIONS  E-WTH-001, E-WTH-002

    -- RG-010 · keep only what can be trusted
    LET usable = FILTER request.readings
                 WHERE temperature ≠ ABSENT
                   AND quality_flag = VALID
                   AND temperature BETWEEN -90.0 AND 60.0

    -- RG-020 · a summary on too few readings is not a summary
    IF COUNT OF usable < P-03 THEN
        RAISE ERROR E-WTH-003 "not enough usable readings to publish a summary"
    ELSE
        the calculation continues
    END IF

    -- RG-030 · iterative outlier rejection, bounded by P-02
    LET retained = usable
    LET round_number = 0
    LET converged = FALSE

    WHILE NOT converged AND round_number < P-02
        LET mean_now  = MEAN OF temperature OVER retained
        LET spread    = STANDARD DEVIATION OF temperature OVER retained
        LET outliers  = FILTER retained WHERE |temperature − mean_now| > P-01 × spread

        IF NONE OF retained IS IN outliers THEN
            converged = TRUE
        ELSE
            retained     = FILTER retained WHERE temperature IS NOT IN outliers
            round_number = round_number + 1
        END IF
    END WHILE

    -- RG-040 · the statistics, on the retained readings only
    retained_count   = COUNT OF retained
    rejected_count   = COUNT OF usable − COUNT OF retained
    mean_temperature = ROUND( MEAN OF temperature OVER retained, P-05, P-04 )
    min_temperature  = MINIMUM OF temperature OVER retained
    max_temperature  = MAXIMUM OF temperature OVER retained

    -- RG-050 · frost episodes, in chronological order
    LET chronological = SORT retained BY recorded_at ASCENDING
    LET frost_runs    = GROUP chronological BY consecutive runs where temperature < P-06

    frost_episodes = empty
    FOR EACH run IN frost_runs
        LET episode = new FrostEpisode
        episode.started_at    = recorded_at OF THE FIRST OF run
        episode.ended_at      = recorded_at OF THE LAST OF run
        episode.reading_count = COUNT OF run
        frost_episodes = frost_episodes followed by episode
    END FOR

    -- RG-060 · what the summary is worth
    IF NOT converged THEN
        quality_label = SUSPECT
    ELSE IF rejected_count = 0 THEN
        quality_label = GOOD
    ELSE
        quality_label = ACCEPTABLE
    END IF

    RETURN result
```

> **`STANDARD DEVIATION OF … OVER …` is not in the lexicon, and that is deliberate.** It is
> a named statistical operation, like `arctangent` or `logarithm`: the pseudo-language does
> not redefine mathematics. What the specification owes the developer is **which** standard
> deviation — here the sample one, divisor `n − 1` — and that is stated in `RG-030`.

### RG-005 — What the summary identifies

```
result.station_id = request.station_id
```

The summary carries the station it describes. The day is not carried in the result: it is
`observation_date`, and `E-WTH-002` guarantees that every reading belongs to it — so the
summary describes that day and no other.

> **A pass-through still deserves a rule.** Without one, nothing in the document says that
> `station_id` comes out unchanged, and nothing forbids an implementation from normalising
> it, trimming it, or upper-casing it on the way. It looks obvious; obvious is exactly what
> nobody writes down and everybody reads differently.

### RG-010 — Usable readings

```
usable = FILTER readings
         WHERE temperature ≠ ABSENT
           AND quality_flag = VALID
           AND temperature BETWEEN -90.0 AND 60.0
```

A reading is discarded if it is missing, if the acquisition chain flagged it, or if it
falls outside the physically possible range.

> **`SUSPECT` readings are discarded, not kept with a warning.** The acquisition chain
> knows something we do not, and a summary that silently mixes suspect readings with good
> ones cannot be audited afterwards.

### RG-020 — Minimum number of readings

```
IF COUNT OF usable < P-03 THEN
    RAISE ERROR E-WTH-003 "not enough usable readings to publish a summary"
ELSE
    the calculation continues
END IF
```

> **Publishing nothing beats publishing a mean over two readings.** The threshold `P-03` is
> a business decision: it is what the network manager is prepared to defend.

### RG-030 — Iterative outlier rejection

Each round computes the mean and the **sample** standard deviation (divisor `n − 1`) of the
readings still retained, then discards those further than `P-01` standard deviations from
that mean. It stops when a round discards nothing.

```
WHILE NOT converged AND round_number < P-02
    LET mean_now = MEAN OF temperature OVER retained
    LET spread   = STANDARD DEVIATION OF temperature OVER retained
    LET outliers = FILTER retained WHERE |temperature − mean_now| > P-01 × spread

    IF NONE OF retained IS IN outliers THEN
        converged = TRUE
    ELSE
        retained     = FILTER retained WHERE temperature IS NOT IN outliers
        round_number = round_number + 1
    END IF
END WHILE
```

> **An explicit loop must be justified, and this one is.** The rejection is genuinely
> iterative: removing an outlier changes the mean, which can expose another. There is no
> closed form.
>
> **Stopping criterion**: a round that discards nothing. **Maximum rounds**: `P-02` = 5.
> **On non-convergence**: the calculation does **not** fail — it publishes what it has, and
> `RG-060` labels the result `SUSPECT`. Stopping without saying so would be the real defect.

> **Why `>` and not `≥`.** A reading at exactly `P-01` standard deviations is **kept**. The
> boundary has to fall on one side, and the network manager chose the inclusive one: at the
> boundary, the reading is not evidence of a fault.

### RG-040 — Statistics

```
retained_count   = COUNT OF retained
rejected_count   = COUNT OF usable − COUNT OF retained
mean_temperature = ROUND( MEAN OF temperature OVER retained, P-05, P-04 )
min_temperature  = MINIMUM OF temperature OVER retained
max_temperature  = MAXIMUM OF temperature OVER retained
```

Only the published mean is rounded. The minimum and the maximum are readings, and a reading
is published as it was measured.

> **No tie-break is needed here, and that is worth stating.** `MINIMUM` and `MAXIMUM` yield
> a **value**, not a chosen reading: if three readings share the lowest temperature, the
> minimum is that temperature, whichever one we look at. A tie-break would only be needed
> if the output were *which* reading was coldest.

### RG-050 — Frost episodes

Readings are put in chronological order, then cut into maximal runs of consecutive readings
strictly below `P-06`:

```
LET chronological = SORT retained BY recorded_at ASCENDING
LET frost_runs    = GROUP chronological BY consecutive runs where temperature < P-06

FOR EACH run IN frost_runs
    started_at    = recorded_at OF THE FIRST OF run
    ended_at      = recorded_at OF THE LAST OF run
    reading_count = COUNT OF run
END FOR
```

> **Strictly below, and it matters.** A reading at exactly 0.0 °C is **not** frost. Water
> does not freeze instantly at zero, and the network publishes frost warnings: a threshold
> at `≤` would raise an alert every autumn night that touches zero.
>
> **`THE FIRST` and `THE LAST` need no tie-break, because the run is ordered and its
> instants are unique.** `RG-050` sorts by `recorded_at`, and `E-WTH-001` rejects two
> readings sharing one instant — so "the first of the run" designates one reading and only
> one. Take away either of those and the superlative would become ambiguous.
>
> **The order comes from `recorded_at`, never from the order in the input.** Two stations
> send their readings in different orders, and a summary that depended on it would not be
> reproducible.

### RG-060 — Quality label

| Convergence (`RG-030`) | Rejected readings | **`quality_label`** |
|---|---|---|
| not reached in `P-02` rounds | — | **`SUSPECT`** |
| reached | 0 | **`GOOD`** |
| reached | ≥ 1 | **`ACCEPTABLE`** |

The table is complete: the three combinations cover every possible case, and none overlaps.

### Coverage table

| Rule | Covered by |
|---|---|
| `RG-005` | CT-01, CT-02, CT-04 |
| `RG-010` | CT-01, CT-02, CT-03 |
| `RG-020` | CT-03 |
| `RG-030` | CT-01, CT-02, CT-04 |
| `RG-040` | CT-01, CT-02, CT-04 |
| `RG-050` | CT-01, CT-02 |
| `RG-060` | CT-01 (`ACCEPTABLE`), CT-02 (`GOOD`), CT-04 (`SUSPECT`) |

---

## 8. Invariants

| Id | Statement |
|---|---|
| <a id="inv-01"></a>`INV-01` | `retained_count + rejected_count = COUNT OF usable`. No reading disappears without being counted somewhere. |
| <a id="inv-02"></a>`INV-02` | `min_temperature ≤ mean_temperature ≤ max_temperature`, up to the rounding of the mean. |
| <a id="inv-03"></a>`INV-03` | Frost episodes are **disjoint** and in increasing order of `started_at`. Two episodes never overlap: a run is maximal by construction. |
| <a id="inv-04"></a>`INV-04` | The result is **invariant under permutation** of the input readings. `RG-050` sorts by `recorded_at`, so the order of arrival changes nothing. |
| <a id="inv-05"></a>`INV-05` | `retained_count ≥ P-03`, always. Below that, `RG-020` rejected before any statistic was computed. |

## 9. Business error cases

| Id | Condition | Behaviour |
|---|---|---|
| <a id="e-wth-001"></a>`E-WTH-001` | Two readings carry the same `recorded_at` | Reject: the chronological order of `RG-050` would not be defined |
| <a id="e-wth-002"></a>`E-WTH-002` | A `recorded_at` falls outside `observation_date` | Reject: the summary would mix two days |
| <a id="e-wth-003"></a>`E-WTH-003` | Fewer than `P-03` usable readings | Reject, reporting how many were usable |

> **Non-convergence is not in this table**, and that is the point of `RG-060`. It is a
> result to be labelled, not a failure to be raised.

---

## 10. Test set

All cases use station `STA-001` on 2026-01-15, hourly readings from 00:00 UTC, and the
parameters above.

### CT-01 — One outlier, converges, one frost episode

| Time | Temperature | Flag |
|---|---|---|
| 00:00 | 2.0 | VALID |
| 01:00 | 1.5 | VALID |
| 02:00 | 0.5 | VALID |
| 03:00 | −0.5 | VALID |
| 04:00 | −1.0 | VALID |
| 05:00 | 0.0 | VALID |
| 06:00 | 1.0 | VALID |
| 07:00 | 45.0 | VALID |

**Rejection rounds.** Round 1: mean 6.0625, deviation 15.7649, threshold ±31.5297 →
**45.0 discarded**. Round 2: mean 0.5000, deviation 1.0801, threshold ±2.1602 → nothing
discarded, **converged**.

| Output | Value |
|---|---|
| `retained_count` | **7** |
| `rejected_count` | **1** |
| `mean_temperature` | **0.5 °C** |
| `min_temperature` | **−1.0 °C** |
| `max_temperature` | **2.0 °C** |
| `frost_episodes` | **one**: 03:00 → 04:00, 2 readings |
| `quality_label` | **`ACCEPTABLE`** |

**Two things are settled here at once.** The 45.0 °C reading was flagged `VALID` by the
chain — only `RG-030` catches it. And 05:00 at exactly 0.0 °C is **not** in the frost
episode, which ends at 04:00.

### CT-02 — No outlier

The same eight readings **without** the 07:00 one. Round 1 discards nothing: converged
immediately.

| Output | Value |
|---|---|
| `retained_count` | **7** · `rejected_count` **0** |
| `mean_temperature` | **0.5 °C** |
| `frost_episodes` | one: 03:00 → 04:00, 2 readings |
| `quality_label` | **`GOOD`** |

**The mean is the same as CT-01, 0.5 °C.** That is the proof that rejection worked: an
implementation that skipped `RG-030` would publish 6.1 °C for CT-01.

### CT-03 — Not enough usable readings

Eight readings, but five flagged `FAULTY` and one `ABSENT`: two usable, below `P-03` = 6.
Rejected by `E-WTH-003`, **before** any statistic is computed.

### CT-04 — Non-convergence

Twelve readings whose values are 0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024 °C, clipped
to the physical range then re-expressed within it. Each round discards exactly the largest
value, and five rounds are not enough.

| Round | Mean | Deviation | Discarded |
|---|---|---|---|
| 1 | 170.583 | 308.798 | 1024 |
| 2 | 93.000 | 159.495 | 512 |
| 3 | 51.100 | 82.509 | 256 |
| 4 | 28.333 | 42.749 | 128 |
| 5 | 15.875 | 22.184 | 64 |

After `P-02` = 5 rounds the process has not converged. The summary **is still published**,
on the 7 remaining readings, with `quality_label` = **`SUSPECT`**.

**This is the case that documents the loop's exit.** Without it, nobody would know what
happens when the iteration runs out — and every implementer would decide for themselves.

### Provenance and validation

| | |
|---|---|
| **Provenance** | Rounds recomputed step by step in exact arithmetic, independently of any implementation |
| **How they were examined** | Each round was recomputed: mean, sample deviation with divisor `n − 1`, threshold, discarded set. The frost episodes were checked reading by reading against the strict `<` bound |
| **What the examination produced** | CT-04 was **built** to make the loop run out: no natural dataset did it, and without it the non-convergence branch of `RG-060` was covered by nothing |
| **Approved by** | Observation network manager, 2026-08-22 |

---

## 11. Constraints and requirements

| Id | Statement | Source | Owner | Verification |
|---|---|---|---|---|
| <a id="ex-01"></a>`EX-01` | Double precision is enough: readings carry one decimal and an uncertainty of ±0.2 °C, far above any numerical noise | Sensor specification | IT architecture | Design review |
| <a id="ex-02"></a>`EX-02` | 1 440 readings per station per day, 3 000 stations, one summary per station per day | Network sizing | Operations manager | Measurement in operation |
| <a id="ex-03"></a>`EX-03` | The summary is **replayable identically** from the archived readings, for 30 years | Climate archiving `CLI-ARC-1` | Quality assurance | Annual replay against archive |

## 12. Open questions

| Id | Question | Decider | Due | Status |
|---|---|---|---|---|
| <a id="q-01"></a>`Q-01` | Should a station whose summary is `SUSPECT` three days running be taken out of the network automatically? | Observation network manager | 2026-12-01 | Open |
| <a id="q-02"></a>`Q-02` | Should `P-01` depend on the season? Winter dispersion is naturally larger | Observation network manager | | Closed |

## 13. History and change notices

| Version | Date | Change | Impact on results | Notice |
|---|---|---|---|---|
| 1.0.0 | 2026-08-22 | Initial version | — | — |

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../../../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `SPEC-WTH-001` | `d016ba2f-3f89-4b82-92cc-e30124eb547f` | document | SPEC-WTH-001 — Daily summary of a weather station |
| `RG-005` | `154eba7a-a195-45cd-85ce-a25c6488c7f8` | règle | What the summary identifies |
| `RG-010` | `8c70a047-add6-44ca-be32-bde0cfe2ae3f` | règle | Usable readings |
| `RG-020` | `d94a065b-1bf3-4a0e-bc6e-488713d5da24` | règle | Minimum number of readings |
| `RG-030` | `830ed5d0-0f38-4489-a084-42d3a5e7f00a` | règle | Iterative outlier rejection |
| `RG-040` | `4fe6018e-437e-401b-b4e1-30cb153b7788` | règle | Statistics |
| `RG-050` | `1d9ad939-2231-4a94-8e2a-01b72c5d94f8` | règle | Frost episodes |
| `RG-060` | `efe07b5d-a903-4138-860d-bd0f6005a17d` | règle | Quality label |
| `CT-01` | `bf599ac6-916a-4a89-b7b4-8985a6d093b6` | cas de test | One outlier, converges, one frost episode |
| `CT-02` | `a6b6ce38-a651-4269-bdb2-3bea51583d09` | cas de test | No outlier |
| `CT-03` | `4784e57e-41d9-4424-bd92-2d7fa1cf0152` | cas de test | Not enough usable readings |
| `CT-04` | `45f29eca-fd22-4d17-8b9d-e9817c3f52f8` | cas de test | Non-convergence |
