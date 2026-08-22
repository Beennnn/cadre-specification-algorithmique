# SPEC-THM-001 — Cooling forecast for a hot drink

| | |
|---|---|
| **Identifier** | SPEC-THM-001 |
| **Version** | 1.0.1 |
| **Status** | Accepted |
| **Business author** | *(role: Physical models manager)* |
| **Business approver** | *(role: Product management)* |
| **Technical co-author** | *(role: Application architect)* |
| **Effective date** | 2026-01-01 |
| **Reference glossary** | internal — §3 of this document |

> **Reading note.** A teaching example of the framework described in
> [CADRE.md](../../../CADRE.md), applied to a **physical model**. The phenomenon — a hot
> drink cooling down — is familiar to everyone and is computed with a law everyone has met
> at school. That is exactly what makes it a good yardstick: **no domain knowledge can
> paper over an ambiguity of wording.**
>
> This example illustrates the three adaptations of the framework to scientific computing
> (CADRE.md §2.8): **specify a model and a tolerance, not a solving method**, **distinguish
> reproducibility, numerical accuracy and model validity**, and **treat convergence as a
> business decision**.

---

## 1. Purpose and context

From the initial state of a hot drink and of its environment, forecast:

1. its **temperature at a given instant**;
2. the **instant at which it reaches a target temperature** ("drinkable").

The calculation takes into account a possible **addition of a cold liquid** (milk, water)
at a declared instant.

**Two uses, one specification** — and that is the interest of the case:

- **Embedded**: in a consumer application, run on the phone, **with no network**, several
  times a second while the user drags a slider.
- **Laboratory**: in a teaching tool, where the model is compared against real measurements
  and the ambient temperature is allowed to vary over time.

These two uses have very different constraints (§11) and **will lead to two different
implementations of the same specification**. That is the demonstration that the
specification really describes *what is computed*, not *how*.

## 2. Scope

**In scope:**
- The cooling model and its assumptions.
- The mixing of two liquids at different temperatures.
- The instant a target temperature is reached, and the conditions for its existence.
- The cases where the target is unreachable, or not reached within the horizon.

**Out of scope:**
- The **estimation** of the cooling coefficient from measurements (`SPEC-THM-002`). It is
  an input here.
- Thermal radiation and evaporation: the model retained does not represent them explicitly
  (`RG-010`, assumptions). See `Q-01`.
- Internal gradients: the drink is assumed to be at a homogeneous temperature.
- Any recommendation made to the user ("wait 4 minutes") — that is display, not
  calculation.

## 3. Glossary

| Term | Definition |
|---|---|
| **Beverage** | A mass of liquid at a homogeneous temperature, in a given container. |
| **Ambient temperature** | The temperature of the air around the container, assumed constant over the whole forecast. |
| **Cooling coefficient** | The parameter `k` of the model (`RG-010`). It characterises the container-plus-environment pair, not the liquid. |
| **Time constant** | `1 ÷ k`. The time after which the gap to the ambient temperature has been divided by `e` (≈ 2.718). |
| **Specific heat capacity** | The energy needed to raise one kilogram of the liquid by one degree. |
| **Addition** | The introduction of a second liquid, at a declared instant. |
| **Target temperature** | The temperature the drink is asked to reach. |
| **Horizon** | The longest duration over which the forecast means anything. Beyond it, we do not answer. |

## 4. Inputs

```
request :
    beverage :
        mass                    : Mass(kg, > 0, 4 decimals)
        initial_temperature     : Temperature(°C, 2 decimals)
        specific_heat_capacity  : HeatCapacity(kJ·kg⁻¹·K⁻¹, > 0, 3 decimals)
    ambient_temperature         : Temperature(°C, 2 decimals)
    cooling_coefficient         : Rate(min⁻¹, > 0, 5 decimals)
    addition                    : Addition — optional
    requested_instant           : Duration(min, ≥ 0, 4 decimals) — optional
    target_temperature          : Temperature(°C, 2 decimals) — optional

Addition :
    mass                    : Mass(kg, ≥ 0, 4 decimals)
    temperature             : Temperature(°C, 2 decimals)
    specific_heat_capacity  : HeatCapacity(kJ·kg⁻¹·K⁻¹, > 0, 3 decimals)
    instant                 : Duration(min, ≥ 0, 4 decimals)
```

**Preconditions:**
- At least one of `requested_instant` and `target_temperature` is supplied.
- `cooling_coefficient` is strictly positive.
- Masses and specific heat capacities are strictly positive, except the mass of the
  addition, which may be zero (`CT-07`).

> **A note on the temperature unit.** Temperatures are expressed in degrees Celsius. The
> model of `RG-010` involves only **differences** of temperature: it is therefore invariant
> under a change of origin of the scale (`INV-04`), and a calculation carried out in kelvin
> gives exactly the same instant (`CT-06`). This property is written down **because it
> would stop being true** if a radiation term were added, which depends on the absolute
> temperature (`Q-01`). It is the kind of consequence a specification has to anticipate.

