# 3 — Le contrat, et son passage vers le code

*Troisième étape. Le contrat existe déjà, aux §4 et §5 de la
[spécification](spec/SPEC-MAS-001.en.md). Ce document ne le réécrit pas : il
montre **ce qu'un développeur en fait**, et pourquoi ce passage est mécanique.*

---

## Le contrat se valide avant le développement, et sert à qualifier après

C'est sa double vie, et elle explique pourquoi il est écrit avec cette rigueur :

| Avant | Après |
|---|---|
| Le relecteur technique répond **oui ou non** à « ai-je tout pour coder sans reposer de question ? » | Le contrat devient le critère de conformité : ce qui entre, ce qui sort, et dans quels domaines |
| Les contrôles mécaniques `C-01` à `C-04` y cherchent les **morts** et les **fantômes** | Le harnais de qualification rejoue les données de référence contre lui |

---

## Ce que chaque colonne du contrat impose au code

Le contrat n'est pas une liste de champs : chaque attribut porte une contrainte que le
développeur doit honorer, et qu'il ne peut pas relâcher seul.

| Ce que le contrat écrit | Ce que le développeur en déduit | Ce qu'il n'a **pas** le droit de faire |
|---|---|---|
| `Mass(kg, …)` | la grandeur est une masse, en kilogrammes | changer d'unité sans conversion déclarée à la frontière |
| `[Scaled]` | famille ISO/IEC 11404 : décimal **exact** | prendre un `double` — `INV-01` exige une égalité stricte |
| `3 decimals` | la résolution de la valeur | en afficher plus, ou arrondir plus tôt |
| `> 0`, `≥ 0` | le domaine de validité | accepter une valeur hors domaine « pour être tolérant » |
| `Sequence[Component](1 .. 50)` | ordonné, borné | supposer un ordre de tri, ou ignorer la borne |
| `unique` sur `component_id` | une précondition, `E-MAS-003` | dédupliquer silencieusement |

> **L'étendue bornée n'est pas un détail.** `1 .. 50` dit au développeur qu'un lot ne
> comporte jamais mille composants : il peut choisir la structure la plus simple. Une
> étendue **non bornée** est un défaut de spécification, jamais une souplesse — elle
> transfère au développeur une décision de dimensionnement qu'il prendra à l'aveugle.

---

## La correspondance vers Java

Elle est **déclarée une fois, par cible**, et systématique — c'est ce qui permet à la
traçabilité de tenir dans les deux sens.

| Contrat (§4, §5) | Famille ISO/IEC 11404 | Java |
|---|---|---|
| `target_batch_mass : Mass(kg, > 0, 3 decimals)` | `Scaled` | `BigDecimal targetBatchMass` |
| `component_id : Identifier(text, 3 to 20)` | `CharacterString` | `String componentId` |
| `target_mass_fraction : Ratio(dimensionless, 6 decimals)` | `Scaled` | `BigDecimal targetMassFraction` |
| `components : Sequence[Component](1 .. 50)` | `Sequence` | `List<Component>` |
| `Component`, `Dispensed` | — | `record Component`, `record Dispensed` |
| `E-MAS-00x` (§9) | — | `BusinessError`, portant son **code**, pas seulement un message |

Le nommage suit la règle de [CADRE §2.4](../../CADRE.md) : `snake_case` dans la
spécification, convention du langage cible dans le code — `target_batch_mass` devient
`targetBatchMass`, mécaniquement.

> **Pourquoi l'erreur porte son identifiant.** `E-MAS-002` est un objet de la
> spécification, avec une identité durable et un comportement déclaré. Une exception qui
> ne porterait qu'un message serait intraçable : on ne pourrait ni la tester contre les
> données de référence, ni prouver à un auditeur que le cas prévu est bien celui qui se
> produit.

---

## Ce que le contrat ne dit pas, et c'est volontaire

`BigDecimal` plutôt qu'un entier de pas de balance. Un seul passage plutôt que deux. Des
`record` plutôt que des classes. L'ordre de parcours. Ces décisions appartiennent au
développeur — la spécification a **contraint le résultat et libéré le chemin**.

Un entier de pas serait d'ailleurs un choix parfaitement défendable, peut-être meilleur :
il rendrait `INV-02` vrai par construction. Que la spécification n'ait pas tranché est ce
qui laisse cette porte ouverte.

---

→ Étape suivante : [4 — le code](code/), qui met en œuvre ces règles, et le harnais qui
le qualifie contre les données de référence.
