# SPEC-SPD-001 — Vitesse moyenne d'un trajet

| | |
|---|---|
| **Identifiant** | SPEC-SPD-001 |
| **Version** | 1.0.0 |
| **Statut** | Validée |
| **Valideur métier** | Responsable exploitation de flotte |
| **Co-auteur technique** | Équipe logiciel mobilité |
| **Glossaire de référence** | [GLOSSAIRE.md](../../../GLOSSAIRE.md) v1.0.0 |
| **Traduction de** | [SPEC-SPD-001.en.md](SPEC-SPD-001.en.md) |

> **Version française, non normative.** La version qui fait foi est
> [`SPEC-SPD-001.en.md`](SPEC-SPD-001.en.md). `C-42` vérifie que les deux portent les
> mêmes objets et les mêmes identités durables.

> **Commencez par là.** C'est le plus petit exemple du dépôt : deux fonctions, deux
> paramètres, trois règles. Il se lit en cinq minutes, en entier.
>
> Il est petit, et il porte quand même un vrai faux ami — de ceux qui ont l'air inoffensifs
> et donnent une réponse fausse à tous les coups.

---

## 1. Objectif et contexte

Un trajet est fait de segments. Chaque segment a une distance et une vitesse. On veut la
**vitesse moyenne sur l'ensemble du trajet**.

La réponse évidente est fausse. Faire la moyenne des vitesses des segments donne un nombre
auquel aucun véhicule n'a jamais roulé, et il est toujours trop optimiste : **on passe plus
de temps sur les segments lents, donc ils pèsent davantage.**

## 2. Périmètre

**Dans le périmètre.** Distance totale, durée totale et vitesse moyenne d'un trajet.

**Hors périmètre.** Les arrêts, le trafic, les limitations, la consommation, et toute
planification d'itinéraire.

## 3. Fonctions

| Id | Fonction | Rôle |
|---|---|---|
| <a id="fn-001"></a>`FN-001` | **Durée d'un segment** | Le temps passé sur un segment, d'après sa distance et sa vitesse |
| <a id="fn-002"></a>`FN-002` | **Vitesse moyenne du trajet** | La distance totale divisée par la durée totale |

---

## 4. Entrées

```
journey :
    legs : Sequence[Leg](1 .. 200)

Leg :
    distance : Length(km, > 0, 3 decimals)   [Scaled]
    speed    : Speed(km/h, > 0, 1 decimal)   [Scaled]
```

**Précondition :** toute `speed` est strictement positive (`E-SPD-001`).

## 5. Sorties

```
result :
    total_distance : Length(km, > 0, 3 decimals)     [Scaled]
    total_duration : Duration(h, > 0, 6 decimals)    [Real]
    average_speed  : Speed(km/h, > 0, P-02 decimals) [Real]
```

## 6. Paramètres

| Id | Nom | Valeur | Qui peut le changer | Date d'effet |
|---|---|---|---|---|
| <a id="p-01"></a>`P-01` | Mode d'arrondi de la vitesse publiée | `HALF_EVEN` | Responsable exploitation de flotte | 2026-01-01 |
| <a id="p-02"></a>`P-02` | Décimales de la vitesse publiée | 1 | Responsable exploitation de flotte | 2026-01-01 |

---

## 7. Règles

### 7.1 L'algorithme, en un seul morceau

```
DEFINE compute_average_speed(journey) : result

    PRECONDITIONS  E-SPD-001

    leg_duration   = ...                (RG-010, par segment)
    total_distance = ...                (RG-020)
    total_duration = ...                (RG-020)
    average_speed  = ...                (RG-030)

    RETURN result
```

### RG-010 — Durée d'un segment

Pour chaque segment :

```
leg_duration = distance ÷ speed
```

Les durées des segments **ne sont pas arrondies**. Seule la vitesse publiée l'est
(`RG-030`).

> **Pourquoi ça compte.** Arrondir chaque durée à la minute puis sommer décalerait le
> résultat sur un long trajet. L'endroit où l'on arrondit est une décision métier, et la
> voici : **une fois, à la fin**.

### RG-020 — Totaux du trajet

```
total_distance = SUM OF distance OVER legs
total_duration = SUM OF leg_duration OVER legs
```

### RG-030 — Vitesse moyenne

```
average_speed = ROUND( total_distance ÷ total_duration, P-02, P-01 )
```

> **C'est le faux ami, et c'est tout l'intérêt de cet exemple.** La vitesse moyenne n'est
> **pas** la moyenne des vitesses des segments.
>
> 60 km à 30 km/h puis 60 km à 60 km/h : la moyenne des vitesses donne 45 km/h, la vraie
> réponse est **40 km/h**. On passe deux heures sur le premier segment et une sur le
> second — le segment lent pèse deux fois plus.
>
> Les deux réponses coïncident seulement quand tous les segments durent le **même temps**,
> ce qui n'arrive presque jamais. Un développeur laissé à lui-même écrirait la moyenne
> arithmétique : c'est la lecture naturelle de « vitesse moyenne », et elle est fausse.