## 5. Outputs

```
result :
    temperature_at_instant     : Temperature(°C) — if requested_instant is supplied
    target_reached_at          : Duration(min)   — if target_temperature is supplied and reached
    target_already_reached     : Boolean         — true if reached at instant 0
    target_unreachable         : Boolean         — true if the target is out of domain (RG-050)
    target_beyond_horizon      : Boolean         — true if reached beyond P-04
    temperature_after_addition : Temperature(°C) — if an addition took place
    iterations_used            : Integer(≥ 0)    — 0 if solved exactly
```

> The three indicators `target_already_reached`, `target_unreachable` and
> `target_beyond_horizon` are not redundant: they describe **three physically distinct
> situations** that a missing value alone would merge. A scientific result you cannot say
> *why* is absent is not usable.

## 6. Parameters

| Id | Name | Value | Unit | Who may change it | Approval route | Observed frequency | Effective date |
|---|---|---|---|---|---|---|---|
| <a id="p-01"></a>`P-01` | Cooling coefficient — open ceramic cup | 0.03000 | min⁻¹ | Models manager | Product management approval | 1 to 2 times a year | 2026-01-01 |
| <a id="p-02"></a>`P-02` | Cooling coefficient — closed insulated mug | 0.00600 | min⁻¹ | Models manager | Product management approval | 1 to 2 times a year | 2026-01-01 |
| <a id="p-03"></a>`P-03` | Default drinking target temperature | 55.00 | °C | Product management | Management approval | once a year | 2026-01-01 |
| <a id="p-04"></a>`P-04` | Forecast horizon | 120.0 | min | Product management | Management approval | rare | 2026-01-01 |
| <a id="p-05"></a>`P-05` | Convergence tolerance on the instant | 0.01 | min | Models manager | Product management approval | rare | 2026-01-01 |
| <a id="p-06"></a>`P-06` | Maximum number of iterations | 100 | — | Models manager | Product management approval | rare | 2026-01-01 |
| <a id="p-07"></a>`P-07` | Minimum gap to the ambient temperature for the target to be deemed reachable | 0.10 | °C | Models manager | Product management approval | rare | 2026-01-01 |

> `P-01` and `P-02` are **measured values**, not physical constants: they depend on the
> container and the environment, and they will be re-estimated when the product range
> changes. They therefore cannot live in the code.
> Specific heat capacities, by contrast, are **inputs** and not parameters, because they
> depend on the liquid the user chooses.

## 7. Rules

### RG-010 — The cooling model

```
LET k          = cooling_coefficient
LET T_ambient  = ambient_temperature
LET T(0)       = beverage.initial_temperature

The temperature of the drink follows Newton's law of cooling:

    dT/dt = − k × ( T(t) − T_ambient )

T_ambient is constant over the whole forecast (assumption H-2).
```

**Assumptions the business takes responsibility for**, each of them false in absolute terms
and acceptable within the intended domain of use:

| # | Assumption | Consequence if violated |
|---|---|---|
| H-1 | The drink is at a homogeneous temperature | Underestimation of the surface temperature, hence of what is felt |
| H-2 | The ambient temperature is constant | Slow drift over long durations (see `Q-02`) |
| H-3 | The coefficient `k` depends neither on the temperature nor on the volume | The model slightly overestimates cooling at high temperatures, where radiation and evaporation dominate |
| H-4 | The mixing (`RG-030`) is instantaneous and lossless | Negligible for an addition lasting a few seconds |

> **The specification states the equation, not its solving method.** For constant `k` and
> `T_ambient`, this equation admits a closed-form solution; an implementation may use it,
> or integrate numerically. **Both are conforming** as long as they meet the tolerances of
> §8.2. That choice belongs to the developer — and §11 shows that the two uses of the
> product will not settle it the same way.
>
> Writing "integrate with fourth-order Runge-Kutta at a 1 s step" would be a mistake: it
> would freeze a method, forbid the exact solution, and become wrong the day `T_ambient`
> starts to vary.

### RG-020 — Temperature at a given instant

```
temperature_at_instant = the value at t = requested_instant of the solution of RG-010,
                         starting from the initial state and including any addition (RG-040)
```

If `requested_instant` exceeds `P-04`, the value is not produced and
`target_beyond_horizon` does not apply — that is the error `E-HORIZON-001`.

### RG-030 — Mixing two liquids

```
On an addition, the resulting temperature is the one that conserves the thermal
energy of the whole:

LET beverage_heat_capacity = beverage.mass × beverage.specific_heat_capacity
LET addition_heat_capacity = addition.mass × addition.specific_heat_capacity

    temperature_after_addition =
        ( beverage_heat_capacity × T(addition.instant)
          + addition_heat_capacity × addition.temperature )
        ÷ ( beverage_heat_capacity + addition_heat_capacity )

The mixing does not modify the beverage: it produces a new one.

    mixed_beverage.mass                   = beverage.mass + addition.mass
    mixed_beverage.specific_heat_capacity = ( beverage_heat_capacity + addition_heat_capacity )
                                            ÷ mixed_beverage.mass
    mixed_beverage.temperature            = temperature_after_addition
```

