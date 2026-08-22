# Le pseudo-langage

*Une trentaine de mots, tous en anglais, et la liste est fermée. Cette page les donne tous,
dit pourquoi ils sont si peu nombreux, et ce qu'on a délibérément laissé dehors.*

> **Ce n'est pas un langage de programmation simplifié.** C'est du français discipliné, avec
> juste assez de mots-clés pour qu'une phrase ne puisse pas se lire de deux façons. Personne
> ne l'exécute, aucun outil ne le compile : il est lu par des humains et contrôlé par un
> script qui vérifie sa cohérence, pas sa syntaxe.

---

## Les quatre principes

### 1. On écrit en pseudo-code exactement ce dont une lecture différente changerait un résultat

Tout le reste s'écrit en français. Une spécification n'est pas un programme raté : le
pseudo-code n'y est qu'un **outil de désambiguïsation**, et là où il n'y a rien à
désambiguïser, il alourdit sans rien apporter.

| S'écrit **en pseudo-code**, toujours | S'écrit **en prose**, et c'est mieux |
|---|---|
| Une **formule** de calcul | Un parcours dont l'itération ne porte aucune décision |
| Un **arrondi** : à quelle étape, combien de décimales, quel mode | Un enchaînement que la chaîne de traitement donne déjà |
| Une **branche** dont les deux issues diffèrent | Une agrégation évidente, dite par une opération d'ensemble |
| Un **départage**, une **borne** `≥` / `>` | Le contexte, l'intention, la justification d'un choix |

**La forme à préférer est mixte** : une phrase qui dit l'intention, puis la formule.

> La prose remplace la **cérémonie**, jamais la **décision**. « On arrondit convenablement »,
> « on prend le meilleur », « on gère les cas particuliers » restent des défauts (`C-41`).
> Le test ne change pas : si deux développeurs honnêtes peuvent en tirer deux résultats
> différents, il fallait du pseudo-code.
>
> → Le détail, avec l'exemple de la boucle inutile : [CADRE §2.5](CADRE.md).

### 2. Les mots-clés sont en anglais, la prose dans la langue de l'équipe

Le pseudo-code et les noms de grandeurs traversent la frontière métier / technique et
finissent lus par des développeurs, des relecteurs et des outils qui travaillent en anglais.
Le commentaire qui explique *pourquoi* une règle est ainsi reste dans la langue où on le
pense le mieux.

### 3. On décrit un résultat, pas un parcours

C'est ce qui **libère l'architecture**. Formulé en résultat, le développeur peut choisir une
somme en mémoire, une agrégation en base ou un cache incrémental — aucun de ces choix ne
peut trahir la règle. Formulé en parcours, il croit devoir reproduire la boucle.

| ✗ On impose un parcours | ✓ On décrit un résultat |
|---|---|
| `POUR i DE 1 À n : total = total + segment[i].energie` | `LET total = SUM OF energie OVER segments` |

**Chaque fois qu'une opération d'ensemble remplace une boucle, on la préfère.**

### 4. Le lexique est fermé

Tout mot qui n'y figure pas n'est pas un mot-clé. On n'en ajoute un que si son absence a
causé une ambiguïté réelle — jamais par confort. Un lexique qu'on ne peut pas mémoriser en
une lecture n'est pas adopté.

---

## Le lexique


### Structure

| Mot-clé | Rôle |
|---|---|
| `DEFINE name(param : Type) : Type` | déclarer une fonction |
| `INPUTS` · `OUTPUTS` | le contrat |
| `PRECONDITIONS` · `POSTCONDITIONS` · `INVARIANTS` | ce qu'on exige, garantit, maintient |
| `LET name = …` | **introduire** un nom — jamais le réaffecter ([CADRE §2.4](CADRE.md)) |
| `RETURN …` | produire le résultat |
| `RAISE ERROR E-xxx "…"` | signaler une erreur **métier** |

### Conditions

| Mot-clé | Rôle |
|---|---|
| `IF … THEN` · `ELSE IF … THEN` · `ELSE` · `END IF` | l'alternative |