### Table de couverture

| Règle | Couverte par |
|---|---|
| `RG-010` | CT-01, CT-02, CT-03, CT-04 |
| `RG-020` | CT-01, CT-02, CT-03, CT-04 |
| `RG-030` | CT-01, CT-02, CT-03, CT-04 |

---

## 8. Invariants

| Id | Énoncé |
|---|---|
| <a id="inv-01"></a>`INV-01` | `average_speed` est comprise entre la vitesse du segment le **plus lent** et celle du **plus rapide**. Un résultat hors de cet intervalle signale une arithmétique fausse. |
| <a id="inv-02"></a>`INV-02` | Avant arrondi, `average_speed × total_duration = total_distance`. |

> `INV-01` est le contrôle bon marché qui attrape l'erreur que cette spécification existe
> pour empêcher — et il se teste sur des trajets **engendrés**, pas seulement sur les
> quatre cas ci-dessous.

## 9. Cas d'erreur métier

| Id | Condition | Comportement |
|---|---|---|
| <a id="e-spd-001"></a>`E-SPD-001` | Un segment a une vitesse `≤ 0` | Rejet. Une vitesse nulle signifie une durée infinie : il n'y a pas de moyenne à publier |

---

## 10. Jeu d'essai

### CT-01 — Distances égales, vitesses différentes

| Segment | Distance | Vitesse | Durée |
|---|---|---|---|
| 1 | 60 km | 30,0 km/h | 2,000000 h |
| 2 | 60 km | 60,0 km/h | 1,000000 h |

120 km ÷ 3 h → **`average_speed` = 40,0 km/h**

**La moyenne des vitesses donnerait 45,0.** Ce cas à lui seul sépare une implémentation
correcte de l'erreur naturelle.

### CT-02 — Durées égales

| Segment | Distance | Vitesse | Durée |
|---|---|---|---|
| 1 | 30 km | 30,0 km/h | 1,000000 h |
| 2 | 60 km | 60,0 km/h | 1,000000 h |

90 km ÷ 2 h → **`average_speed` = 45,0 km/h**

Ici les deux méthodes **coïncident**. Le cas est gardé exprès : il montre qu'une
implémentation fausse peut passer un test plausible, et que c'est CT-01 qui travaille.

### CT-03 — Un seul segment

100 km à 80,0 km/h → 1,250000 h → **`average_speed` = 80,0 km/h**

### CT-04 — Arrondi

| Segment | Distance | Vitesse | Durée |
|---|---|---|---|
| 1 | 10 km | 7,0 km/h | 1,428571 h |
| 2 | 10 km | 13,0 km/h | 0,769231 h |

20 km ÷ 2,197802 h = 9,10000… → **`average_speed` = 9,1 km/h**

### CT-05 — Vitesse nulle

Un segment à 0,0 km/h → rejeté par `E-SPD-001`.

### Provenance et validation

| | |
|---|---|
| **Provenance** | Calculés à la main en arithmétique décimale exacte, indépendamment de toute implémentation |
| **Comment ils ont été examinés** | Chaque durée, total et quotient a été recalculé, et chaque cas confronté à la moyenne arithmétique des vitesses pour savoir s'il discrimine |
| **Ce que l'examen a produit** | CT-02 a été ajouté **parce qu'il ne discrimine pas** : sans lui, personne ne verrait que passer trois cas ne prouve rien |
| **Où ils vivent** | [`../code/src/test/resources/reference-data.csv`](../code/src/test/resources/reference-data.csv) |
| **Validé par** | Responsable exploitation de flotte, 2026-08-22 |

---

## 11. Contraintes et exigences

| Id | Énoncé | Source | Propriétaire | Vérification |
|---|---|---|---|---|
| `EX-01` | Distances et vitesses sont exactes telles que saisies ; seule la vitesse publiée est arrondie | Contrat §4, §5 | Architecture SI | Revue de conception |
| `EX-02` | Un trajet compte au plus 200 segments ; le calcul est appelé au plus 50 fois par seconde | Mesure d'exploitation | Responsable exploitation | Tir de charge |

## 12. Questions ouvertes

| Id | Question | Décideur | Échéance | Statut |
|---|---|---|---|---|
| `Q-01` | Faut-il compter les arrêts dans la durée ? Aujourd'hui ils sont hors périmètre, donc la vitesse publiée est une moyenne *en roulant* | Responsable exploitation de flotte | 2026-12-01 | Ouverte |

## 13. Historique et notices de changement

| Version | Date | Changement | Impact sur les résultats | Notice |
|---|---|---|---|---|
| 1.0.0 | 2026-08-22 | Version initiale | — | — |

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