If `addition.mass = 0`, the formula leaves the drink unchanged: the case is **covered by
the general rule**, it does not need separate treatment. A specification that adds an
`IF mass = 0 THEN …` branch for a case already covered creates two paths where one is
enough — and one day they will diverge.

### RG-040 — Order of operations: when the addition happens

```
The instant of the addition is an INPUT. The specification imposes no default
convention: the caller must declare it.

The calculation runs in phases:
    1. cooling alone, from t = 0 to t = addition.instant                  (RG-010)
    2. mixing at t = addition.instant, producing temperature_after_addition  (RG-030)
    3. cooling of mixed_beverage, from t = addition.instant onwards       (RG-010)
```

> **This is the central false friend of this specification, and it is counter-intuitive.**
>
> Adding the milk **early** then waiting, or waiting then adding the milk **late**, does not
> give the same final temperature — although the amount of milk, its temperature and the
> total duration are identical. `CT-02` puts a number on it: at 15 minutes, the immediate
> addition gives **55.1833 °C** and the delayed one **54.5160 °C**. Adding the milk straight
> away therefore leaves the drink **warmer**.
>
> The explanation is one sentence: cooling is proportional to the gap to the ambient
> temperature, so a drink already cooled by the milk then loses its heat **more slowly**.
>
> No developer can guess this, and none should have to decide it. Without `RG-040`, two
> honest implementations would produce two different answers to "what will the temperature
> be in a quarter of an hour?", and the gap — two thirds of a degree — would be blamed on
> rounding.

### RG-050 — Instant the target temperature is reached

```
The instant reached is the SMALLEST t ≥ 0 such that T(t) ≤ target_temperature.

Conditions of existence, evaluated in this order:

  IF beverage.initial_temperature ≤ target_temperature THEN
     target_reached_at = 0.00 ; target_already_reached = true ; stop

  ELSE IF target_temperature ≤ ambient_temperature + P-07 THEN
     target_unreachable = true ; no instant is produced ; stop

  ELSE IF the instant reached is greater than P-04 THEN
     target_beyond_horizon = true ; no instant is produced

  ELSE
     target_reached_at is produced, to within P-05
  END IF
```

> **Why the margin `P-07` and not a comparison to the ambient temperature alone.** The
> temperature tends to the ambient one without ever reaching it. A target set at 20.001 °C
> for an ambient of 20.00 °C is *mathematically* reachable — after several hours — and
> *physically* meaningless, the model having no validity at that scale. The margin turns an
> asymptote into an explicit decision. It is set by the business, not by the developer.

### RG-060 — Convergence

If the implementation solves `RG-050` by an iterative method:

```
The calculation stops when the bracket on the instant is smaller than P-05,
or after P-06 iterations.

IF P-06 iterations are reached without satisfying P-05 THEN
   RAISE ERROR E-CONV-001
   no instant is produced
ELSE
   target_reached_at is produced, and the number of iterations used is reported
END IF

The number of iterations actually used is reported.
An implementation that solves RG-050 exactly reports 0 iterations.
```

> **The three elements of an iterative calculation, and the one always forgotten.** A
> stopping criterion: almost always written down. A maximum number of iterations: often
> written down. **What is reported on non-convergence: almost never written down** — and
> that is the one that produces silently wrong results, when the program returns its last
> iterate as though it had converged. Here the business has decided: nothing is returned,
> it is reported. A business decision, not a technical one.

## 8. Invariants and numerical acceptance criteria

### 8.1 Invariants

| Id | Property |
|---|---|
| <a id="inv-01"></a>`INV-01` | If `T(0) > T_ambient`, then `T(t)` is strictly decreasing |
| <a id="inv-02"></a>`INV-02` | `T(t)` stays strictly between `T_ambient` and `T(0)` |
| <a id="inv-03"></a>`INV-03` | `T(t)` tends to `T_ambient` as `t` grows, without ever reaching it |
| <a id="inv-04"></a>`INV-04` | **Invariance under a change of origin of the scale**: adding a constant to *all* the temperatures (initial, ambient, addition, target) leaves the instant unchanged — a calculation in kelvin gives the same result as one in degrees Celsius (`CT-06`) |
| <a id="inv-05"></a>`INV-05` | **Mixing is bracketed**: `T_mixed` lies between the two temperatures mixed, and equals the warmer one exactly if the mass added is zero |
| <a id="inv-06"></a>`INV-06` | **Monotonicity in `k`**: all else equal, a larger `k` gives a shorter instant |
| <a id="inv-07"></a>`INV-07` | **Monotonicity in the target**: a lower target gives a later instant |
| <a id="inv-08"></a>`INV-08` | The calculation is deterministic: two runs on the same input give the same result, indicators included |