Un `IF` a **toujours** son `ELSE` (`C-08`). Au-delà de deux conditions combinées, on
n'imbrique pas : on écrit une **table de décision** ([CADRE §2.6](CADRE.md)).

### Itération

| Mot-clé | Rôle |
|---|---|
| `FOR EACH x IN c … END FOR` | parcourir sans indice |
| `WHILE cond … END WHILE` | itérer — **à justifier**, avec critère d'arrêt, maximum d'itérations et comportement en cas de non-convergence (`C-13`) |

### Ensembles — le style à préférer

| Mot-clé | Rôle |
|---|---|
| `SUM` · `MEAN` · `MINIMUM` · `MAXIMUM` · `COUNT` | agréger |
| `SUM OF x OVER c` | agréger une grandeur sur une collection |
| `FILTER c WHERE cond` | restreindre |
| `SORT c BY a ASCENDING \| DESCENDING` | ordonner, avec départage explicite |
| `GROUP c BY criterion` | partitionner |
| `THE FIRST` · `THE LAST` | extraire d'une collection **ordonnée** |
| `THERE EXISTS` · `NONE` · `ALL` | quantifier |

**Chaque fois qu'une opération d'ensemble remplace une boucle, on la préfère** : elle
décrit un résultat et laisse le développeur libre de son chemin ([CADRE §2.5](CADRE.md)).

### Opérateurs et valeurs

| | |
|---|---|
| Arithmétique | `+` `−` `×` `÷` `^` |
| Comparaison | `=` `≠` `<` `≤` `>` `≥` · `BETWEEN a AND b` |
| Logique | `AND` · `OR` · `NOT` |
| Appartenance | `IN` |
| Valeurs | `TRUE` · `FALSE` · `ABSENT` |
| Fonction imposée | `ROUND(value, decimals, mode)` |

`ABSENT` désigne **l'absence métier** d'une valeur facultative. Ce n'est pas un `null`
technique : son traitement est une décision métier, déclarée (`C-12`).

Les fonctions mathématiques usuelles — racine, exponentielle, logarithme, sinus, arc
tangente — s'écrivent avec leur notation habituelle et ne sont pas des mots-clés.

### Ce que le lexique n'a pas, et pourquoi

| Absent | À la place | Pourquoi |
|---|---|---|
| `SWITCH` / `CASE` | une **table de décision** | elle rend la complétude vérifiable, un `SWITCH` non |
| `BREAK` · `CONTINUE` · `GOTO` | un critère d'arrêt déclaré | une sortie anticipée cache une condition qui n'a pas été écrite |
| `NULL` · `NIL` | `ABSENT` | l'absence est une notion métier, pas une valeur technique |
| `+=` · `++` · toute réaffectation | un **nouveau nom** | l'immutabilité ([CADRE §2.4](CADRE.md)) |
| `TRY` / `CATCH` | `RAISE ERROR` | la spécification dit **quelle erreur métier**, pas comment elle se propage |
| Fonctions anonymes, pointeurs, généricité, héritage | rien | ce sont des moyens d'implémentation |

---

## Le lexique au travail

Un extrait réel — le rejet itératif des températures aberrantes d'une station météo. Neuf
mots-clés en dix-sept lignes, et chacun y est parce que le calcul en a besoin :

```
    -- RG-030 · rejet itératif des aberrantes, borné par P-02
    LET retained = usable
    LET round_number = 0
    LET converged = FALSE

    WHILE NOT converged AND round_number < P-02
        LET mean_now  = MEAN OF temperature OVER retained
        LET spread    = STANDARD DEVIATION OF temperature OVER retained
        LET outliers  = FILTER retained WHERE |temperature − mean_now| > P-01 × spread

        IF NONE OF retained IS IN outliers THEN
            converged = TRUE
        ELSE
            retained     = FILTER retained WHERE temperature IS NOT IN outliers
            round_number = round_number + 1
        END IF
    END WHILE
```

**Trois choses à y voir.**

