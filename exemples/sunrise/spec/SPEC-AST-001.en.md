# SPEC-AST-001 — Sunrise and sunset times

| | |
|---|---|
| **Identifier** | SPEC-AST-001 |
| **Version** | 1.0.0 |
| **Status** | Approved |
| **Business approver** | Ephemerides manager |
| **Technical co-author** | Geospatial services team |
| **Reference glossary** | [GLOSSAIRE.md](../../../GLOSSAIRE.md) v1.0.0 |

> **Why this example.** It carries a regime no other example in the repository shows: **an
> equation that does not always have a solution**. Beyond the polar circles the Sun may
> never rise, or never set. That is **not an error**: it is a result, and it has to be
> named, typed and returned.
>
> It also carries two false friends the others do not: **angle sign conventions**, where an
> inverted sign gives a plausible but wrong answer, and **the choice of the definition
> itself** — "sunrise" names four different instants depending on what you decide to
> measure.

---

## 1. Purpose and context

A service must publish, for a place and a date, the times of sunrise and sunset: public
display, lighting control, observation planning.

The difficulty is not in the equations, which have been known and stable for a long time.
It is in the **conventions**: four decisions have to be taken before a developer can write
a single line, and none of them is readable from the formulas.

---

## 2. Scope

**In scope.** For a given place and date, the instants of sunrise and sunset, in
coordinated universal time.

**Out of scope.** Twilight (civil, nautical, astronomical), the Moon, the observer's
altitude, the surrounding terrain, the conversion to a local time and its zone — which
belong to display, not to the calculation.

---

## 3. Glossary

| Term | Definition |
|---|---|
| **Declination** (`δ`) | The angle between the direction of the Sun and the plane of the celestial equator. It varies from −23.44° to +23.44° over the year; it is what makes the seasons. |
| **Hour angle** (`ω`) | The angle the Earth must turn through between the instant considered and local solar noon. 15° per hour. |
| **Solar transit** | The instant the Sun crosses the meridian of the place. It does **not** coincide with 12:00: the Earth's orbit is neither circular nor in the plane of the equator. |
| **Reference altitude** (`h₀`) | The angular height of the Sun that **defines** sunrise and sunset. It is not 0°: see `P-01`. |
| **Polar day / polar night** | The regimes where the Sun never crosses `h₀` — in one direction or the other. The equation admits **no** solution there. |

---

## 4. Inputs

```
request :
    observation_date : Date(Gregorian calendar)                     [DateAndTime]
    latitude         : Angle(°, −90.0000 .. +90.0000, 4 decimals)   [Real]
    longitude        : Angle(°, −180.0000 .. +180.0000, 4 decimals) [Real]
```

**Preconditions:**
- `latitude` is counted **positive northwards** (`P-02`).
- `longitude` is counted **positive eastwards** (`P-02`).
- `observation_date` is a civil date, with no time and no zone: the calculation is about
  the whole day.

> **The sign convention is part of the contract, not something obvious.** Both longitude
> conventions — east positive, west positive — coexist in the literature and in databases.
> Confusing them moves the result by twice the longitude: in Paris, **nineteen minutes**.
> The result stays perfectly plausible, which is exactly what makes the mistake expensive.

---

## 5. Outputs

```
result :
    solar_regime  : Enumerated { NORMAL, POLAR_DAY, POLAR_NIGHT }   [Enumerated]
    sunrise_time  : TimeOfDay(UTC, to the minute) — ABSENT if solar_regime ≠ NORMAL
    sunset_time   : TimeOfDay(UTC, to the minute) — ABSENT if solar_regime ≠ NORMAL
    declination   : Angle(°, −23.5000 .. +23.5000, 4 decimals)      [Real]
```

> **`solar_regime` is not an error code.** A polar day is an astronomical fact, not a
> failure: the function succeeded perfectly. Returning it as an error would force every
> caller to handle as an exception a case that happens six months a year in Longyearbyen.

---

## 6. Parameters

| Id | Name | Value | Unit | Who may change it | Effective date |
|---|---|---|---|---|---|
| <a id="p-01"></a>`P-01` | Reference altitude `h₀` | −0.833 | ° | Ephemerides manager | 2026-01-01 |
| <a id="p-02"></a>`P-02` | Sign convention of the coordinates | north and east positive | — | Ephemerides manager | 2026-01-01 |
| <a id="p-03"></a>`P-03` | Rounding direction to the minute | `P-03.sunrise` = `CEILING`, `P-03.sunset` = `FLOOR` | — | Ephemerides manager | 2026-01-01 |
| <a id="p-04"></a>`P-04` | Obliquity of the ecliptic | 23.4397 | ° | Ephemerides manager | 2026-01-01 |

