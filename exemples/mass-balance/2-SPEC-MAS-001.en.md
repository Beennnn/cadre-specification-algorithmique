# SPEC-MAS-001 — Batch mass balance

| | |
|---|---|
| **Identifier** | SPEC-MAS-001 |
| **Version** | 1.0.0 |
| **Status** | Approved |
| **Business approver** | Formulation manager |
| **Technical co-author** | Process software team |
| **Reference glossary** | [GLOSSAIRE.md](../../GLOSSAIRE.md) v1.0.0 |

> **Why this example.** It is the counterpart of
> [SPEC-THM-001](../SPEC-THM-001-refroidissement.md). There, **continuous** quantities, a
> differential equation, and an **approximate** result whose accuracy is open to
> discussion. Here, **discrete and exact** quantities: a weighed mass exists at the balance
> step, and nowhere in between. The result is not approximate, it is **right or wrong** —
> and mass conservation makes that a property we can check, not a convention.
>
> The false friends are not the same ones, and that is why both examples are kept.

---

## 1. Purpose and context

A batch is prepared by dosing several components according to **target mass fractions**.
Each component is weighed on a balance whose **resolution** is finite: we cannot put
0.333333 kg on it, only a multiple of the balance step.

Rounding each dose on its own means that **their sum does not land on the batch mass**. The
gap is tiny — one balance step — but it is real: the prepared batch would not have the mass
we announce, and the fractions would no longer add up to 1.

The function computes, for each component, the **mass to weigh**, such that their sum is
**exactly** the target batch mass.

---

## 2. Scope

**In scope.** Computing the masses to weigh from the target fractions, rounding them to the
balance step, and allocating the residual.

**Out of scope.** Driving the balance, the physical order of weighings, raw material batch
tracking, quality control of the resulting mix.

---

## 3. Glossary

| Term | Definition |
|---|---|
| **Batch** | The quantity of product prepared in one go, with a declared target mass. |
| **Component** | One of the materials that go into the batch, identified and dosed. |
| **Target mass fraction** | The share of the batch mass that a component must represent. Dimensionless, between 0 and 1. The fractions add up to exactly 1. |
| **Balance step** | The smallest mass difference the balance can tell apart. Every mass to weigh is an integer multiple of it. |
| **Residual** | The gap between the target batch mass and the sum of the rounded masses. It is at most a few balance steps, and it is never ignored. |

---

## 4. Inputs

```
request :
    target_batch_mass : Mass(kg, > 0, 3 decimals)          [Scaled]
    balance_step      : Mass(kg, > 0, 3 decimals)          [Scaled]
    components        : Sequence[Component](1 .. 50)

Component :
    component_id         : Identifier(text, 3 to 20 characters, unique)   [CharacterString]
    target_mass_fraction : Ratio(dimensionless, 6 decimals, 0.000000 .. 1.000000)  [Scaled]
```

> **`[Scaled]`, not `Real`.** The family in brackets is the ISO/IEC 11404 one
> ([CADRE §2.3](../../CADRE.md)). `Scaled` means a rational with a fixed decimal scale —
> **exact**; `Real` means an approximation. `INV-01` requires strict equality: it does not
> hold in binary floating point, where 0.1 cannot be represented. The type carries the
> requirement — it does not merely recommend it.

**Preconditions:**
- `SUM OF target_mass_fraction OVER components = 1.000000`, exactly (`E-MAS-001`).
- No two components ever carry the same `component_id` (`E-MAS-003`).
- `target_batch_mass` is an integer multiple of `balance_step` (`E-MAS-004`).

---

## 5. Outputs

```
result :
    dispensed : Sequence[Dispensed]
    residual  : Mass(kg, 3 decimals)   [Scaled]   -- signed, before allocation

Dispensed :
    component_id   : Identifier(text, 3 to 20 characters)   [CharacterString]
    nominal_mass   : Mass(kg, ≥ 0, 9 decimals)   [Scaled]   -- before rounding, for traceability
    dispensed_mass : Mass(kg, ≥ 0, 3 decimals)   [Scaled]   -- what is actually weighed
```

---

## 6. Parameters

| Id | Name | Value | Unit | Who may change it | Effective date |
|---|---|---|---|---|---|
| `P-01` | Rounding mode for doses | `HALF_EVEN` | — | Formulation manager | 2026-01-01 |
| `P-02` | Maximum tolerated residual, in balance steps | 3 | steps | Formulation manager | 2026-01-01 |