**La boucle est justifiée, bornée, et sa sortie est décrite.** `WHILE` est le seul mot-clé
du lexique qu'il faut défendre : le rejet est réellement itératif — retirer une aberrante
change la moyenne, ce qui peut en révéler une autre —, il n'y a pas de forme fermée, le
maximum est un paramètre métier (`P-02`), et **ce qui se passe quand il est atteint est
écrit** : le relevé est publié quand même, étiqueté `SUSPECT`. Sans cette dernière phrase,
chaque implémenteur déciderait pour lui-même.

**`STANDARD DEVIATION` n'est pas dans le lexique, et c'est voulu.** C'est une opération
mathématique nommée, comme un logarithme : le pseudo-langage ne redéfinit pas les
mathématiques. Ce que la spécification doit au développeur, c'est **laquelle** — ici celle
d'échantillon, au diviseur `n − 1` — et elle le dit dans la règle.

**La borne est stricte, et ça change un résultat.** `>` et non `≥` : une lecture à
exactement `P-01` écarts-types est **gardée**. La frontière devait tomber d'un côté ; le
métier a choisi l'inclusif, et l'a écrit.

> **→ L'algorithme complet** — 65 lignes, 33 des 35 éléments du lexique :
> [SPEC-WTH-001 §7.1](exemples/weather-summary/spec/SPEC-WTH-001.en.md).
> **→ Le plus petit, pour commencer** : [la vitesse moyenne d'un
> trajet](exemples/average-speed/) — deux fonctions, trois règles.

---

## L'aide-mémoire

À imprimer et à garder à côté du clavier. **Tout le lexique tient ici.**

```
STRUCTURE
  DEFINE name(param : Type) : Type
  INPUTS · OUTPUTS · PRECONDITIONS · POSTCONDITIONS · INVARIANTS
  LET x = ...             (on introduit un nom, on ne le réaffecte jamais)
  RETURN x
  RAISE ERROR E-XXX "message"

CONDITIONS          IF ... THEN / ELSE IF ... THEN / ELSE / END IF
                    (au-delà de deux conditions : table de décision)

ITÉRATION           FOR EACH x IN c ... END FOR
                    WHILE cond ... END WHILE          (à justifier)

ENSEMBLES           SUM · MEAN · MINIMUM · MAXIMUM · COUNT
                    SUM OF x OVER c
                    FILTER c WHERE cond · GROUP c BY criterion
                    SORT c BY a ASCENDING|DESCENDING
                    THE FIRST · THE LAST · THERE EXISTS · NONE · ALL

OPÉRATEURS          + − × ÷ ^     = ≠ < ≤ > ≥     BETWEEN a AND b
                    AND · OR · NOT · IN
VALEURS             TRUE · FALSE · ABSENT
IMPOSÉE             ROUND(value, decimals, mode)

TYPES               <nom> : <Famille>(<unité pivot> ▸ <unité d'usage>,
                                      <précision>, <plage>)
  Familles (ISO/IEC 11404) : Integer · Real · Scaled · CharacterString
             Boolean · Enumerated · DateAndTime · TimeInterval
             Sequence[…] · Array[…] · Table[…]

IDENTIFIANTS
  FN fonction · SPEC spécification · RG règle · P paramètre · D donnée
  EX exigence · ET étape · GR groupe · INV invariant · CT cas de test
  E erreur · Q question · SM suggestion · N notice · C/H contrôle
  + un UUID par objet, attribué une fois, jamais réattribué

LES 8 QUESTIONS À SE POSER AVANT DE DIRE « C'EST FINI »
  1. Chaque grandeur porte-t-elle ce qui la rend interprétable — unité, échelle, référentiel ?
  2. Chaque IF a-t-il son ELSE ? Chaque table est-elle complète ?
  3. Où arrondit-on, à combien, dans quel sens, et où va le résidu ?
  4. Que fait-on des ex æquo ?
  5. Que fait-on d'une donnée absente, nulle, négative, aberrante ?
  6. L'ordre d'application des règles change-t-il le résultat ? Si oui, est-il écrit ?
  7. Chaque règle est-elle couverte par au moins un cas de test calculé à la main ?
  8. Les volumes, la latence, la précision et la rejouabilité sont-ils chiffrés ?
```

---

← [Retour au cadre](CADRE.md) · [Les exemples](exemples/)
