# SPEC-SPD-001 — Average speed of a journey

| | |
|---|---|
| **Identifier** | SPEC-SPD-001 |
| **Version** | 1.0.0 |
| **Status** | Approved |
| **Business approver** | Fleet operations manager |
| **Technical co-author** | Mobility software team |
| **Reference glossary** | [GLOSSAIRE.md](../../../GLOSSAIRE.md) v1.0.0 |

> **Start here.** This is the smallest example in the repository: two functions, two
> parameters, three rules. It is meant to be read in five minutes, in full.
>
> It is small, and it still carries a real false friend — one that looks harmless and gets
> the answer wrong every time.

---

## 1. Purpose and context

A journey is made of legs. Each leg has a distance and a speed. We want the **average speed
over the whole journey**.

The obvious answer is wrong. Averaging the leg speeds gives a number that no vehicle ever
travelled at, and it is always too optimistic: **you spend more time on the slow legs, so
they weigh more.**

## 2. Scope

**In scope.** Total distance, total duration, and average speed of one journey.

**Out of scope.** Stops, traffic, speed limits, fuel, and any journey planning.

## 3. Functions

| Id | Function | Role |
|---|---|---|
| <a id="fn-001"></a>`FN-001` | **Leg duration** | Time spent on one leg, from its distance and speed |
| <a id="fn-002"></a>`FN-002` | **Journey average speed** | Total distance divided by total duration |

---

## 4. Inputs

```
journey :
    legs : Sequence[Leg](1 .. 200)

Leg :
    distance : Length(km, > 0, 3 decimals)   [Scaled]
    speed    : Speed(km/h, > 0, 1 decimal)   [Scaled]
```

**Precondition:** every `speed` is strictly positive (`E-SPD-001`).

## 5. Outputs

```
result :
    total_distance : Length(km, > 0, 3 decimals)     [Scaled]
    total_duration : Duration(h, > 0, 6 decimals)    [Real]
    average_speed  : Speed(km/h, > 0, P-02 decimals) [Real]
```

## 6. Parameters

| Id | Name | Value | Who may change it | Effective date |
|---|---|---|---|---|
| <a id="p-01"></a>`P-01` | Rounding mode of the published speed | `HALF_EVEN` | Fleet operations manager | 2026-01-01 |
| <a id="p-02"></a>`P-02` | Decimals of the published speed | 1 | Fleet operations manager | 2026-01-01 |

---

## 7. Rules

### 7.1 The algorithm, in one piece

```
DEFINE compute_average_speed(journey) : result

    PRECONDITIONS  E-SPD-001

    leg_duration   = ...                (RG-010, per leg)
    total_distance = ...                (RG-020)
    total_duration = ...                (RG-020)
    average_speed  = ...                (RG-030)

    RETURN result
```

### RG-010 — Duration of one leg

For each leg:

```
leg_duration = distance ÷ speed
```

Leg durations are **not rounded**. Only the published speed is (`RG-030`).

> **Why it matters.** Rounding each duration to the minute and then summing would shift the
> result on a long journey. Where we round is a business decision, and here it is: **once,
> at the end**.

### RG-020 — Journey totals

```
total_distance = SUM OF distance OVER legs
total_duration = SUM OF leg_duration OVER legs
```

### RG-030 — Average speed

```
average_speed = ROUND( total_distance ÷ total_duration, P-02, P-01 )
```

> **This is the false friend, and it is the whole point of this example.** The average
> speed is **not** the average of the leg speeds.
>
> 60 km at 30 km/h then 60 km at 60 km/h: the averaged speeds give 45 km/h, the real answer
> is **40 km/h**. You spend two hours on the first leg and one on the second — the slow leg
> weighs twice as much.
>
> The two answers coincide only when all legs take the **same time**, which almost never
> happens. A developer left to guess would write the arithmetic mean: it is the natural
> reading of "average speed", and it is wrong.

### Coverage table

| Rule | Covered by |
|---|---|
| `RG-010` | CT-01, CT-02, CT-03, CT-04 |
| `RG-020` | CT-01, CT-02, CT-03, CT-04 |
| `RG-030` | CT-01, CT-02, CT-03, CT-04 |

---

## 8. Invariants

| Id | Statement |
|---|---|
| <a id="inv-01"></a>`INV-01` | `average_speed` lies between the **slowest** and the **fastest** leg speed. A result outside that range means the arithmetic is wrong. |
| <a id="inv-02"></a>`INV-02` | Before rounding, `average_speed × total_duration = total_distance`. |

> `INV-01` is the cheap check that catches the mistake this specification exists to
> prevent — and it is tested on generated journeys, not only on the four cases below.

## 9. Business error cases

| Id | Condition | Behaviour |
|---|---|---|
| <a id="e-spd-001"></a>`E-SPD-001` | A leg has a speed `≤ 0` | Reject. A null speed means an infinite duration: there is no average to publish |

---

## 10. Test set

### CT-01 — Equal distances, different speeds

| Leg | Distance | Speed | Duration |
|---|---|---|---|
| 1 | 60 km | 30.0 km/h | 2.000000 h |
| 2 | 60 km | 60.0 km/h | 1.000000 h |

120 km ÷ 3 h → **`average_speed` = 40.0 km/h**