> **`P-01` carries the very definition of the result, and it is the central false friend.**
> "The Sun rises" can name four distinct instants:
>
> | Definition | `h₀` | Difference in Paris |
> |---|---|---|
> | Centre of the disc at the **geometric** horizon | 0° | reference |
> | Centre of the disc, atmospheric refraction included | −0.583° | ≈ 2 min earlier |
> | **Upper limb of the disc, refraction included** | **−0.833°** | ≈ 3 to 4 min earlier |
> | Upper limb, without refraction | −0.250° | ≈ 1 min earlier |
>
> The third is the convention of published ephemerides, and it is the one retained. A
> developer writing `0°` — the "natural" choice — would produce a service whose every value
> is a few minutes wrong, with no test to say so if the expected results come from the code
> itself.

> **`P-03` is asymmetric, and deliberately so.** Rounding sunrise up and sunset down
> **narrows** the daylight window announced. That is the cautious direction for the uses
> that depend on it — switching street lighting off, ending outdoor work: it is better to
> announce a day slightly shorter than it is. Rounding to nearest in both directions would
> be more "accurate" and less safe.

---

## 7. Rules

### 7.1 The algorithm, in one piece

**The rules below are the steps of a single algorithm, not fragments to assemble.** Here is
its composition, as the business states it:

```
DEFINE compute_sunrise_sunset(request) : result

    PRECONDITIONS  E-AST-001, E-AST-002

    LET julian_day         = ...                    (RG-005)
    LET days_since_epoch   = ...                    (RG-010)
    LET mean_anomaly       = ...                    (RG-020)
    LET ecliptic_longitude = ...                    (RG-020)
    declination            = ...                    (RG-030)
    LET solar_transit      = ...                    (RG-040)
    LET cos_hour_angle     = ...                    (RG-050)

    IF cos_hour_angle IS OUTSIDE [−1, 1] THEN       (RG-050)
        solar_regime = POLAR_DAY OR POLAR_NIGHT, according to its sign
        sunrise_time = ABSENT
        sunset_time  = ABSENT
    ELSE
        solar_regime = NORMAL
        sunrise_time = ...                          (RG-060)
        sunset_time  = ...                          (RG-060)
    END IF

    RETURN result
```

> **The integrated algorithm reads in one go; the numbered rules are addressable.** They
> are two views of the same calculation, not two options. Without the first, the order
> would be left to guess; without the second, nothing — a test case, a change notice, a
> code comment — could be attached to a precise point.

### 7.2 Processing chain

| Step | Consumes | Produces | Rules |
|---|---|---|---|
| `ET-01` Julian day | `observation_date` | `julian_day` | `RG-005` |
| `ET-02` Local day | `julian_day`, `longitude` | `days_since_epoch` | `RG-010` |
| `ET-03` Orbital position | `days_since_epoch` | `mean_anomaly`, `ecliptic_longitude` | `RG-020` |
| `ET-04` Declination | `ecliptic_longitude`, `P-04` | `declination` | `RG-030` |
| `ET-05` Solar transit | `days_since_epoch`, `longitude`, `mean_anomaly`, `ecliptic_longitude` | `solar_transit` | `RG-040` |
| `ET-06` Solar regime | `latitude`, `declination`, `ecliptic_longitude`, `P-01` | `cos_hour_angle`, `solar_regime` | `RG-050` |
| `ET-07` Instants | `cos_hour_angle`, `solar_regime`, `solar_transit`, `P-03` | `sunrise_time`, `sunset_time` | `RG-060` |

```bash
java outils/Verifier.java --chaine exemples/sunrise/spec/SPEC-AST-001.en.md
```

> **Unlike the mass balance, this chain is not a queue.** `ET-04` (declination) and `ET-05`
> (solar transit) do not depend on each other: they are **independent and computable in
> parallel**. The tool derives that from the table, without anyone writing it down. The
> specification says what is independent; it does not say to take advantage of it — that
> choice belongs to the developer.

### 7.3 Internal quantities

*Scope **internal**: visible inside the body of this function only. They appear neither in
the contract nor in the data catalogue — but they are described with the same rigour,
because a developer has to know what to instantiate.*

