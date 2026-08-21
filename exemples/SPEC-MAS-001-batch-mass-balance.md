# SPEC-MAS-001 — Batch mass balance

| | |
|---|---|
| **Identifiant** | SPEC-MAS-001 |
| **Version** | 1.0.0 |
| **Statut** | Validée |
| **Valideur métier** | Responsable formulation |
| **Co-auteur technique** | Équipe logiciel procédés |
| **Glossaire de référence** | [GLOSSAIRE.md](../GLOSSAIRE.md) v1.0.0 |

> **Pourquoi cet exemple.** Il est le pendant de
> [SPEC-THM-001](SPEC-THM-001-refroidissement.md). Là, des grandeurs **continues**, une
> équation différentielle, un résultat **approché** dont on discute la justesse. Ici, des
> grandeurs **discrètes et exactes** : une masse pesée existe au pas de la balance, pas
> en deçà. Le résultat n'est pas approché, il est **juste ou faux** — et la conservation
> de la masse en fait une propriété vérifiable, pas une convention.
>
> Les faux amis n'y sont pas les mêmes, et c'est tout l'intérêt de garder les deux.

---

## 1. Objectif et contexte

Un lot est préparé en dosant plusieurs composants selon des **fractions massiques
cibles**. Chaque composant est pesé sur une balance dont la **résolution** est finie :
on ne peut pas déposer 0,333333 kg, seulement un multiple du pas de la balance.

Arrondir chaque dose indépendamment fait que **leur somme ne retombe pas sur la masse du
lot**. L'écart est infime — un pas de balance — mais il est réel : la masse du lot
préparé ne serait pas celle annoncée, et la somme des fractions ne vaudrait plus 1.

La fonction calcule, pour chaque composant, la **masse à peser**, telle que leur somme
égale **exactement** la masse cible du lot.

---

## 2. Périmètre

**Dans le périmètre.** Le calcul des masses à peser à partir des fractions cibles, leur
arrondi au pas de la balance, et l'affectation du résidu.

**Hors périmètre.** Le pilotage de la balance, l'ordre physique des pesées, la
traçabilité des lots de matière première, le contrôle qualité du mélange obtenu.

---

## 3. Glossaire

| Terme | Définition |
|---|---|
| **Lot** (*batch*) | La quantité de produit préparée en une fois, de masse cible déclarée. |
| **Composant** (*component*) | L'une des matières entrant dans le lot, identifiée et dosée. |
| **Fraction massique cible** (*target mass fraction*) | La part de la masse du lot que doit représenter un composant. Sans dimension, entre 0 et 1. La somme des fractions vaut exactement 1. |
| **Pas de la balance** (*balance step*) | La plus petite différence de masse que la balance distingue. Toute masse à peser en est un multiple entier. |
| **Résidu** (*residual*) | L'écart entre la masse cible du lot et la somme des masses arrondies. Il vaut au plus quelques pas de balance, et n'est jamais ignoré. |

---

## 4. Entrées

```
request :
    target_batch_mass : Mass(kg, > 0, 3 decimals)
    balance_step      : Mass(kg, > 0, 3 decimals)
    components        : List[Component](1 .. 50)

Component :
    component_id         : Identifier(text, 3 to 20 characters, unique)
    target_mass_fraction : Ratio(dimensionless, 6 decimals, 0.000000 .. 1.000000)
```

**Préconditions :**
- `SUM OF target_mass_fraction OVER components = 1.000000`, exactement (`E-MAS-001`).
- Deux composants ne portent jamais le même `component_id` (`E-MAS-003`).
- `target_batch_mass` est un multiple entier de `balance_step` (`E-MAS-004`).

---

## 5. Sorties

```
result :
    dispensed : List[Dispensed]
    residual  : Mass(kg, 3 decimals)          -- signé, avant affectation

Dispensed :
    component_id   : Identifier(text, 3 to 20 characters)
    nominal_mass   : Mass(kg, ≥ 0, 9 decimals)   -- avant arrondi, pour la traçabilité
    dispensed_mass : Mass(kg, ≥ 0, 3 decimals)   -- ce qui est effectivement pesé
```