> `INV-04` and `INV-06` are **symmetry and monotonicity properties**. They are tested
> automatically on thousands of generated inputs and catch the most insidious class of
> error in physical computing: the one where a result silently depends on the unit or the
> origin chosen.

### 8.2 Three levels of exactness, never to be confused

| | **Reproducibility** | **Numerical accuracy** | **Model validity** |
|---|---|---|---|
| Question | Do two conforming implementations give the same number? | Is the number the true solution of the equation `RG-010`? | Does the equation describe reality? |
| Assessed against | The definition in this specification | The **analytical solution** of `RG-010` | **Real measurements** |
| Tolerance required | **10⁻⁹ relative** on temperatures and instants; **strict equality** on the indicators | **10⁻⁶ relative** on the temperature; **`P-05`** on the instant | **± 2 °C** over 60 min, on the reference measurement bench |
| Exceeding it means | An implementation is not conforming, **or the specification is ambiguous** | The solving method or its step is unsuitable | Assumptions `H-1` to `H-4` have left their domain — **this is not a defect of the program** |
| Checked | At acceptance, on every delivery | At acceptance, on the synthesis cases | In **model validation**, periodically, with the business |

> Confusing these three levels is the commonest mistake on scientific software. A gap of
> 1.5 °C between the forecast and a real thermometer may be perfectly normal — it is the
> model that is approximate, not the program that is wrong. A gap of 10⁻⁶ between two
> implementations never is.
>
> **This distinction is what protects the development team.** Without it, every measurement
> gap becomes a bug ticket, and the team spends its time hunting for a defect in correct
> code.

### 8.3 Randomness

This specification contains no stochastic step. If a later version introduces one —
estimating `k` by fitting, propagating uncertainty by Monte Carlo — then **the algorithm of
the pseudo-random generator and its seed become inputs of the specification**, because the
reproducibility of a result depends on them.

## 9. Business error cases

| Code | Condition | Consequence | Message |
|---|---|---|---|
| <a id="e-param-001"></a>`E-PARAM-001` | `cooling_coefficient ≤ 0` | No result | "The cooling coefficient must be strictly positive." |
| <a id="e-param-002"></a>`E-PARAM-002` | A mass or a specific heat capacity of the beverage is zero or negative | No result | "The characteristics of the drink are invalid." |
| <a id="e-entree-001"></a>`E-ENTREE-001` | Neither `requested_instant` nor `target_temperature` is supplied | No result | "Give an instant or a target temperature." |
| <a id="e-horizon-001"></a>`E-HORIZON-001` | `requested_instant > P-04` | No result | "The forecast does not go beyond 2 hours." |
| <a id="e-conv-001"></a>`E-CONV-001` | `P-06` iterations without converging to within `P-05` | No instant produced | "The calculation did not converge." |

## 10. Test set

### Provenance and validation of the expected results

| | |
|---|---|
| **Provenance** | **Analytical solution** of the equation of `RG-010` |
| **How they were examined** | Recomputed on a calculator; the gap between the two variants of `CT-02` checked in both directions |
| **Approved by** | *(role: Product management)* |
| **On** | 2026-01-01 |
| **For version** | 1.0.0 |

*These results are **reference data**: they qualify the code and serve as the
non-regression baseline. They change only through a dated business re-approval
([CADRE §5](../../../CADRE.md)).*

**Building the oracle.** For constant `k` and `T_ambient`, `RG-010` admits the analytical
solution `T(t) = T_amb + (T₀ − T_amb)·exp(−k·t)`, and `RG-050` the solution
`t = ln((T_target − T_amb) ÷ (T₀ − T_amb)) ÷ (−k)`. **The reference truth is therefore
analytical**: it comes from no implementation, and anyone can recompute it with a
scientific calculator.

Unless stated otherwise: `T_ambient = 20.00 °C`, `k = P-01 = 0.03000 min⁻¹`, a beverage of
`0.2000 kg`, specific heat capacity `4.180 kJ·kg⁻¹·K⁻¹`.

### At a glance

| Id | What it exercises | Expected result |
|---|---|---|
| `CT-01` | Simple cooling, temperature and instant reached | `T(30) = 46.4270 °C`; `t(55 °C) = 20.6346 min` |
| `CT-02` | **Order of operations**: early addition vs late addition (`RG-040`) | `55.1833 °C` vs `54.5160 °C` |
| `CT-03` | Target below the ambient temperature (`RG-050`) | `target_unreachable = true` |
| `CT-04` | Target already reached at instant 0 | `t = 0.00`; `target_already_reached = true` |
| `CT-05` | Target reached beyond the horizon | `target_beyond_horizon = true` |
| `CT-06` | **Scale invariance**: same data in kelvin (`INV-04`) | `t = 20.6346 min`, identical to `CT-01` |
| `CT-07` | Zero mass added (`RG-030`) | Temperature unchanged: `85.0000 °C` |
| `CT-08` | Negative cooling coefficient | error `E-PARAM-001` |

