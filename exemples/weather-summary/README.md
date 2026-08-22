# Le relevé d'une station météo — le pseudo-langage au travail

**C'est l'exemple à ouvrir pour juger le pseudo-langage.** Son algorithme intégré fait
65 lignes et emploie **33 des 35 éléments** du lexique — non par étalage, mais parce que le
calcul en a besoin : un filtrage, une boucle réellement itérative et bornée, des
statistiques, un regroupement en suites consécutives, et une table de décision.

Le cas : une station enregistre des températures ; on publie en fin de journée la moyenne,
les extrêmes et les épisodes de gel. Deux choses le compliquent — les capteurs tombent en
panne, et une sonde au soleil affiche 45 °C en janvier.

---

## Le petit projet

| | | |
|---|---|---|
| **La spécification** | [en anglais](spec/SPEC-WTH-001.en.md) — fait foi · [en français](spec/SPEC-WTH-001.fr.md) | Métier, avec un co-auteur technique |
| **Les cas de test** | [`tests/`](tests/) — un document par cas : ce qu'il attrape, et ce qu'il laisse passer | Métier et technique |
| **Le code** | [`code/`](code/) — disposition Maven, annoté `@ImplementsSpec` règle par règle | Technique |
| **Les rapports** | [test](reports/TEST-REPORT.md) · [couverture](reports/COVERAGE-REPORT.md) | Engendrés |

```bash
cd code && javac -encoding UTF-8 -d /tmp/wth $(find src -name '*.java')
cd .. && java -cp /tmp/wth method.weathersummary.WeatherSummaryTest \
    code/src/test/resources/reference-data.csv spec/SPEC-WTH-001.en.md reports/TEST-REPORT.md

java outils/Couverture.java exemples/weather-summary        # depuis la racine du dépôt
```

---

## Ce que cet exemple montre et que les autres ne montrent pas

### La boucle peut s'épuiser, et ce n'est pas une erreur

Le rejet des aberrantes est réellement itératif : retirer une valeur change la moyenne, ce
qui peut en révéler une autre. Après `P-02` tours sans convergence, le relevé **est publié
quand même**, étiqueté `SUSPECT`. La non-convergence est délibérément absente de la table
des erreurs : c'est un résultat à qualifier, pas un échec à lever.

`CT-04` a été *construit* pour épuiser la boucle — aucun jeu naturel ne le faisait.

### Couvrir une règle n'est pas arbitrer un paramètre

Six paramètres gouvernent ce calcul. Un cas qui traverse `RG-040` le « couvre » au sens
comptable sans rien décider de `P-04` : si la moyenne vaut 0,5 dans tous les cas, le mode
d'arrondi n'a jamais été consulté.

Le harnais répond à la vraie question en **rejouant chaque cas avec un paramètre modifié**
et en rapportant lesquels bougent :

```
| `P-04` | rounding mode HALF_EVEN → HALF_UP | CT-07 |
| `P-06` | frost threshold 0.0 → 0.5 °C      | CT-01, CT-02, CT-04, CT-07 |
```

Avant la v1.1.0, cette table portait « **aucun cas** » en face de `P-04`.

### Un invariant et un jeu de référence attrapent des choses différentes

`CT-07` publie une moyenne qui tombe **pile sur l'égalité d'arrondi** : 0,25 °C. C'est le
seul cas où `P-04` décide de quelque chose — et, une fois écrit, il ne tenait pas en place.
Il publiait 0,2 ou 0,3 **selon l'ordre d'arrivée des lectures**.

La cause n'est pas le mode d'arrondi : l'addition en double précision n'est pas
associative. Sommées dans un ordre, les huit lectures donnent 2,0 ; dans un autre,
2,0000000000000004. Divisé par huit, l'un est sur l'égalité et l'autre juste au-dessus.

> **C'était un défaut de `EX-01`, pas du code.** L'exigence disait que la double précision
> suffisait parce que les lectures portent une incertitude de ±0,2 °C, très au-dessus du
> bruit numérique. Ce raisonnement vaut pour un nombre qu'on **compare** ; il tombe pour un
> nombre qu'on **arrondit**, parce que l'arrondi n'a aucune tolérance : il transforme un
> écart de 10⁻¹⁶ en un écart de 0,1 °C sur la valeur publiée.
>
> Il a fallu **les deux dispositifs** pour le trouver : la propriété de permutation, qui
> teste sur des ordres engendrés, et le cas sur l'égalité, qui est le seul endroit où cette
> propriété a des dents. Aucun des deux seul ne voyait rien.

### Trois détails qui décident d'un résultat

**Une borne stricte porte une conséquence métier.** Une lecture à exactement 0,0 °C n'est
pas du gel. Avec `≤`, le réseau alerterait chaque nuit d'automne qui frôle zéro.

**`ABSENT` remplace la convention `−999`.** Les chaînes d'acquisition encodent
traditionnellement une lecture manquante par un nombre impossible. Cette convention a
produit assez d'hivers à −999 °C pour être bannie.

**`CT-01` et `CT-02` publient la même moyenne, 0,5 °C.** C'est la preuve que le rejet a
fonctionné : une implémentation qui sauterait `RG-030` publierait 6,1 °C pour `CT-01` — et
passerait pourtant tous les autres cas.

---

← [Retour aux exemples](../README.md)