---

## 6. Paramètres

| Id | Nom | Valeur | Unité | Qui peut le changer | Date d'effet |
|---|---|---|---|---|---|
| `P-01` | Mode d'arrondi des doses | `HALF_EVEN` | — | Responsable formulation | 2026-01-01 |
| `P-02` | Résidu maximal toléré, en pas de balance | 3 | pas | Responsable formulation | 2026-01-01 |

> **`P-01` vaut `HALF_EVEN`, et ce n'est pas un détail.** Sur un grand nombre de lots,
> l'arrondi au plus proche « moitié vers le haut » biaise systématiquement les masses
> vers le haut. `HALF_EVEN` répartit les cas d'égalité et annule ce biais. C'est une
> décision métier : elle change les résultats.

---

## 7. Règles

### RG-010 — Masse nominale d'un composant

```
FOR EACH component IN components
    nominal_mass = target_batch_mass × component.target_mass_fraction
END FOR
```

La masse nominale est conservée en sortie, **non arrondie** : c'est elle qui permet de
rejouer le calcul et d'expliquer un écart.

### RG-020 — Arrondi au pas de la balance

```
FOR EACH component IN components
    LET steps = ROUND( nominal_mass ÷ balance_step, 0, P-01 )
    rounded_mass = steps × balance_step
END FOR
```

> **On arrondit un nombre de pas, pas une masse.** Écrire
> `ROUND(nominal_mass, 3, P-01)` ne serait juste que si le pas valait exactement
> 0,001 kg. Une balance à 5 g existe : la dose doit alors être un multiple de 0,005 kg,
> ce qu'un arrondi à trois décimales ne garantit pas.

### RG-030 — Résidu

```
LET dispensed_total = SUM OF rounded_mass OVER components
residual = target_batch_mass − dispensed_total
```

Le résidu est **signé** : positif s'il manque de la matière, négatif s'il y en a trop.

### RG-040 — Recevabilité du résidu

```
IF |residual| > P-02 × balance_step THEN
    RAISE ERROR E-MAS-002 "residual exceeds the tolerated number of balance steps"
ELSE
    the residual is allocated by RG-050
END IF
```

> **Pourquoi une borne.** Un résidu de quelques pas est la conséquence normale de
> l'arrondi. Un résidu plus grand signale autre chose — des fractions incohérentes, un
> pas de balance inadapté au lot — et l'absorber silencieusement masquerait le défaut.

### RG-050 — Affectation du résidu

```
LET largest = MAXIMUM OF target_mass_fraction OVER components
LET eligible = FILTER components WHERE target_mass_fraction = largest
LET receiver = THE FIRST OF ( SORT eligible BY component_id ASCENDING )

FOR EACH component IN components
    IF component.component_id = receiver.component_id THEN
        dispensed_mass = rounded_mass + residual
    ELSE
        dispensed_mass = rounded_mass
    END IF
END FOR
```

> **Deux décisions métier, et une seule serait insuffisante.**
>
> **Sur qui.** Le résidu va au composant de plus grande fraction, parce que c'est là
> qu'il pèse le moins en écart relatif : un pas de balance sur 0,667 kg fausse la
> fraction de 0,15 %, sur 0,167 kg de 0,6 %.
>
> **En cas d'égalité.** Deux composants peuvent porter la même plus grande fraction.
> Sans règle, le résultat dépendrait de l'ordre de lecture des données — donc de
> l'implémentation, donc il ne serait pas reproductible. On retient le plus petit
> `component_id` dans l'ordre alphabétique : arbitraire, mais **déclaré et stable**.

### Table de couverture

| Règle | Couverte par |
|---|---|
| `RG-010` | CT-01, CT-02, CT-03, CT-04 |
| `RG-020` | CT-01, CT-02, CT-03, CT-04 |
| `RG-030` | CT-01, CT-02, CT-03, CT-04 |
| `RG-040` | CT-05 |
| `RG-050` | CT-01, CT-02, CT-03 |

---