### CT-01 — Simple cooling

`T₀ = 85.00 °C`, `T_amb = 20.00 °C`, `k = 0.03000 min⁻¹`, target `55.00 °C`, requested
instant `30.0000 min`.

| Quantity | Calculation | Expected |
|---|---|---|
| Time constant | `1 ÷ 0.03` | 33.33 min |
| `T(30)` | `20 + 65 × exp(−0.9)` | **46.4270 °C** |
| Instant reached | `ln(35 ÷ 65) ÷ (−0.03)` | **20.6346 min** |
| Check | `T(20.6346)` | 55.0000 °C ✓ |
| `iterations_used` | exact solving possible | 0 (or ≤ `P-06` if iterative) |

### CT-02 — Order of operations: the central case

Coffee: `0.2000 kg` at `85.00 °C`, `c = 4.180`.
Milk: `0.0300 kg` at `5.00 °C`, `c = 3.900`.
`T_amb = 20.00 °C`, `k = 0.03000 min⁻¹`. **The temperature is asked at `t = 15 min`.**

Heat capacities: coffee `0.2000 × 4.180 = 0.8360 kJ·K⁻¹`; milk
`0.0300 × 3.900 = 0.1170 kJ·K⁻¹`; total `0.9530 kJ·K⁻¹`.

**Variant A — the milk is added at `t = 0`**

| Step | Calculation | Result |
|---|---|---|
| Mixing (`RG-030`) | `(0.8360 × 85 + 0.1170 × 5) ÷ 0.9530` | 75.1784 °C |
| Cooling for 15 min | `20 + 55.1784 × exp(−0.45)` | **55.1833 °C** |

**Variant B — the milk is added at `t = 15 min`**

| Step | Calculation | Result |
|---|---|---|
| Cooling for 15 min | `20 + 65 × exp(−0.45)` | 61.4458 °C |
| Mixing (`RG-030`) | `(0.8360 × 61.4458 + 0.1170 × 5) ÷ 0.9530` | **54.5160 °C** |

> **Gap: 0.6673 °C, in favour of the immediate addition.** Same ingredients, same duration,
> same amount of milk — two results. This is exactly what a specification has to settle, and
> exactly what a developer cannot guess.
>
> This test case is also an excellent **detector of a lazy implementation**: the one that
> applies the mixing at the end of the calculation "because it is simpler" gives 54.5160 in
> both variants, and passes every other test case.

### CT-03 — Unreachable target

`T₀ = 85.00 °C`, `T_amb = 20.00 °C`, target `15.00 °C`.

- `15.00 ≤ 20.00 + 0.10` → the target is below the ambient temperature plus `P-07`.
- **`target_unreachable = true`**, no instant produced, no error reported.

> This is not an error: the request is legitimate, and the answer is "never". The
> distinction matters, because an error interrupts a processing chain whereas an indicator
> propagates.

### CT-04 — Target already reached

`T₀ = 50.00 °C`, target `55.00 °C`.

- `T(0) = 50.00 ≤ 55.00` → **`target_reached_at = 0.00 min`**,
  `target_already_reached = true`.

> The condition is evaluated **first** in `RG-050`. The order of evaluation of the
> conditions is part of the rule: swapping the first two branches would give
> `target_unreachable` here for a drink that is already drinkable.

### CT-05 — Target beyond the horizon

Insulated mug: `k = P-02 = 0.00600 min⁻¹`, `T₀ = 85.00 °C`, target `25.00 °C`.

| Quantity | Calculation | Expected |
|---|---|---|
| Theoretical instant | `ln(5 ÷ 65) ÷ (−0.006)` | 427.4916 min |
| Compared with the horizon | `427.49 > 120.0` | exceeded |
| Result | | **`target_beyond_horizon = true`**, no instant produced |
| Check | `T(120) = 20 + 65 × exp(−0.72)` | 51.6389 °C — still far from 25 °C ✓ |

### CT-06 — Scale invariance (kelvin)

The same data as `CT-01`, expressed in kelvin: `T₀ = 358.15`, `T_amb = 293.15`, target
`328.15`.

- Gaps unchanged: `358.15 − 293.15 = 65` and `328.15 − 293.15 = 35`.
- **Instant reached: `20.6346 min`, strictly identical to `CT-01`.**

> This case fits in three numbers and checks `INV-04`. It detects the classic mistake of
> writing `T(t) = T₀ × exp(−k·t)` — forgetting the ambient temperature — which gives a
> plausible result in Celsius and an absurd one in kelvin.

### CT-07 — Zero mass added

Addition of `0.0000 kg` at `5.00 °C`, at `t = 0`.