```
julian_day         : Real(days, > 0, 6 decimals)      -- Julian day of observation_date at 0 h UTC
days_since_epoch   : Integer(days)                    -- whole days since J2000.0
mean_anomaly       : Angle(°, 0.0000 .. 360.0000, 4 decimals)
equation_of_centre : Angle(°, −2.0000 .. +2.0000, 4 decimals)
ecliptic_longitude : Angle(°, 0.0000 .. 360.0000, 4 decimals)
solar_transit      : Real(days, > 0, 6 decimals)      -- solar noon, as a Julian day
cos_hour_angle     : Real(dimensionless, 5 decimals)  -- domain NOT bounded to [−1, 1]: see RG-050
hour_angle         : Angle(°, 0.0000 .. 180.0000, 4 decimals)
```

> **`cos_hour_angle` is not bounded to `[−1, 1]`, and that is deliberate.** It is a cosine,
> so you would expect it in that interval — but the quantity computed legitimately leaves
> it, and that is precisely what carries the information of `RG-050`. Declaring a domain of
> `−1 .. 1` would lead a developer to validate the input of the arc cosine and to reject
> the polar cases as errors.

### RG-005 — Julian day of the observation date

```
julian_day = Julian day of observation_date at 0 h UTC, Gregorian calendar
```

> **We do not rewrite the calendar conversion.** It is a standardised algorithm, stable for
> centuries and available in any date library. The specification says *which quantity is
> expected and on which scale*; reimplementing the conversion would be exactly the kind of
> technical decision §1.4 leaves to the developer.

### RG-010 — Day at solar noon

```
LET days_since_epoch = ROUND( julian_day(observation_date) − 2451545.0
                              + 0.0008 + longitude ÷ 360, 0, HALF_UP )
```

> **`− 2451545.0`** brings the date back to epoch J2000.0. **`+ 0.0008`** corrects for the
> accumulated leap seconds. **`+ longitude ÷ 360`** attaches the date to the **local** solar
> day: this is where the convention of `P-02` enters the calculation, and where an inverted
> sign goes unnoticed.

### RG-020 — Position of the Sun on its orbit

```
LET mean_anomaly = ( 357.5291 + 0.98560028 × days_since_epoch ) modulo 360

LET equation_of_centre = 1.9148 × sin(mean_anomaly)
                       + 0.0200 × sin(2 × mean_anomaly)
                       + 0.0003 × sin(3 × mean_anomaly)

LET ecliptic_longitude = ( mean_anomaly + equation_of_centre + 180 + 102.9372 ) modulo 360
```

> **`modulo 360` is a decision, not a convenience.** An angle is defined up to a full turn.
> Without an explicit reduction, an implementation accumulates values of several thousand
> degrees over the centuries — mathematically correct, and catastrophic for the precision
> of a sine in floating point.

### RG-030 — Declination

```
declination = arcsin( sin(ecliptic_longitude) × sin(P-04) )
```

### RG-040 — Solar transit

```
LET solar_transit = 2451545.0 + days_since_epoch − longitude ÷ 360
                  + 0.0053 × sin(mean_anomaly)
                  − 0.0069 × sin(2 × ecliptic_longitude)
```

> **Solar noon is earlier towards the east.** Hence `− longitude ÷ 360`, and not `+`. The
> last two terms are the **equation of time**: they reach ±16 minutes, and leaving them out
> is the most frequent mistake in this calculation.

### RG-050 — Hour angle, and existence of a solution

```
LET cos_hour_angle = ( sin(P-01) − sin(latitude) × sin(declination) )
                     ÷ ( cos(latitude) × cos(declination) )

IF cos_hour_angle < −1 THEN
    solar_regime = POLAR_DAY
ELSE IF cos_hour_angle > 1 THEN
    solar_regime = POLAR_NIGHT
ELSE
    solar_regime = NORMAL
END IF
```

> **This is the business core of the specification.** The arc cosine is defined on
> `[−1, 1]` only. Outside that interval there is no instant at which the Sun crosses `h₀` —
> and the **sign** says which of the two regimes is observed: too low to come back down
> (`< −1`, polar day), or too high to come up (`> 1`, polar night).
>
> An implementation calling `arccos` without that test would produce, depending on the
> language, an exception, a silent `NaN`, or a value folded onto the bound — three different
> behaviours for one specification. This is exactly the kind of gap the
> [double implementation test](../../../CADRE.md) exists to reveal.