> **`P-01` is `HALF_EVEN`, and this is not a detail.** Over a large number of batches,
> rounding "half up" pushes masses upwards every single time. `HALF_EVEN` splits the tie
> cases and cancels that drift. It is a business decision: it changes results.

---

## 7. Rules

### 7.1 The algorithm, in one piece

**The rules below are not independent fragments: they are the steps of one algorithm.**
Here is how they compose, as the business states it. This is what settles the **order**,
and the developer has nothing to invent:

```
DEFINE compute_mass_balance(request) : result

    PRECONDITIONS  E-MAS-001, E-MAS-003, E-MAS-004

    FOR EACH component IN request.components
        nominal_mass = ...                          (RG-010)
        rounded_mass = ...                          (RG-020)
    END FOR

    LET residual = ...                              (RG-030)

    IF the residual exceeds the bound THEN          (RG-040)
        RAISE ERROR E-MAS-002
    ELSE
        dispensed_mass = ...                        (RG-050)
    END IF

    RETURN result
```

> **Why both views, and not just this one.** The integrated algorithm is read in one go and
> judged in one go: it is what lets a technical reviewer answer "yes, I can code this". The
> numbered rules are the **addressable units** of the document — a test case (§10), a change
> notice (§13), a developer suggestion, a comment in the code all attach to one of them. A
> single eighty-line block cannot be cited, cannot be covered rule by rule, and cannot be
> versioned in parts.
>
> So these are not two options to choose between: it is **the same algorithm at two levels
> of detail**, and both are required.

### 7.2 Processing chain

The same sequence, seen as boxes passing quantities to each other. This table is what makes
the order **checkable by a tool**: `C-35` reports a step that consumes a quantity nothing
produced upstream, `C-36` a quantity produced that nobody uses.

| Step | Consumes | Produces | Rules |
|---|---|---|---|
| `ET-01` Nominal mass | `target_batch_mass`, `target_mass_fraction` | `nominal_mass` | `RG-010` |
| `ET-02` Rounding to the step | `nominal_mass`, `balance_step`, `P-01` | `rounded_mass` | `RG-020` |
| `ET-03` Residual | `rounded_mass`, `target_batch_mass` | `residual` | `RG-030` |
| `ET-04` Acceptability | `residual`, `balance_step`, `P-02` | `residual_is_acceptable` | `RG-040` |
| `ET-05` Allocation | `rounded_mass`, `residual`, `residual_is_acceptable`, `target_mass_fraction`, `component_id` | `dispensed_mass` | `RG-050` |

```bash
java outils/Verifier.java --chaine exemples/mass-balance/2-SPEC-MAS-001.en.md
```

From this table the tool derives **who creates and who uses** each quantity, the
**execution levels** — what can run in parallel — and the **critical path**. None of these
views is written by hand: they follow from the table, so they cannot drift away from the
document.

> **Here the chain is strictly sequential**: each step consumes what the previous one
> produces, so there is nothing to parallelise. That is a result, not an omission — and it
> is useful for the developer to know it before trying.

### RG-010 — Nominal mass of a component

For each component:

```
nominal_mass = target_batch_mass × target_mass_fraction
```

The nominal mass is kept in the output, **not rounded**: it is what allows the calculation
to be replayed and a deviation to be explained.

### RG-020 — Rounding to the balance step

For each component, we round a **number of steps**, then come back to a mass:

```
LET steps    = ROUND( nominal_mass ÷ balance_step, 0, P-01 )
rounded_mass = steps × balance_step
```

> **We round a number of steps, not a mass.** Writing `ROUND(nominal_mass, 3, P-01)` would
> only be right if the step were exactly 0.001 kg. Balances with a 5 g step exist: the dose
> must then be a multiple of 0.005 kg, which rounding to three decimals does not give.

### RG-030 — Residual

```
LET dispensed_total = SUM OF rounded_mass OVER components
residual = target_batch_mass − dispensed_total
```

The residual is **signed**: positive if material is missing, negative if there is too much.

### RG-040 — Acceptability of the residual

```
IF |residual| > P-02 × balance_step THEN
    RAISE ERROR E-MAS-002 "residual exceeds the tolerated number of balance steps"
ELSE
    the residual is allocated by RG-050
END IF
```

> **Why a bound.** A residual of a few steps is the normal consequence of rounding. A larger
> one means something else — inconsistent fractions, a balance step that does not fit the
> batch — and absorbing it silently would hide the problem.

### RG-050 — Allocation of the residual

