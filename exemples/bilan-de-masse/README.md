# Le bilan de masse d'un lot — la chaîne complète

**C'est le seul exemple du dépôt qui va du besoin jusqu'au code exécuté.** Les autres
s'arrêtent à la spécification ; celui-ci montre aussi ce qui se passe après la passation —
le contrat traduit, le code écrit, les données de référence rejouées, et l'écart analysé.

Le cas est volontairement petit : doser les composants d'un lot sur une balance de
résolution finie. Assez simple pour être lu en entier, assez piégeux pour porter cinq faux
amis.

---

## Le parcours

| | Document | Ce que l'étape produit | Qui la tient |
|---|---|---|---|
| **1** | [Le besoin](1-BESOIN.md) | Ce que le métier dit, dans ses mots, avant toute formalisation — et la liste des questions qu'il ne tranche pas encore | Métier |
| **2** | [La spécification](2-SPEC-MAS-001-batch-mass-balance.md) | Chaque question reçoit une réponse numérotée, justifiée et versionnée. Contrat typé, règles, invariants, jeu d'essai calculé à la main | Métier, avec un co-auteur technique |
| **3** | [Le contrat](3-CONTRAT.md) | Ce qu'un développeur déduit du contrat, et la correspondance vers Java — mécanique, déclarée une fois | Technique |
| **4** | [Le code](4-code/) | L'implémentation annotée règle par règle, et le harnais qui la qualifie contre les données de référence | Technique |
| **5** | [Les écarts](5-ECARTS.md) | Le métier juge les écarts au regard de ses tolérances — et décide ce qu'on en fait | Métier, avec le support de la technique |

---

## Exécuter le code

Deux commandes, aucune dépendance, aucun gestionnaire de construction :

```bash
cd exemples/bilan-de-masse/4-code
javac -d /tmp/mb *.java && java -cp /tmp/mb MassBalanceTest
```

```
Qualification de MassBalance contre donnees-de-reference.csv

  OK      CT-01    3 doses, résidu 0.001, INV-01 et INV-02 tenus
  OK      CT-02    3 doses, résidu -0.001, INV-01 et INV-02 tenus
  OK      CT-03    3 doses, résidu 0.001, INV-01 et INV-02 tenus
  OK      CT-04    3 doses, résidu 0.000, INV-01 et INV-02 tenus
  OK      CT-05    7 doses, résidu 0.300, INV-01 et INV-02 tenus
  OK      CT-06    rejet attendu E-MAS-001
  OK      CT-07    rejet attendu E-MAS-003
  OK      CT-08    rejet attendu E-MAS-004
  OK      INV-04   200 permutations, résultat identique

Tous les cas de référence sont satisfaits.
```

Code de retour `1` si un cas est en écart : utilisable en intégration continue tel quel.

---

## Les trois choses que cet exemple démontre

**1. Le harnais ne contient aucune valeur attendue.** Il rejoue
[`donnees-de-reference.csv`](4-code/donnees-de-reference.csv), qui est le §10 de la
spécification transcrit sans interprétation. C'est ce qui en fait une **qualification** et
non un test écrit par celui qui a codé : un test dont les valeurs attendues sortent de
l'implémentation ne vérifie rien.

**2. Un invariant se teste sur des entrées engendrées.** `INV-04` — l'invariance par
permutation — rejoue 200 ordres différents. C'est le contrôle qui attrape une
implémentation ayant « oublié » le départage de `RG-050` : elle passe **tous** les cas
nominaux et se trompe le jour où les composants arrivent dans un autre ordre.

**3. Un écart n'est pas forcément un bogue.** L'analyse de [l'étape 5](5-ECARTS.md) conclut
que le code est conforme et que **la spécification est en défaut**. La correction remonte
donc au document, versionnée et motivée — pas dans un correctif local.

---

## Ce que le code décide, et ce qu'il ne décide pas

C'est la frontière de [CADRE §1.4](../../CADRE.md), rendue concrète :

| Tranché par la spécification | Tranché par le développeur |
|---|---|
| Le mode d'arrondi (`P-01`), et pourquoi `HALF_EVEN` | `BigDecimal` plutôt qu'un entier de pas |
| Qui reçoit le résidu, et le départage en cas d'égalité | Des `record` plutôt que des classes |
| La borne du résidu, et qu'elle soit `>` et non `≥` | Un seul passage de calcul plutôt que deux |
| Ce qu'on fait d'une recette invalide | L'ordre de parcours, la structure des collections |

Le code porte ces choix **en commentaire à côté de la règle qu'il applique** : partant
d'une règle, on trouve le code ; partant du code, on retrouve la règle et sa justification.

---

← [Retour aux exemples](../README.md)