## 8. Invariants

| Id | Énoncé |
|---|---|
| `INV-01` | `SUM OF dispensed_mass OVER dispensed = target_batch_mass`, **exactement**. C'est la conservation de la masse : elle ne tolère aucun écart, fût-il d'un pas. |
| `INV-02` | Chaque `dispensed_mass` est un multiple entier de `balance_step`. |
| `INV-03` | Chaque `dispensed_mass` est `≥ 0`. Un résidu négatif ne peut pas rendre une dose négative — si cela arrivait, `RG-040` aurait dû rejeter avant. |
| `INV-04` | Le résultat est **invariant par permutation** de la liste des composants : réordonner l'entrée ne change aucune masse pesée. C'est ce que garantit la règle de départage de `RG-050`. |

> `INV-04` se teste sur des entrées générées : on permute la liste au hasard et on
> vérifie que le résultat est identique. C'est le contrôle qui attrape une
> implémentation ayant « oublié » le départage — celle qui fonctionne sur tous les cas
> nominaux et se trompe un jour sur un lot dont les composants ont été saisis dans un
> autre ordre.

---

## 9. Cas d'erreur métier

| Id | Condition | Comportement |
|---|---|---|
| `E-MAS-001` | La somme des fractions cibles ne vaut pas exactement 1 | Rejet. Aucun résultat partiel n'est produit |
| `E-MAS-002` | Le résidu dépasse `P-02` pas de balance | Rejet, en indiquant le résidu constaté |
| `E-MAS-003` | Deux composants portent le même `component_id` | Rejet : le départage de `RG-050` ne serait pas défini |
| `E-MAS-004` | `target_batch_mass` n'est pas un multiple de `balance_step` | Rejet : la conservation exacte de `INV-01` serait impossible |

---

## 10. Jeu d'essai

Tous les cas prennent `balance_step = 0,001 kg` et `P-01 = HALF_EVEN`.

### CT-01 — Résidu positif, plus grande fraction unique

| Composant | Fraction cible | Masse nominale | Arrondie | **Pesée** |
|---|---|---|---|---|
| `CMP-A` | 0,333333 | 0,333333000 | 0,333 | **0,333** |
| `CMP-B` | 0,333333 | 0,333333000 | 0,333 | **0,333** |
| `CMP-C` | 0,333334 | 0,333334000 | 0,333 | **0,334** |

`target_batch_mass = 1,000` · somme des arrondis `= 0,999` · `residual = +0,001`
→ affecté à `CMP-C`, plus grande fraction. Somme finale **= 1,000**.

### CT-02 — Résidu négatif

| Composant | Fraction cible | Masse nominale | Arrondie | **Pesée** |
|---|---|---|---|---|
| `CMP-A` | 0,166667 | 0,166667000 | 0,167 | **0,167** |
| `CMP-B` | 0,166667 | 0,166667000 | 0,167 | **0,167** |
| `CMP-C` | 0,666666 | 0,666666000 | 0,667 | **0,666** |

`target_batch_mass = 1,000` · somme des arrondis `= 1,001` · `residual = −0,001`
→ retiré à `CMP-C`. Somme finale **= 1,000**.

### CT-03 — Ex æquo sur la plus grande fraction

| Composant | Fraction cible | Masse nominale | Arrondie | **Pesée** |
|---|---|---|---|---|
| `CMP-A` | 0,400000 | 0,400400000 | 0,400 | **0,401** |
| `CMP-B` | 0,400000 | 0,400400000 | 0,400 | **0,400** |
| `CMP-C` | 0,200000 | 0,200200000 | 0,200 | **0,200** |

`target_batch_mass = 1,001` · somme des arrondis `= 1,000` · `residual = +0,001`
→ `CMP-A` et `CMP-B` sont à égalité ; le départage retient `CMP-A`, plus petit
identifiant. Somme finale **= 1,001**.

**C'est le cas qui discrimine.** Une implémentation sans règle de départage donne ici
un résultat qui dépend de l'ordre de lecture — et passe pourtant CT-01, CT-02 et CT-04.