- `RG-030`: `(0.8360 × 85 + 0 × 5) ÷ (0.8360 + 0)` = **85.0000 °C**.
- No special branch: the general rule is enough (`INV-05`).

### CT-08 — Negative coefficient

`k = −0.03000` → **error `E-PARAM-001`**, no result.

### Coverage table

| Rule | Covered by | | Rule | Covered by |
|---|---|---|---|---|
| `RG-010` | all | | `RG-060` | *not covered — see `Q-03`* |
| `RG-020` | CT-01, CT-02 | | `INV-04` | CT-06 + property tests |
| `RG-030` | CT-02, CT-07 | | `INV-05` | CT-07 + property tests |
| `RG-040` | **CT-02** | | `INV-06`, `INV-07` | property tests |
| `RG-050` | CT-01, CT-03, CT-04, CT-05 | | `E-HORIZON-001` | *not covered — `Q-03`* |

> Two empty cells, named rather than hidden. `RG-060` (non-convergence) is not covered
> because no realistic input triggers it with exact solving — which is precisely a question
> to settle (`Q-03`).

## 11. Constraints and requirements

### 11.1 Business constraints

Two usage profiles, **one specification**. It is the constraints sheet that differs, and it
is the constraints sheet that will produce two implementations.

| Dimension | **Embedded** use (consumer application) | **Laboratory** use (teaching tool) |
|---|---|---|
| **Volume** | 2 million forecasts a day, spread over devices; up to 60 recomputations a second while a slider is dragged | a few hundred calculations per session, 50 sessions a day |
| **Call mode** | On demand, **on the device, with no network** | On demand, on a workstation |
| **Latency** | **Under 1 ms per calculation.** Beyond that the slider stutters — the product is judged poor quality | under 2 s; the user is waiting for a plot |
| **Energy and memory** | **Hard constraint**: the calculation must not be noticeable on battery life; memory footprint of a few kilobytes | none |
| **Exactness** | See §8.2. Reproducibility 10⁻⁹; numerical accuracy 10⁻⁶; model validity ± 2 °C at 60 min | the same, **plus** the ability to vary `T_ambient` over time (`Q-02`) |
| **Determinism** | Strict (`INV-08`), indicators included | strict |
| **Replayability** | No requirement: the result is not kept | **Results published in a report must be replayable for 5 years**, with the specification version and the parameter set of the time |
| **Auditability** | None | Inputs, parameters and specification version are kept with every plot |
| **Explainability** | The user must understand why no answer is given: the three indicators of `RG-050` are displayed in plain words | the same, **plus** the number of iterations |
| **Criticality and degraded mode** | No external dependency: the calculation must work in flight mode. If the embedded parameters are older than the latest published version, the calculation happens **anyway**, with the old ones, and the user is told | a calculation in error does not interrupt the session |
| **Confidentiality** | No personal data; no network output — which is also a commercial argument | none |
| **Change frequency** | `P-01`, `P-02`, `P-03`: re-estimated 1 to 2 times a year as the container range evolves. Rules: very rare | the same |
| **Who changes it** | The models manager must be able to re-estimate `P-01` and `P-02` **without a software release** — so the parameters are downloaded, not compiled | the same |
| **Lifetime** | 5 years and more | 10 years (teaching material) |

### 11.2 Implementation requirements

| Id | Requirement | Source | Who approves | Verification |
|---|---|---|---|---|
| <a id="ex-01"></a>`EX-01` | The embedded calculation **makes no network request**: it works offline and transmits nothing | Product commitment "no data leaves the device" | Product management | Network traffic analysis, on every published version |
| <a id="ex-02"></a>`EX-02` | The downloaded parameter file is **signed**, and an invalid signature keeps the previous version in place | Application security policy `SEC-APP-4` | IT security | Non-regression test on an invalid signature |
| <a id="ex-03"></a>`EX-03` | The code follows the **internal coding standard** `STD-DEV-2` | Architecture committee | IT architecture | Continuous integration |

## 12. Open questions

| Id | Question | Decider | Due | Status |
|---|---|---|---|---|
| <a id="q-01"></a>`Q-01` | Should a radiation and evaporation term be added for temperatures above 80 °C, where `H-3` is at its most wrong? It would **break `INV-04`** (the model would stop being invariant under a change of origin of the scale) and would force working in kelvin | Models manager | 2026-11-30 | Open |
| <a id="q-02"></a>`Q-02` | The laboratory use asks for an ambient temperature varying over time. `RG-010` remains valid, but the closed form disappears: only numerical solving is left. Should it be brought into this specification or made a variant? | Product management | 2026-12-31 | Open |
| <a id="q-03"></a>`Q-03` | `RG-060` (non-convergence) and `E-HORIZON-001` are covered by no test case. Should they be kept if no realistic input triggers them? | Business author | 2026-10-15 | Open |
| <a id="q-04"></a>`Q-04` | `P-07` is 0.10 °C. Should that margin depend on the initial gap to the ambient temperature rather than being a constant? | Models manager | 2026-11-30 | Open |
| <a id="q-05"></a>`Q-05` | *Settled on 2025-12-10:* should the instant of the addition have a default value (0, that is "right now")? → **no**. A default on `RG-040` would hide a decision that changes the result; the caller must declare it | Product management | — | Closed |