### RG-060 — Sunrise and sunset instants

```
IF solar_regime = NORMAL THEN
    LET hour_angle = arccos( cos_hour_angle )
    sunrise_time = ROUND( solar_transit − hour_angle ÷ 360, minute, P-03.sunrise )
    sunset_time  = ROUND( solar_transit + hour_angle ÷ 360, minute, P-03.sunset )
ELSE
    sunrise_time = ABSENT
    sunset_time  = ABSENT
END IF
```

> **`ABSENT`, and not midnight, nor an empty string, nor `00:00`.** The absence is business
> information here: *there is no sunrise instant that day*. A default value would make it
> disappear, and a caller would display "sunrise at 00:00" on a polar night.

### Coverage table

| Rule | Covered by |
|---|---|
| `RG-005` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-010` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-020` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-030` | CT-01, CT-02, CT-03, CT-04, CT-05 |
| `RG-040` | CT-01, CT-02, CT-03 |
| `RG-050` | CT-01, CT-02, CT-03 (`NORMAL`), CT-04 (`POLAR_DAY`), CT-05 (`POLAR_NIGHT`) |
| `RG-060` | CT-01, CT-02, CT-03; CT-04 and CT-05 for the `ABSENT` branch |

---

## 8. Invariants

| Id | Statement |
|---|---|
| <a id="inv-01"></a>`INV-01` | If `solar_regime = NORMAL`, then `sunrise_time < sunset_time` — sunrise always precedes sunset on the same solar day. |
| <a id="inv-02"></a>`INV-02` | `sunrise_time` and `sunset_time` are **both** `ABSENT` or both present. Never one without the other. |
| <a id="inv-03"></a>`INV-03` | `−23.44° ≤ declination ≤ +23.44°`, whatever the date. A value outside that interval signals an angle conversion error — the commonest symptom of a degrees / radians confusion. |
| <a id="inv-04"></a>`INV-04` | **Symmetry in longitude**: at equal latitude, moving the longitude `Δ` degrees eastwards brings sunrise **and** sunset forward by `Δ ÷ 15` hours, up to the rounding minute. This is what catches an inverted sign convention. |
| <a id="inv-05"></a>`INV-05` | `solar_regime ≠ NORMAL` implies `|latitude| > 66.0°`. Below the polar circle there is always a sunrise and a sunset. |

> `INV-04` and `INV-05` are tested on **generated** inputs, not on the five cases of the
> test set. `INV-05` in particular sweeps the whole globe: it is the only check that
> verifies the polar regime never fires where it must not.

---

## 9. Business error cases

| Id | Condition | Behaviour |
|---|---|---|
| <a id="e-ast-001"></a>`E-AST-001` | `latitude` outside `[−90, +90]` | Reject: the coordinate does not exist |
| <a id="e-ast-002"></a>`E-AST-002` | `longitude` outside `[−180, +180]` | Reject. **We do not silently wrap**: a longitude of 200° signals a convention not respected upstream, and correcting it would hide the defect |

> **The polar day is not in this table, and that is the point of the specification.** It is
> not an error case: it is a result, carried by `solar_regime`.

---

## 10. Test set

### CT-01 — Paris, summer solstice

`2026-06-21`, latitude `48.8566`, longitude `2.3522`

| | |
|---|---|
| `declination` | **+23.4393°** |
| `cos_hour_angle` | −0.52032 |
| `solar_regime` | **NORMAL** |
| sunrise, before rounding | 03:46:52 UTC → **03:47** (up) |
| sunset, before rounding | 19:57:42 UTC → **19:57** (down) |

### CT-02 — Paris, winter solstice

`2026-12-21`, latitude `48.8566`, longitude `2.3522`

| | |
|---|---|
| `declination` | **−23.4371°** |
| `cos_hour_angle` | +0.47210 |
| `solar_regime` | **NORMAL** |
| sunrise | 07:41:13 UTC → **07:42** |
| sunset | 15:55:51 UTC → **15:55** |

### CT-03 — Quito, equinox, negative longitude

`2026-03-20`, latitude `−0.1807`, longitude `−78.4678`

| | |
|---|---|
| `declination` | **−0.6159°** |
| `cos_hour_angle` | −0.01457 |
| `solar_regime` | **NORMAL** |
| sunrise | 11:18:23 UTC → **11:19** |
| sunset | 23:25:04 UTC → **23:25** |

**This is the case that discriminates the sign convention.** A western longitude, a sunrise
and a sunset late in the UTC day. Under the opposite convention the result stays
plausible — and is wrong by more than ten hours.

### CT-04 — Tromsø, polar day

`2026-06-01`, latitude `69.6492`, longitude `18.9553`

| | |
|---|---|
| `declination` | **+22.0328°** |
| `cos_hour_angle` | **−1.13615** → outside `[−1, 1]` |
| `solar_regime` | **POLAR_DAY** |
| `sunrise_time`, `sunset_time` | **ABSENT**, both of them |

### CT-05 — Tromsø, polar night

`2026-12-01`, latitude `69.6492`, longitude `18.9553`

| | |
|---|---|
| `declination` | **−21.7751°** |
| `cos_hour_angle` | **+1.03195** → outside `[−1, 1]` |
| `solar_regime` | **POLAR_NIGHT** |
| `sunrise_time`, `sunset_time` | **ABSENT**, both of them |

**CT-04 and CT-05 differ only by the sign** of `cos_hour_angle`. An implementation testing
`|cos_hour_angle| > 1` without looking at the sign would pass both cases while confusing
polar day and polar night — the gravest error this function can make, and the only one
these two cases reveal.

### Provenance and validation

| | |
|---|---|
| **Provenance** | Classical ephemeris equations, applied independently of any implementation of the component |
| **How they were examined** | The three `NORMAL` cases were compared with the times published for those places and dates: agreement to the minute. The two polar cases were checked by the sign of `cos_hour_angle` and against the known astronomical fact |
| **What the examination produced** | A sign error on the longitude was found **by that comparison**, not by re-reading: the calculation placed the Paris sunrise 19 minutes late, a perfectly plausible gap on a display. That is what motivated `P-02`, `INV-04` and CT-03 |
| **Approved by** | Ephemerides manager, 2026-08-21 |

---

## 11. Constraints and requirements

| Id | Statement | Source | Owner | Verification |
|---|---|---|---|---|
| <a id="ex-01"></a>`EX-01` | **Accuracy: ± 2 minutes** against the reference ephemerides, for every latitude `|φ| < 65°`. Beyond that the error grows sharply: the Sun grazes the horizon there, and a minute angular difference moves the instant by several minutes | Service commitment | Ephemerides manager | Annual comparison over 200 places |
| <a id="ex-02"></a>`EX-02` | **Binary double precision is enough.** The output quantity is rounded to the minute, that is 4 × 10⁻⁴ day, far above any numerical noise | Accuracy analysis | IT architecture | Design review |
| <a id="ex-03"></a>`EX-03` | The calculation is **replayable identically**: no dependency on the current clock nor on the machine's time zone | Service requirement | IT architecture | Replay against archive |
| <a id="ex-04"></a>`EX-04` | Volume: up to 5 000 calls per second at peak, 99th-percentile latency under 5 ms | Production measurement | Service manager | Quarterly load test |

> **`EX-01` distinguishes the accuracy of the model from the exactness of the calculation**
> ([CADRE §2.9](../../../CADRE.md)). The ±2 minutes do not come from numerical imprecision:
> they come from the **model**, which neglects the actual refraction of the day, the
> observer's altitude and the terrain. A calculation in infinite precision would give the
> same gap. Confusing the two would lead someone to hunt for a bug where there is none.

---

## 12. Open questions

| Id | Question | Decider | Due | Status |
|---|---|---|---|---|
| <a id="q-01"></a>`Q-01` | Should civil, nautical and astronomical twilight be exposed? They are the same rules with `h₀` at −6°, −12° and −18°: `P-01` would become an input | Ephemerides manager | 2026-11-01 | Open |
| <a id="q-02"></a>`Q-02` | Should the observer's altitude be corrected for? The effect reaches 4 minutes at 1 000 m | Ephemerides manager | 2026-11-01 | Open |
| <a id="q-03"></a>`Q-03` | Should the service return local time rather than UTC? | Ephemerides manager | | Closed: no, the time zone belongs to display |

---

## 13. History and change notices

| Version | Date | Change | Impact on results | Notice |
|---|---|---|---|---|
| 1.0.0 | 2026-08-21 | Initial version | — | — |

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../../../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `SPEC-AST-001` | `428e65ae-0ed5-4a6b-8eb4-e41d0215211a` | document | SPEC-AST-001 — Sunrise and sunset times |
| `RG-005` | `f278d30e-0fd0-4d13-a1d8-fcbce08fde09` | règle | Julian day of the observation date |
| `RG-010` | `af97c323-6b9f-4fd6-9713-4e06c6d4672e` | règle | Day at solar noon |
| `RG-020` | `beef87a2-3576-4109-91f9-89d9fc288add` | règle | Position of the Sun on its orbit |
| `RG-030` | `3b88e87e-2677-41d1-b5fc-c8c951311eca` | règle | Declination |
| `RG-040` | `16b13a55-55df-4eb7-b868-bc369b1138c0` | règle | Solar transit |
| `RG-050` | `de6df248-be98-4400-8d44-f15007369dd1` | règle | Hour angle, and existence of a solution |
| `RG-060` | `8f627688-ce42-47d8-a4b5-0e55eb8d1571` | règle | Sunrise and sunset instants |
| `CT-01` | `66ca83b9-80a9-49ed-8d97-9836840ca7d7` | cas de test | Paris, summer solstice |
| `CT-02` | `caf14734-10f4-4794-92ac-b92659005d00` | cas de test | Paris, winter solstice |
| `CT-03` | `6a936d8a-f897-4ada-883b-4d430c07ad49` | cas de test | Quito, equinox, negative longitude |
| `CT-04` | `a3113841-3709-4029-9ccd-7ceef61af128` | cas de test | Tromsø, polar day |
| `CT-05` | `ad06693c-79b6-44d2-a7eb-bf351321c88a` | cas de test | Tromsø, polar night |
| `P-01` | `103ce181-d9a9-43da-8828-8b39052e7b99` | paramètre | Reference altitude `h₀` |
| `P-02` | `c8a555a6-878d-4f81-afce-2434795a0886` | paramètre | Sign convention of the coordinates |
| `P-03` | `5b432b90-2b5c-482f-a032-922be7159f4e` | paramètre | Rounding direction to the minute |
| `P-04` | `a9c2da1c-b965-4ba5-90c9-d4635518a4ea` | paramètre | Obliquity of the ecliptic |
| `EX-01` | `e9900cc4-7c76-48db-a91f-521b12b216d1` | exigence | Accuracy: ± 2 minutes against the reference ephemerides, for every lat |
| `EX-02` | `4a8d293a-238f-4600-89b0-3a587757d3e8` | exigence | Binary double precision is enough. The output quantity is rounded to t |
| `EX-03` | `3125ec69-b46f-4d89-b533-f5b6985a4e50` | exigence | The calculation is **replayable identically**: no dependency on the cu |
| `EX-04` | `871209e3-ca46-4ea2-8f77-455f0575b2f4` | exigence | Volume: up to 5 000 calls per second at peak, 99th-percentile latency |
| `INV-01` | `3dd144ed-ae0c-4122-a8e9-6cfe601315c0` | invariant | If `solar_regime = NORMAL`, then `sunrise_time < sunset_time` — sunris |
| `INV-02` | `53eadd31-c842-4683-974c-e02c56b7aeb3` | invariant | `sunrise_time` and `sunset_time` are **both** `ABSENT` or both present |
| `INV-03` | `062582ac-178f-486e-ab1e-94ccce05a0ca` | invariant | `−23.44° ≤ declination ≤ +23.44°`, whatever the date. A value outside |
| `INV-04` | `8cad23b6-1739-4778-899a-8b3d7c2958fd` | invariant | Symmetry in longitude: at equal latitude, moving the longitude `Δ` d |
| `INV-05` | `77f14f85-1c37-4cb0-b59c-3cd5e9ea5c33` | invariant | `solar_regime ≠ NORMAL` implies |
| `E-AST-001` | `baea8eed-a39c-46c8-96e6-543b35802c95` | cas d'erreur | `latitude` outside `[−90, +90]` |
| `E-AST-002` | `b7f23f9b-aa20-4597-9d4b-ca157a334135` | cas d'erreur | `longitude` outside `[−180, +180]` |
| `Q-01` | `68ad3237-3c15-4f58-a8d6-ee9383bf46e2` | question | Should civil, nautical and astronomical twilight be exposed? They are |
| `Q-02` | `392ff06a-a1d6-4f50-9cb2-858811eb569b` | question | Should the observer's altitude be corrected for? The effect reaches 4 |
| `Q-03` | `b35f41d9-46c8-441d-a4be-11abad2464f7` | question | Should the service return local time rather than UTC? |