### CT-04 — Résidu nul

| Composant | Fraction cible | Masse nominale | Arrondie | **Pesée** |
|---|---|---|---|---|
| `CMP-A` | 0,500000 | 0,500000000 | 0,500 | **0,500** |
| `CMP-B` | 0,250000 | 0,250000000 | 0,250 | **0,250** |
| `CMP-C` | 0,250000 | 0,250000000 | 0,250 | **0,250** |

`residual = 0,000` : `RG-050` ne modifie aucune dose. Somme **= 1,000**.

### CT-05 — Résidu au-delà de la tolérance

`target_batch_mass = 1,000`, `balance_step = 0,100`, quinze composants de fraction
`0,066667` (somme `1,000005` ≠ 1) → rejet par `E-MAS-001` **avant** tout calcul.

Variante recevable en entrée mais rejetée au résidu : `balance_step = 0,100`,
`target_batch_mass = 1,000`, sept composants de fractions `0,142857 × 6` et `0,142858`.
Chaque dose nominale vaut ≈ 0,1429 kg, arrondie à 0,100 kg — somme 0,700 kg, résidu
0,300 kg, soit **3 pas**. Le résidu vaut exactement `P-02` pas : il est **accepté**
(`>` et non `≥`, `RG-040`). Avec `P-02 = 2`, le même cas serait rejeté par `E-MAS-002`.

### Provenance et validation

| | |
|---|---|
| **Provenance** | Calculs conduits en arithmétique décimale exacte, indépendamment de toute implémentation du composant |
| **Comment ils ont été examinés** | Chaque ligne a été recalculée : masse nominale, nombre de pas, arrondi `HALF_EVEN`, résidu, affectation. La somme finale a été confrontée à `INV-01` sur chaque cas |
| **Ce que l'examen a produit** | CT-03 a été construit **pour** faire apparaître l'égalité de fractions : sans lui, la règle de départage de `RG-050` n'était couverte par aucun cas |
| **Validé par** | Responsable formulation, 2026-08-21 |

---

## 11. Contraintes et exigences

| Id | Énoncé | Source | Propriétaire | Vérification |
|---|---|---|---|---|
| `EX-01` | Le calcul emploie une **arithmétique décimale exacte**. Le binaire flottant est exclu : 0,1 n'y est pas représentable, et `INV-01` exige une égalité stricte | Contrainte d'exactitude §11 | Architecture SI | Revue de conception |
| `EX-02` | Un lot comporte au plus 50 composants ; le calcul est appelé au plus 200 fois par heure | Capacité de l'atelier | Responsable production | Mesure en exploitation |
| `EX-03` | Le calcul est **rejouable à l'identique** : mêmes entrées, mêmes masses, indéfiniment. Aucune dépendance à l'horloge, à l'ordre de lecture ou à un aléa | Exigence de traçabilité `QUA-TRC-2` | Assurance qualité | Rejeu sur archive, trimestriel |
| `EX-04` | Les masses nominales sont conservées 10 ans avec le lot | `QUA-TRC-2` §4 | Assurance qualité | Audit d'archive |

---

## 12. Questions ouvertes

| Id | Question | Décideur | Échéance | Statut |
|---|---|---|---|---|
| `Q-01` | Faut-il répartir un résidu de plusieurs pas sur plusieurs composants, plutôt que de le porter entièrement sur le plus gros ? | Responsable formulation | 2026-10-01 | Ouverte |
| `Q-02` | Le départage alphabétique doit-il céder la place à un ordre de priorité déclaré par composant ? | Responsable formulation | | Fermée |

---

## 13. Historique et notices de changement

