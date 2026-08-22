# 1 — The need, as it arrives

*First step of the chain. This document comes **before** any formalisation: it is what the
business says, in its own words, before we ask it anything.*

---

## What the formulation manager says

> "We prepare batches by mixing several components. For each batch, the recipe gives
> percentages: this component 33.3 %, that one 20 %, and so on. We multiply by the batch
> mass, and that gives the mass to weigh for each one.
>
> The problem is that the balance does not go below the gram. So we round. And when we add
> up the rounded masses, **we do not land on the batch mass**. One gram is missing, or
> there is one too many.
>
> Today each operator sorts it out on their own. Some put the difference on the first
> component, some on the largest one, some do not correct at all. The result: two
> operators, two different batches, from the same recipe. And at audit time, we cannot
> explain why.
>
> What we want is a calculation done once and for all, the same way every time, and that
> we can justify."

---

## What we keep from this, and what we still do not know

**What the need says clearly.** There is a calculation, it is simple, and today it produces
results that differ between operators. The difference is not caused by a mistake: it comes
from **a decision nobody has taken explicitly**.

**What it does not say — and what will have to be decided.** These questions are not
implementation details. They are the business decisions the specification will have to
carry, and that no developer can take in place of the business:

| Question | Why it belongs to the business |
|---|---|
| Round in which direction? To the nearest — and if we land exactly halfway? | Over thousands of batches, "half up" always uses a little more material than planned |
| Who receives the missing gram? | Each answer gives a different batch, and the manager answers for it at audit time |
| And if two components are tied to receive it? | Without a rule, the result depends on the order of entry — so it is not reproducible |
| Up to which difference do we accept to correct? | One gram is normal; fifty mean the recipe or the balance do not fit, and absorbing them would hide the problem |
| What do we do if the percentages do not add up to 100 %? | The recipe is wrong. Fixing it silently would be worse than refusing it |

> **These are exactly what the method calls [false friends](../../CADRE.md).** They look
> like technical details — a rounding, an order of traversal — and they are business
> decisions. As long as they are not written down, **the developer will take them**,
> without knowing it and without a mandate.

---

## What is decided at this step, and what is not

**Decided.** The scope: we compute the masses to weigh. We do not drive the balance, we do
not trace raw material batches, we do not check the quality of the mix.

**Not decided yet, and on purpose.** No data structure, no language, no exchange format, no
performance figure. The need does not say *how* to round either: it says that **it has to
be decided**. That difference is the whole method.

---

→ Next step: [2 — the specification](2-SPEC-MAS-001.en.md), where each question above gets a
written, numbered and justified answer.