```
LET largest = MAXIMUM OF target_mass_fraction OVER components
LET eligible = FILTER components WHERE target_mass_fraction = largest
LET receiver = THE FIRST OF ( SORT eligible BY component_id ASCENDING )
```

The receiver takes the residual; every other component keeps its rounded mass:

```
IF component_id = receiver.component_id THEN
    dispensed_mass = rounded_mass + residual
ELSE
    dispensed_mass = rounded_mass
END IF
```

> **Two business decisions, and one alone would not be enough.**
>
> **Who gets it.** The residual goes to the component with the largest fraction, because
> that is where it weighs least in relative terms: one balance step on 0.667 kg shifts the
> fraction by 0.15 %, on 0.167 kg by 0.6 %.
>
> **What if there is a tie.** Two components can carry the same largest fraction. Without a
> rule, the result would depend on the order in which the data is read — so on the
> implementation, so it would not be reproducible. We take the smallest `component_id` in
> alphabetical order: arbitrary, but **declared and stable**.

### Coverage table

| Rule | Covered by |
|---|---|
| `RG-010` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-020` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-030` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-040` | CT-05 (bound reached, accepted), CT-06 to CT-08 (preconditions) |
| `RG-050` | CT-01, CT-02, CT-03, CT-05 |

---

## 8. Invariants

| Id | Statement |
|---|---|
| `INV-01` | `SUM OF dispensed_mass OVER dispensed = target_batch_mass`, **exactly**. This is mass conservation: it tolerates no gap, not even one step. |
| `INV-02` | Every `dispensed_mass` is an integer multiple of `balance_step`. |
| `INV-03` | Every `dispensed_mass` is `≥ 0`. A negative residual cannot make a dose negative — if that happened, `RG-040` should have rejected first. |
| `INV-04` | The result is **invariant under permutation** of the component list: reordering the input changes no weighed mass. This is what the tie-break rule of `RG-050` guarantees. |

> `INV-04` is tested on generated inputs: we shuffle the list at random and check that the
> result is identical. It is the check that catches an implementation which "forgot" the
> tie-break — one that works on every nominal case and gets it wrong the day the components
> are entered in a different order.

---

## 9. Business error cases

| Id | Condition | Behaviour |
|---|---|---|
| `E-MAS-001` | The target fractions do not add up to exactly 1 | Reject. No partial result is produced |
| `E-MAS-002` | The residual exceeds `P-02` balance steps | Reject, reporting the residual found |
| `E-MAS-003` | Two components carry the same `component_id` | Reject: the tie-break of `RG-050` would not be defined |
| `E-MAS-004` | `target_batch_mass` is not a multiple of `balance_step` | Reject: the exact conservation of `INV-01` would be impossible |

---

## 10. Test set

All cases use `balance_step = 0.001 kg` and `P-01 = HALF_EVEN`, unless stated otherwise.

### CT-01 — Positive residual, single largest fraction

| Component | Target fraction | Nominal mass | Rounded | **Weighed** |
|---|---|---|---|---|
| `CMP-A` | 0.333333 | 0.333333000 | 0.333 | **0.333** |
| `CMP-B` | 0.333333 | 0.333333000 | 0.333 | **0.333** |
| `CMP-C` | 0.333334 | 0.333334000 | 0.333 | **0.334** |

`target_batch_mass = 1.000` · sum of rounded `= 0.999` · `residual = +0.001`
→ goes to `CMP-C`, the largest fraction. Final sum **= 1.000**.

### CT-02 — Negative residual

| Component | Target fraction | Nominal mass | Rounded | **Weighed** |
|---|---|---|---|---|
| `CMP-A` | 0.166667 | 0.166667000 | 0.167 | **0.167** |
| `CMP-B` | 0.166667 | 0.166667000 | 0.167 | **0.167** |
| `CMP-C` | 0.666666 | 0.666666000 | 0.667 | **0.666** |

`target_batch_mass = 1.000` · sum of rounded `= 1.001` · `residual = −0.001`
→ taken from `CMP-C`. Final sum **= 1.000**.

### CT-03 — Tie on the largest fraction

| Component | Target fraction | Nominal mass | Rounded | **Weighed** |
|---|---|---|---|---|
| `CMP-A` | 0.400000 | 0.400400000 | 0.400 | **0.401** |
| `CMP-B` | 0.400000 | 0.400400000 | 0.400 | **0.400** |
| `CMP-C` | 0.200000 | 0.200200000 | 0.200 | **0.200** |

`target_batch_mass = 1.001` · sum of rounded `= 1.000` · `residual = +0.001`
→ `CMP-A` and `CMP-B` are tied; the tie-break takes `CMP-A`, the smallest identifier.
Final sum **= 1.001**.

**This is the discriminating case.** An implementation without a tie-break rule gives a
result here that depends on the reading order — and it still passes CT-01, CT-02 and CT-04.

### CT-04 — Zero residual

| Component | Target fraction | Nominal mass | Rounded | **Weighed** |
|---|---|---|---|---|
| `CMP-A` | 0.500000 | 0.500000000 | 0.500 | **0.500** |
| `CMP-B` | 0.250000 | 0.250000000 | 0.250 | **0.250** |
| `CMP-C` | 0.250000 | 0.250000000 | 0.250 | **0.250** |

`residual = 0.000`: `RG-050` changes no dose. Sum **= 1.000**.

### CT-05 — Residual exactly at the bound

`balance_step = 0.100`, `target_batch_mass = 1.000`, seven components with fractions
`0.142857` × 6 and `0.142858`.

Each nominal dose is ≈ 0.1429 kg, rounded to 0.100 kg — sum 0.700 kg, **residual 0.300 kg,
which is exactly 3 steps**. That equals `P-02`, so it is **accepted**, because `RG-040`
bounds with `>` and not `≥`. The residual goes to `CMP-G`, the largest fraction, which
receives **0.400 kg**. Final sum **= 1.000**.

**This is the edge case of the bound.** With `P-02 = 2`, the same input would be rejected by
`E-MAS-002`. An implementation writing `≥` instead of `>` would fail here, and only here.

### CT-06 — Fractions not adding up to 1

Three components with fraction `0.333333` — sum `0.999999`. Rejected by `E-MAS-001`,
**before any calculation**. No partial result is produced.

### CT-07 — Duplicate identifiers

Two components both named `CMP-A`, fraction `0.500000` each. Rejected by `E-MAS-003`: the
tie-break of `RG-050` would not be defined.

### CT-08 — Target mass not a multiple of the step

`target_batch_mass = 1.000`, `balance_step = 0.300`. Rejected by `E-MAS-004`: the exact
conservation of `INV-01` would be impossible.

### Provenance and validation

| | |
|---|---|
| **Provenance** | Computed in exact decimal arithmetic, independently of any implementation of the component |
| **How they were examined** | Every line was recomputed: nominal mass, number of steps, `HALF_EVEN` rounding, residual, allocation. The final sum was checked against `INV-01` for each case |
| **What the examination produced** | CT-03 was built **in order to** expose the tie between fractions: without it, the tie-break rule of `RG-050` was covered by no case. CT-05 was built for the residual bound, which no other case reached |
| **Where they live** | [`4-code/reference-data.csv`](4-code/reference-data.csv), replayed on every run of the qualification harness |
| **Approved by** | Formulation manager, 2026-08-21 |

---

## 11. Constraints and requirements

| Id | Statement | Source | Owner | Verification |
|---|---|---|---|---|
| `EX-01` | The calculation uses **exact decimal arithmetic**, as required by the `Scaled` family in the contract (ISO/IEC 11404). Binary floating point is excluded: 0.1 cannot be represented in it, and `INV-01` requires strict equality | Contract §4, §5 | IT architecture | Design review |
| `EX-02` | A batch has at most 50 components; the calculation is called at most 200 times per hour | Plant capacity | Production manager | Measurement in operation |
| `EX-03` | The calculation is **replayable identically**: same inputs, same masses, indefinitely. No dependency on the clock, on reading order, or on randomness | Traceability requirement `QUA-TRC-2` | Quality assurance | Replay against archive, quarterly |
| `EX-04` | Nominal masses are kept for 10 years with the batch | `QUA-TRC-2` §4 | Quality assurance | Archive audit |

---

## 12. Open questions

| Id | Question | Decider | Due | Status |
|---|---|---|---|---|
| `Q-01` | Should a residual of several steps be spread over several components, instead of going entirely to the largest one? | Formulation manager | 2026-10-01 | Open |
| `Q-02` | Should the alphabetical tie-break give way to a priority order declared per component? | Formulation manager | | Closed |

---

## 13. History and change notices

| Version | Date | Change | Impact on results | Notice |
|---|---|---|---|---|
| 1.0.0 | 2026-08-21 | Initial version | — | — |

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `RG-010` | `1f7dd670-3f98-4ea6-9bd1-04e2f61ad316` | règle | Nominal mass of a component |
| `RG-020` | `2c2489c2-702a-46dd-a7cd-1064022ddbd4` | règle | Rounding to the balance step |
| `RG-030` | `ed9131d6-4472-4d21-b75d-fba1122da558` | règle | Residual |
| `RG-040` | `8080dc01-878b-4657-909b-b2199ecf4b60` | règle | Acceptability of the residual |
| `RG-050` | `c55d71da-977b-4b31-93a3-66c3370a00a1` | règle | Allocation of the residual |
| `CT-01` | `b3e154c8-b776-4ecb-86c7-fe1cc06173a9` | cas de test | Positive residual, single largest fraction |
| `CT-02` | `98d1b468-0e1f-4082-9f1a-43a47d0f1802` | cas de test | Negative residual |
| `CT-03` | `b660aa4f-dd37-4cd9-b967-798a11586fd2` | cas de test | Tie on the largest fraction |
| `CT-04` | `dfb6ff92-8efb-47e8-96c8-ca15b1704d70` | cas de test | Zero residual |
| `CT-05` | `bdc923c8-9721-47cf-8b54-b77407f04bb1` | cas de test | Residual exactly at the bound |
| `CT-06` | `0bd9840e-b26b-4f5c-8fb1-942cf27adb3c` | cas de test | Fractions not adding up to 1 |
| `CT-07` | `9ed8d49a-3e8a-4e33-9dae-78541e2c80e1` | cas de test | Duplicate identifiers |
| `CT-08` | `c971327c-6b5f-4696-815e-ce1ee4479bb7` | cas de test | Target mass not a multiple of the step |
| `P-01` | `4b92babc-dd3d-423e-8feb-046b7166db4e` | paramètre | Rounding mode for doses |
| `P-02` | `cd6b8a1b-c171-4083-aa90-16fbfbffba9b` | paramètre | Maximum tolerated residual, in balance steps |
| `EX-01` | `289af665-16cb-4113-86ea-9ff53ce1a4c5` | exigence | The calculation uses **exact decimal arithmetic**, as required by the  |
| `EX-02` | `525363e7-8bba-49e7-89e3-f0761903866b` | exigence | A batch has at most 50 components; the calculation is called at most 2 |
| `EX-03` | `f265a3a3-f970-41c9-81ca-f27bb0b129cb` | exigence | The calculation is **replayable identically**: same inputs, same masse |
| `EX-04` | `2c76616f-cdf7-439f-88a6-107cd43b3d24` | exigence | Nominal masses are kept for 10 years with the batch |
| `INV-01` | `dbccfecf-777b-4959-a321-7cb6555461cf` | invariant | `SUM OF dispensed_mass OVER dispensed = target_batch_mass`, **exactly* |
| `INV-02` | `60bffc03-1312-4d2a-94b9-152a5f0f18e6` | invariant | Every `dispensed_mass` is an integer multiple of `balance_step`. |
| `INV-03` | `14aa1bd5-f615-42c4-a9cb-3d2c078c1e0b` | invariant | Every `dispensed_mass` is `≥ 0`. A negative residual cannot make a dos |
| `INV-04` | `9eb759d5-63d4-4b94-8f80-dd07b8d47340` | invariant | The result is **invariant under permutation** of the component list: r |
| `E-MAS-001` | `694d1152-ae43-44ea-9cee-1a9226214548` | cas d'erreur | The target fractions do not add up to exactly 1 |
| `E-MAS-002` | `1ae84506-b8e5-48fc-b902-b166a80b4b30` | cas d'erreur | The residual exceeds `P-02` balance steps |
| `E-MAS-003` | `c1c93cb0-ddf8-40e4-820d-e702b518f1fe` | cas d'erreur | Two components carry the same `component_id` |
| `E-MAS-004` | `f12f64b6-8041-4729-91ec-92cdee0b1e71` | cas d'erreur | `target_batch_mass` is not a multiple of `balance_step` |
| `Q-01` | `9111352a-45f3-4b93-8115-3102c370b556` | question | Should a residual of several steps be spread over several components,  |
| `Q-02` | `c16913b4-c54a-41ba-861a-7c369e06a149` | question | Should the alphabetical tie-break give way to a priority order declare |

### Identités retirées

*Un objet supprimé conserve son UUID : il n'est jamais réattribué, pour qu'une référence ancienne reste résoluble.*

| Identifiant | UUID |
|---|---|
| `SPEC-MAS-001` | `292e2c59-db4e-4dcd-b7e1-101cf6d76de0` |