## 13. History

| Version | Date | Change | Impact on results |
|---|---|---|---|
| 1.0.0 | 2026-01-01 | Initial version | — |
| 1.0.1 | 2026-08-21 | `RG-030` and `RG-040`: the instant of the addition is named `addition.instant`, as in the contract, instead of `instant_ajout` — the same quantity carried two names (a ghost caught by `C-03`) | None — the quantity and its value are unchanged |

---

## Annexe — A technical reading of this specification

> Added by the architect after acceptance. It shows what the specification made it possible
> to decide — and, here, **why the two uses end in two different implementations of the same
> specification**.

| Constraint read in §11 | Technical decision it leads to |
|---|---|
| Embedded: **< 1 ms, no network, energy constraint**, `T_ambient` constant | **The closed-form solution** — two exponentials and a division, with no allocation and no iteration. No numerical integrator is defensible here. `iterations_used` is always 0 |
| Laboratory: `T_ambient` expected to vary (`Q-02`) | **An adaptive-step numerical integrator**, its tolerance set to hold the 10⁻⁶ numerical accuracy of §8.2. The closed form then serves as the integrator's **non-regression case** — a rare luxury, and free here |
| Both, same reproducibility tolerances (§8.2) | The **same test set `CT-01` to `CT-08`** validates both implementations. That is the check that guarantees they will not diverge — and it is possible **only** because the specification imposed no method |
| Numerical accuracy 10⁻⁶ relative, model validity ± 2 °C | **Binary double precision is far more than enough.** A deliberate contrast with [SPEC-MAS-001](../../mass-balance/spec/SPEC-MAS-001.en.md), where the exact conservation of mass demanded an exact decimal: here the physical quantity is itself uncertain to ± 2 °C. **The same method, applied to two domains, produces two opposite conclusions on the numeric type** |
| Three levels of exactness distinguished (§8.2) | Three **separate** test campaigns: non-regression on every delivery, numerical accuracy on the analytical cases, model validation on a measurement bench once a year with the business. Without that separation, a measurement gap would become a bug ticket |
| Parameters re-estimated 1 to 2 times a year without a release | Parameters are **downloaded and versioned**, never compiled; the embedded application works with the latest version it knows and says so. Specific heat capacities, by contrast, are inputs: they come from the user |
| Replayability of 5 years on the laboratory side | Inputs, parameters and specification version archived with every plot. On the embedded side: no persistence, therefore no cost |
| `RG-050`: three distinct indicators | The return type is not "a number or nothing" but **a structured result**. A signature returning `null` on failure would lose the distinction between "already reached", "never" and "too late" — three different messages for the user |