**The averaged speeds would give 45.0.** This case alone separates a correct
implementation from the natural mistake.

### CT-02 — Equal durations

| Leg | Distance | Speed | Duration |
|---|---|---|---|
| 1 | 30 km | 30.0 km/h | 1.000000 h |
| 2 | 60 km | 60.0 km/h | 1.000000 h |

90 km ÷ 2 h → **`average_speed` = 45.0 km/h**

Here the two methods **agree**. The case is kept on purpose: it shows that a wrong
implementation can pass a plausible test, and that CT-01 is the one doing the work.

### CT-03 — A single leg

100 km at 80.0 km/h → 1.250000 h → **`average_speed` = 80.0 km/h**

### CT-04 — Rounding

| Leg | Distance | Speed | Duration |
|---|---|---|---|
| 1 | 10 km | 7.0 km/h | 1.428571 h |
| 2 | 10 km | 13.0 km/h | 0.769231 h |

20 km ÷ 2.197802 h = 9.10000… → **`average_speed` = 9.1 km/h**

### CT-05 — Null speed

One leg at 0.0 km/h → rejected by `E-SPD-001`.

### Provenance and validation

| | |
|---|---|
| **Provenance** | Computed by hand in exact decimal arithmetic, independently of any implementation |
| **How they were examined** | Each duration, total and quotient was recomputed, and each case was compared with the arithmetic mean of the speeds to check whether it discriminates |
| **What the examination produced** | CT-02 was added **because** it does *not* discriminate: without it, nobody would see that passing three cases proves nothing |
| **Where they live** | [`../code/src/test/resources/reference-data.csv`](../code/src/test/resources/reference-data.csv) |
| **Approved by** | Fleet operations manager, 2026-08-22 |

---

## 11. Constraints and requirements

| Id | Statement | Source | Owner | Verification |
|---|---|---|---|---|
| `EX-01` | Distances and speeds are exact as entered; only the published speed is rounded | Contract §4, §5 | IT architecture | Design review |
| `EX-02` | A journey has at most 200 legs; the calculation is called at most 50 times per second | Operations measurement | Operations manager | Load test |

## 12. Open questions

| Id | Question | Decider | Due | Status |
|---|---|---|---|---|
| `Q-01` | Should stops be counted in the duration? Today they are out of scope, so the published speed is a *moving* average | Fleet operations manager | 2026-12-01 | Open |

## 13. History and change notices

| Version | Date | Change | Impact on results | Notice |
|---|---|---|---|---|
| 1.0.0 | 2026-08-22 | Initial version | — | — |

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../../../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `SPEC-SPD-001` | `e629651e-8b0d-416e-b97a-fd9cd4ceea3a` | document | SPEC-SPD-001 — Average speed of a journey |
| `RG-010` | `d609f740-a23b-4a73-a66e-59a20ebc5702` | règle | Duration of one leg |
| `RG-020` | `6aa5716c-8d3a-4fc4-acba-387eba4214a1` | règle | Journey totals |
| `RG-030` | `a8bf6497-965d-4777-a9f9-f3d2a35cec8c` | règle | Average speed |
| `CT-01` | `5b9cfac9-fffd-46d7-bd52-3e018d83b220` | cas de test | Equal distances, different speeds |
| `CT-02` | `af954a1c-c6b7-44f7-b1a5-1e2577390e5a` | cas de test | Equal durations |
| `CT-03` | `55b3128b-974b-49b9-bcf2-1153ad2e878c` | cas de test | A single leg |
| `CT-04` | `453d1761-dec3-4e0b-bdb4-23d908e52130` | cas de test | Rounding |
| `CT-05` | `8ae25c37-0e16-4bdd-8baa-106e65af357e` | cas de test | Null speed |
| `FN-001` | `aa32e541-ddbd-4bd1-b0cc-4bf61cc2d00b` | fonction | Leg duration |
| `FN-002` | `6e9e8cc6-857f-497c-8fa7-6e034a83fb04` | fonction | Journey average speed |
| `P-01` | `62f12714-40af-41b4-90fa-79c93e152c6e` | paramètre | Rounding mode of the published speed |
| `P-02` | `b4ccd8a2-23b2-42d7-aa15-6dd5e58e3829` | paramètre | Decimals of the published speed |
| `EX-01` | `2598ec0f-16ad-4bb3-a0df-0cfba7ea5648` | exigence | Distances and speeds are exact as entered; only the published speed is |
| `EX-02` | `990e7004-55a7-42c4-83c6-ad45a25ea78b` | exigence | A journey has at most 200 legs; the calculation is called at most 50 t |
| `INV-01` | `0f0e6185-bb00-480a-b3f3-03efffee34a5` | invariant | `average_speed` lies between the **slowest** and the **fastest** leg s |
| `INV-02` | `18950bf4-58d6-4a2b-9426-20cec1f6edba` | invariant | Before rounding, `average_speed × total_duration = total_distance`. |
| `E-SPD-001` | `30c1c2f2-56d3-49e1-bf5d-869e1dff4936` | cas d'erreur | A leg has a speed `≤ 0` |
| `Q-01` | `b7efbab4-d912-4803-9869-ef1c89743706` | question | Should stops be counted in the duration? Today they are out of scope,  |