| Version | Date | Changement | Impact sur les résultats | Notice |
|---|---|---|---|---|
| 1.0.0 | 2026-08-21 | Version initiale | — | — |

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `SPEC-MAS-001` | `292e2c59-db4e-4dcd-b7e1-101cf6d76de0` | document | SPEC-MAS-001 — Batch mass balance |
| `RG-010` | `1f7dd670-3f98-4ea6-9bd1-04e2f61ad316` | règle | Masse nominale d'un composant |
| `RG-020` | `2c2489c2-702a-46dd-a7cd-1064022ddbd4` | règle | Arrondi au pas de la balance |
| `RG-030` | `ed9131d6-4472-4d21-b75d-fba1122da558` | règle | Résidu |
| `RG-040` | `8080dc01-878b-4657-909b-b2199ecf4b60` | règle | Recevabilité du résidu |
| `RG-050` | `c55d71da-977b-4b31-93a3-66c3370a00a1` | règle | Affectation du résidu |
| `CT-01` | `b3e154c8-b776-4ecb-86c7-fe1cc06173a9` | cas de test | Résidu positif, plus grande fraction unique |
| `CT-02` | `98d1b468-0e1f-4082-9f1a-43a47d0f1802` | cas de test | Résidu négatif |
| `CT-03` | `b660aa4f-dd37-4cd9-b967-798a11586fd2` | cas de test | Ex æquo sur la plus grande fraction |
| `CT-04` | `dfb6ff92-8efb-47e8-96c8-ca15b1704d70` | cas de test | Résidu nul |
| `CT-05` | `bdc923c8-9721-47cf-8b54-b77407f04bb1` | cas de test | Résidu au-delà de la tolérance |
| `P-01` | `4b92babc-dd3d-423e-8feb-046b7166db4e` | paramètre | Mode d'arrondi des doses |
| `P-02` | `cd6b8a1b-c171-4083-aa90-16fbfbffba9b` | paramètre | Résidu maximal toléré, en pas de balance |
| `EX-01` | `289af665-16cb-4113-86ea-9ff53ce1a4c5` | exigence | Le calcul emploie une **arithmétique décimale exacte**. Le binaire flo |
| `EX-02` | `525363e7-8bba-49e7-89e3-f0761903866b` | exigence | Un lot comporte au plus 50 composants ; le calcul est appelé au plus 2 |
| `EX-03` | `f265a3a3-f970-41c9-81ca-f27bb0b129cb` | exigence | Le calcul est **rejouable à l'identique** : mêmes entrées, mêmes masse |
| `EX-04` | `2c76616f-cdf7-439f-88a6-107cd43b3d24` | exigence | Les masses nominales sont conservées 10 ans avec le lot |
| `INV-01` | `dbccfecf-777b-4959-a321-7cb6555461cf` | invariant | `SUM OF dispensed_mass OVER dispensed = target_batch_mass`, **exacteme |
| `INV-02` | `60bffc03-1312-4d2a-94b9-152a5f0f18e6` | invariant | Chaque `dispensed_mass` est un multiple entier de `balance_step`. |
| `INV-03` | `14aa1bd5-f615-42c4-a9cb-3d2c078c1e0b` | invariant | Chaque `dispensed_mass` est `≥ 0`. Un résidu négatif ne peut pas rendr |
| `INV-04` | `9eb759d5-63d4-4b94-8f80-dd07b8d47340` | invariant | Le résultat est **invariant par permutation** de la liste des composan |
| `E-MAS-001` | `694d1152-ae43-44ea-9cee-1a9226214548` | cas d'erreur | La somme des fractions cibles ne vaut pas exactement 1 |
| `E-MAS-002` | `1ae84506-b8e5-48fc-b902-b166a80b4b30` | cas d'erreur | Le résidu dépasse `P-02` pas de balance |
| `E-MAS-003` | `c1c93cb0-ddf8-40e4-820d-e702b518f1fe` | cas d'erreur | Deux composants portent le même `component_id` |
| `E-MAS-004` | `f12f64b6-8041-4729-91ec-92cdee0b1e71` | cas d'erreur | `target_batch_mass` n'est pas un multiple de `balance_step` |
| `Q-01` | `9111352a-45f3-4b93-8115-3102c370b556` | question | Faut-il répartir un résidu de plusieurs pas sur plusieurs composants,  |
| `Q-02` | `c16913b4-c54a-41ba-861a-7c369e06a149` | question | Le départage alphabétique doit-il céder la place à un ordre de priorit |
