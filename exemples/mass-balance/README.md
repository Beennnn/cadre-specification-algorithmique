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
| **1** | [Le besoin](NEED.md) | Ce que le métier dit, dans ses mots, avant toute formalisation — et la liste des questions qu'il ne tranche pas encore | Métier |
| **2** | [La spécification](spec/SPEC-MAS-001.en.md) | Chaque question reçoit une réponse numérotée, justifiée et versionnée. Contrat typé, règles, invariants, jeu d'essai calculé à la main | Métier, avec un co-auteur technique |
| **3** | [Le contrat](CONTRACT.md) | Ce qu'un développeur déduit du contrat, et la correspondance vers Java — mécanique, déclarée une fois | Technique |
| **4** | [Le code](code/) | L'implémentation annotée règle par règle, et le harnais qui la qualifie contre les données de référence | Technique |
| **4 bis** | [Les cas de test](tests/) | Un document par cas : ce qu'il attrape, et ce qu'il laisse passer | Métier et technique |
| **4 ter** | [Les rapports](reports/) | Le rapport de test et le rapport de couverture spec ↔ code, tous deux engendrés | Engendrés |
| **5** | [Les écarts](DEVIATIONS.md) | Le métier juge les écarts au regard de ses tolérances — et décide ce qu'on en fait | Métier, avec le support de la technique |

---

## Exécuter le code

Deux commandes, aucune dépendance, aucun gestionnaire de construction :

```bash
cd code && javac -encoding UTF-8 -d /tmp/mb $(find src -name '*.java')
cd .. && java -cp /tmp/mb method.massbalance.MassBalanceTest \
    code/src/test/resources/reference-data.csv spec/SPEC-MAS-001.en.md reports/TEST-REPORT.md
```

Le harnais écrit [`reports/TEST-REPORT.md`](reports/TEST-REPORT.md) et rend `1` si un cas
est en écart : utilisable en intégration continue tel quel. Le rapport se termine par deux
tables que peu de suites de tests produisent — **ce que la spécification exige et que la
course n'a pas exercé**, et **quels paramètres un cas décide réellement** :

```
| `CT-05` | OK | 7 doses · residual 0.300 kg (3 step(s)) · INV-01 to INV-03 hold
                 · **decides `P-02`** — the residual sits exactly on the bound |
| `CT-10` | OK | 3 doses · residual 0.001 kg (1 step(s)) · INV-01 to INV-03 hold
                 · **decides `P-01`** — a nominal mass falls on a half-step |
```

Le rapport de couverture s'engendre à part, et relie chaque point de la spécification au
fichier et à la ligne qui le mettent en œuvre :

```bash
java outils/Couverture.java exemples/mass-balance     # → reports/COVERAGE-REPORT.md
```

> **Les deux cas ci-dessus n'existaient pas en v1.0.0.** Le rapport de couverture a montré
> que `E-MAS-002` — le rejet d'un résidu trop grand — n'était exercé par aucun cas, et
> qu'aucune masse nominale ne tombait jamais sur un demi-pas, si bien que `P-01` était
> validé, daté, implémenté et **arbitré par rien**. `CT-09` et `CT-10` ont été construits
> pour cela.

---

## Du pseudo-code au code, côte à côte

C'est le passage que toute la méthode organise. À gauche ce que le métier écrit, à droite
ce que le développeur en fait — et rien de plus.

**`RG-020` — arrondi au pas de la balance**, tel qu'il figure au §7 de la
[spécification](spec/SPEC-MAS-001.en.md) :

```
FOR EACH component IN components
    LET steps = ROUND( nominal_mass ÷ balance_step, 0, P-01 )
    rounded_mass = steps × balance_step
END FOR
```

Et son implémentation dans [`MassBalance.java`](code/src/main/java/method/massbalance/MassBalance.java) :

```java
/**
 * RG-020 — rounding to the balance step.
 * What is rounded is a NUMBER OF STEPS, not a mass: rounding to three decimals
 * would be wrong as soon as the step is not 0.001 kg.
 */
@ImplementsSpec("RG-020")
private static BigDecimal roundedMass(BigDecimal nominal, BigDecimal balanceStep) {
    BigDecimal steps = nominal.divide(balanceStep, 0, P_01_ROUNDING_MODE);
    return steps.multiply(balanceStep);
}
```

**Ce qui se transporte à l'identique** : la structure du calcul, le nom des grandeurs
(`nominal_mass` → `nominalMass`, mécaniquement), le paramètre `P-01` — qui reste une
constante nommée et ne se retrouve jamais en valeur littérale au milieu d'une expression.

**Ce que le développeur ajoute, et qui n'a rien à faire dans la spécification** : le type
`BigDecimal`, la signature, la visibilité, le fait que ce soit une méthode privée plutôt
qu'une boucle en ligne.

**Ce qu'il n'a pas le droit de changer** : arrondir le nombre de pas plutôt que la masse.
C'est écrit dans la règle, c'est justifié juste en dessous, et l'inverser donnerait des
résultats faux sur toute balance dont le pas n'est pas 0,001 kg — sans qu'aucun des cas de
référence à pas fin ne le détecte.

> **L'annotation porte l'identifiant de la règle.** Partant de `RG-020`, on trouve le
> code ; partant du code, on retrouve la règle **et sa justification**. C'est ce qui
> permet, six mois plus tard, de répondre à « pourquoi c'est écrit comme ça ? » autrement
> que par une reconstitution — et c'est aussi ce que lit
> [`Couverture.java`](../../outils/Couverture.java) pour produire
> [le rapport de couverture](reports/COVERAGE-REPORT.md), qui lie les deux sens.
>
> Le code et les tests sont **en anglais**, comme tout ce qui s'exécute dans ce dépôt ; la
> spécification, elle, existe [en anglais](spec/SPEC-MAS-001.en.md) — la version qui fait
> foi — et [en français](spec/SPEC-MAS-001.fr.md).

---

## Les trois choses que cet exemple démontre

**1. Le harnais ne contient aucune valeur attendue.** Il rejoue
[`reference-data.csv`](code/src/test/resources/reference-data.csv), qui est le §10 de la
spécification transcrit sans interprétation. C'est ce qui en fait une **qualification** et
non un test écrit par celui qui a codé : un test dont les valeurs attendues sortent de
l'implémentation ne vérifie rien.

**2. Un invariant se teste sur des entrées engendrées.** `INV-04` — l'invariance par
permutation — rejoue 200 ordres différents. C'est le contrôle qui attrape une
implémentation ayant « oublié » le départage de `RG-050` : elle passe **tous** les cas
nominaux et se trompe le jour où les composants arrivent dans un autre ordre.

**2 bis. Et un invariant ne remplace pas un jeu de référence.** `CT-03` et `CT-10` sont
tous deux passés par `INV-01` à `INV-04` **alors que les masses pesées sont fausses** :
une allocation dépendante de l'ordre et un mode d'arrondi erroné produisent des fiches
différentes qui conservent toutes la masse. Ce qui les attrape, ce sont les **masses
validées par le métier**. Les deux dispositifs se complètent, aucun ne se substitue à
l'autre.

**3. Un écart n'est pas forcément un bogue.** L'analyse de [l'étape 5](DEVIATIONS.md) conclut
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