**What the specification deliberately did not say**: the language, the solving method, the
parameter format, the caching strategy. Two teams implemented this document in two opposite
ways, and **both are conforming** — which is the best proof that the frontier was drawn in
the right place.

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../../../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `SPEC-THM-001` | `8416d9a7-cdb6-4116-bc37-a9538d3ff520` | document | SPEC-THM-001 — Cooling forecast for a hot drink |
| `RG-010` | `944cf16e-090c-4191-a688-c1696c65cad4` | règle | The cooling model |
| `RG-020` | `a5b55277-3455-44c9-82d8-c7c98d8d1ff3` | règle | Temperature at a given instant |
| `RG-030` | `378cb511-d51c-4f98-8ef1-b5f414836038` | règle | Mixing two liquids |
| `RG-040` | `dc39074f-2f6b-4c19-a81b-9fc68185e56d` | règle | Order of operations: when the addition happens |
| `RG-050` | `a2f67f9d-5d0f-4b38-b3a6-afd875065ea6` | règle | Instant the target temperature is reached |
| `RG-060` | `ea136394-b2ab-4c96-8cd8-794b0601a8b8` | règle | Convergence |
| `CT-01` | `4b06688f-6465-4532-aa52-0db400bee2f4` | cas de test | Simple cooling |
| `CT-02` | `cfd6fbbf-0aad-4bc7-8951-4c3a53e87223` | cas de test | Order of operations: the central case |
| `CT-03` | `0fbe682c-8776-4e0b-8379-ae7aa9658f96` | cas de test | Unreachable target |
| `CT-04` | `b4ee9744-60d3-4131-86d6-bd96fe9c3416` | cas de test | Target already reached |
| `CT-05` | `37fcab9a-65f1-40e5-920f-e411021411df` | cas de test | Target beyond the horizon |
| `CT-06` | `4d6f1ab4-7ded-4604-b857-b74f36df9407` | cas de test | Scale invariance (kelvin) |
| `CT-07` | `1645cc91-0461-4d1f-9226-2800a352d859` | cas de test | Zero mass added |
| `CT-08` | `7f647fe8-a8cc-4ad3-9d4c-5b3cf790b824` | cas de test | Negative coefficient |
| `P-01` | `96718c6e-7ebb-45a8-becc-7dacb44b8db6` | paramètre | Cooling coefficient — open ceramic cup |
| `P-02` | `6fa6e358-eac1-4052-9fe7-8057f5fa79d8` | paramètre | Cooling coefficient — closed insulated mug |
| `P-03` | `87cb7eb4-c0f7-44bd-90b4-9e00c395c621` | paramètre | Default drinking target temperature |
| `P-04` | `1b4f0d75-6953-45a5-9134-d3e656c3759c` | paramètre | Forecast horizon |
| `P-05` | `75cfe6fc-7213-4967-a81f-f4e80c55c727` | paramètre | Convergence tolerance on the instant |
| `P-06` | `6ea114e2-f73a-4512-8cd4-9656c42c8060` | paramètre | Maximum number of iterations |
| `P-07` | `6f2b55b1-5c6a-451a-a794-963a915f7b63` | paramètre | Minimum gap to the ambient temperature for the target to be deemed rea |
| `EX-01` | `47db7e61-d39c-4d07-ab94-810c0e0032fd` | exigence | The embedded calculation **makes no network request**: it works offlin |
| `EX-02` | `b21a02b4-bdc9-4b4b-af4e-31fdda02e7b7` | exigence | The downloaded parameter file is **signed**, and an invalid signature |
| `EX-03` | `e8c9de71-4877-459f-8946-f1ebfd76f1d8` | exigence | The code follows the **internal coding standard** `STD-DEV-2` |
| `INV-01` | `b11ac534-4ee3-4ae5-8395-6f6b65dca68d` | invariant | If `T(0) > T_ambient`, then `T(t)` is strictly decreasing |
| `INV-02` | `7c52110c-fc9a-4bad-8b2e-30511a8b1751` | invariant | `T(t)` stays strictly between `T_ambient` and `T(0)` |
| `INV-03` | `53836c25-75aa-4f5c-882f-2a05f28f1882` | invariant | `T(t)` tends to `T_ambient` as `t` grows, without ever reaching it |
| `INV-04` | `cdc085b1-e472-43e3-be7b-2d28930aeed1` | invariant | Invariance under a change of origin of the scale: adding a constant to |
| `INV-05` | `6b37cd54-28a1-49d2-a574-60c454308e15` | invariant | Mixing is bracketed: `T_mixed` lies between the two temperatures mixed |
| `INV-06` | `67c240de-37c7-4d40-a6cc-330545759eb9` | invariant | Monotonicity in `k`: all else equal, a larger `k` gives a shorter inst |
| `INV-07` | `3e382a1c-177b-47e7-bb99-0a700f03541a` | invariant | Monotonicity in the target: a lower target gives a later instant |
| `INV-08` | `c3221013-33d7-482b-bb07-98fa142945ca` | invariant | The calculation is deterministic: two runs on the same input give the |
| `E-PARAM-001` | `b19ad839-f72a-4446-a9ec-f02a8aa522a1` | cas d'erreur | `cooling_coefficient ≤ 0` |
| `E-PARAM-002` | `b757d637-570b-4594-bad2-040776993e62` | cas d'erreur | A mass or a specific heat capacity of the beverage is zero or negative |
| `E-ENTREE-001` | `ef5edef2-6e13-4c75-83e6-e4d82f6dfe6a` | cas d'erreur | Neither `requested_instant` nor `target_temperature` is supplied |
| `E-HORIZON-001` | `089406dd-f578-4bef-8d4b-3acbefc50e82` | cas d'erreur | `requested_instant > P-04` |
| `E-CONV-001` | `1b5551ba-9d1b-44e3-beb3-89aac7d2fb26` | cas d'erreur | `P-06` iterations without converging to within `P-05` |
| `Q-01` | `c048bd3f-7a20-4669-b9c7-2b29d366394c` | question | Should a radiation and evaporation term be added for temperatures abov |
| `Q-02` | `6819e7f2-3111-47f1-bc56-070aa8d47524` | question | The laboratory use asks for an ambient temperature varying over time. |
| `Q-03` | `cf4f641d-263d-421b-84fa-4869d01a6a03` | question | `RG-060` (non-convergence) and `E-HORIZON-001` are covered by no test |
| `Q-04` | `aec32bd5-c60a-44a3-9485-5f4187d080fd` | question | `P-07` is 0.10 °C. Should that margin depend on the initial gap to the |
| `Q-05` | `cb226d98-6d59-4412-9083-0b7dfb034ca7` | question | Settled on 2025-12-10:* should the instant of the addition have a defa |
