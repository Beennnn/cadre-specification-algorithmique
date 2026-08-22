# SPEC-WTH-001 — Relevé quotidien d'une station météo

| | |
|---|---|
| **Identifiant** | SPEC-WTH-001 |
| **Version** | 1.1.0 |
| **Statut** | Validée |
| **Valideur métier** | Responsable du réseau d'observation |
| **Co-auteur technique** | Équipe données climatiques |
| **Glossaire de référence** | [GLOSSAIRE.md](../../../GLOSSAIRE.md) v1.0.0 |
| **Traduction de** | [SPEC-WTH-001.en.md](SPEC-WTH-001.en.md) |

> **Version française, non normative.** La version qui fait foi est
> [`SPEC-WTH-001.en.md`](SPEC-WTH-001.en.md). `C-42` vérifie que les deux portent les mêmes
> objets et les mêmes identités durables.

> **Pourquoi cet exemple.** C'est celui qui exerce **tout le pseudo-langage** — 33 des 35
> éléments du lexique, dans un algorithme de 65 lignes. Pas pour faire étalage : le calcul
> en a réellement besoin. Écarter des lectures, les grouper, les ordonner, rejeter les
> valeurs aberrantes **par itérations**, puis qualifier le résultat par une table de
> décision.
>
> Il se situe entre [la vitesse moyenne](../../average-speed/) — trois règles, cinq minutes
> de lecture — et [l'autonomie d'un véhicule](../../fil-rouge/), qui s'étale sur une
> douzaine de fonctions.

---

## 1. Objectif et contexte

Une station météo enregistre des températures au fil de la journée. En fin de journée on
publie un relevé : moyenne, minimum, maximum, et les épisodes de gel.

Deux choses le rendent moins simple qu'il n'y paraît. Les capteurs **tombent en panne** :
une lecture peut manquer, être signalée invalide, ou être franchement absurde — une sonde
au soleil affiche 45 °C en janvier. Et un relevé calculé sur de mauvaises lectures est pire
que pas de relevé du tout, parce que personne ne peut voir qu'il est faux.

## 2. Périmètre

**Dans le périmètre.** Pour une station et un jour : filtrer les lectures, rejeter les
aberrantes, les statistiques, les épisodes de gel, et une étiquette de qualité sur le
résultat.

**Hors périmètre.** L'étalonnage des capteurs, le comblement de lacunes entre stations, la
prévision, et toute agrégation sur plusieurs jours.

## 3. Glossaire

| Terme | Définition |
|---|---|
| **Lecture** | Une température mesurée à un instant, avec l'indicateur de qualité de la chaîne d'acquisition. |
| **Lecture retenue** | Une lecture qui a survécu au filtrage (`RG-010`) et au rejet des aberrantes (`RG-030`). Seules celles-là entrent dans les statistiques. |
| **Valeur aberrante** | Une lecture encore retenue, trop éloignée de la moyenne de son propre groupe. « Trop » vaut `P-01` écarts-types — une décision métier, pas une constante statistique. |
| **Épisode de gel** | Une suite maximale de lectures consécutives **strictement en dessous** de `P-06`. Strictement : une lecture à exactement 0,0 °C n'est pas du gel (`RG-050`). |
| **Étiquette de qualité** | Ce que vaut le relevé : `GOOD`, `ACCEPTABLE` ou `SUSPECT`. Elle voyage avec le résultat et n'est jamais retirée. |

---

## 4. Entrées

```
request :
    station_id       : Identifier(text, 3 to 12 characters)      [CharacterString]
    observation_date : Date(Gregorian calendar)                  [DateAndTime]
    readings         : Sequence[Reading](0 .. 1440)

Reading :
    recorded_at  : DateAndTime(UTC, to the minute)               [DateAndTime]
    temperature  : Temperature(°C, −90.0 .. +60.0, 1 decimal)    [Real] — may be ABSENT
    quality_flag : Enumerated { VALID, SUSPECT, FAULTY }          [Enumerated]
```

> **`ABSENT` n'est pas `−999`.** Les chaînes d'acquisition encodent traditionnellement une
> lecture manquante par un nombre impossible. Cette convention a produit assez d'hivers à
> −999 °C pour être bannie ici : une température manquante est `ABSENT`, et `RG-010` dit ce
> qu'il advient d'elle.

## 5. Sorties

```
result :
    station_id      : Identifier(text, 3 to 12 characters)       [CharacterString]
    retained_count  : Integer(≥ 0)                               [Integer]
    rejected_count  : Integer(≥ 0)                               [Integer]
    mean_temperature: Temperature(°C, P-05 decimals)             [Real]
    min_temperature : Temperature(°C, 1 decimal)                 [Real]
    max_temperature : Temperature(°C, 1 decimal)                 [Real]
    frost_episodes  : Sequence[FrostEpisode]
    quality_label   : Enumerated { GOOD, ACCEPTABLE, SUSPECT }   [Enumerated]

FrostEpisode :
    started_at    : DateAndTime(UTC, to the minute)              [DateAndTime]
    ended_at      : DateAndTime(UTC, to the minute)              [DateAndTime]
    reading_count : Integer(≥ 1)                                 [Integer]
```

## 6. Paramètres

| Id | Nom | Valeur | Unité | Qui peut le changer | Date d'effet |
|---|---|---|---|---|---|
| <a id="p-01"></a>`P-01` | Facteur de rejet des aberrantes | 2,0 | écarts-types | Responsable du réseau d'observation | 2026-01-01 |
| <a id="p-02"></a>`P-02` | Nombre maximal de tours de rejet | 5 | tours | Responsable du réseau d'observation | 2026-01-01 |
| <a id="p-03"></a>`P-03` | Nombre minimal de lectures pour un relevé | 6 | lectures | Responsable du réseau d'observation | 2026-01-01 |
| <a id="p-04"></a>`P-04` | Mode d'arrondi de la moyenne publiée | `HALF_EVEN` | — | Responsable du réseau d'observation | 2026-01-01 |
| <a id="p-05"></a>`P-05` | Décimales de la moyenne publiée | 1 | — | Responsable du réseau d'observation | 2026-01-01 |
| <a id="p-06"></a>`P-06` | Seuil de gel | 0,0 | °C | Responsable du réseau d'observation | 2026-01-01 |

---

## 7. Règles

### 7.1 L'algorithme, en un seul morceau

C'est le calcul entier, dans l'ordre où le métier l'énonce. Tous les mots-clés employés ici
appartiennent au lexique fermé du [pseudo-langage](../../../PSEUDO-LANGAGE.md).

```
DEFINE summarise_station_day(request) : result

    PRECONDITIONS  E-WTH-001, E-WTH-002

    -- RG-010 · keep only what can be trusted
    LET usable = FILTER request.readings
                 WHERE temperature ≠ ABSENT
                   AND quality_flag = VALID
                   AND temperature BETWEEN -90.0 AND 60.0

    -- RG-020 · a summary on too few readings is not a summary
    IF COUNT OF usable < P-03 THEN
        RAISE ERROR E-WTH-003 "not enough usable readings to publish a summary"
    ELSE
        the calculation continues
    END IF

    -- RG-030 · iterative outlier rejection, bounded by P-02
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

    -- RG-040 · the statistics, on the retained readings only
    retained_count   = COUNT OF retained
    rejected_count   = COUNT OF usable − COUNT OF retained
    mean_temperature = ROUND( MEAN OF temperature OVER retained, P-05, P-04 )
    min_temperature  = MINIMUM OF temperature OVER retained
    max_temperature  = MAXIMUM OF temperature OVER retained

    -- RG-050 · frost episodes, in chronological order
    LET chronological = SORT retained BY recorded_at ASCENDING
    LET frost_runs    = GROUP chronological BY consecutive runs where temperature < P-06

    frost_episodes = empty
    FOR EACH run IN frost_runs
        LET episode = new FrostEpisode
        episode.started_at    = recorded_at OF THE FIRST OF run
        episode.ended_at      = recorded_at OF THE LAST OF run
        episode.reading_count = COUNT OF run
        frost_episodes = frost_episodes followed by episode
    END FOR

    -- RG-060 · what the summary is worth
    IF NOT converged THEN
        quality_label = SUSPECT
    ELSE IF rejected_count = 0 THEN
        quality_label = GOOD
    ELSE
        quality_label = ACCEPTABLE
    END IF

    RETURN result
```

> **`STANDARD DEVIATION OF … OVER …` n'est pas dans le lexique, et c'est délibéré.** C'est
> une opération statistique nommée, comme `arctangente` ou `logarithme` : le pseudo-langage
> ne redéfinit pas les mathématiques. Ce que la spécification doit au développeur, c'est
> **quel** écart-type — ici celui d'échantillon, diviseur `n − 1` — et `RG-030` le dit.

### RG-005 — Ce que le relevé identifie

```
result.station_id = request.station_id
```

Le relevé porte la station qu'il décrit. Le jour n'est pas porté dans le résultat : c'est
`observation_date`, et `E-WTH-002` garantit que toute lecture lui appartient — le relevé
décrit donc ce jour-là et aucun autre.

> **Un simple passage mérite quand même une règle.** Sans elle, rien dans le document ne
> dit que `station_id` ressort inchangé, et rien n'interdit à une implémentation de le
> normaliser, de le rogner ou de le passer en majuscules au passage. Ça paraît évident ;
> l'évident est précisément ce que personne n'écrit et que chacun lit à sa façon.

### RG-010 — Lectures exploitables

```
usable = FILTER readings
         WHERE temperature ≠ ABSENT
           AND quality_flag = VALID
           AND temperature BETWEEN -90.0 AND 60.0
```

Une lecture est écartée si elle manque, si la chaîne d'acquisition l'a signalée, ou si elle
sort de la plage physiquement possible.

> **Les lectures `SUSPECT` sont écartées, pas conservées avec un avertissement.** La chaîne
> d'acquisition sait quelque chose que nous ignorons, et un relevé qui mélange silencieusement
> des lectures suspectes et des bonnes ne peut plus être audité ensuite.

### RG-020 — Nombre minimal de lectures

```
IF COUNT OF usable < P-03 THEN
    RAISE ERROR E-WTH-003 "not enough usable readings to publish a summary"
ELSE
    the calculation continues
END IF
```

> **Ne rien publier vaut mieux que publier une moyenne sur deux lectures.** Le seuil `P-03`
> est une décision métier : c'est ce que le responsable du réseau est prêt à défendre.

### RG-030 — Rejet itératif des valeurs aberrantes

Chaque tour calcule la moyenne et l'écart-type **d'échantillon** (diviseur `n − 1`) des
lectures encore retenues, puis écarte celles qui s'éloignent de plus de `P-01` écarts-types
de cette moyenne. On s'arrête dès qu'un tour n'écarte rien.

```
WHILE NOT converged AND round_number < P-02
    LET mean_now = MEAN OF temperature OVER retained
    LET spread   = STANDARD DEVIATION OF temperature OVER retained
    LET outliers = FILTER retained WHERE |temperature − mean_now| > P-01 × spread

    IF NONE OF retained IS IN outliers THEN
        converged = TRUE
    ELSE
        retained     = FILTER retained WHERE temperature IS NOT IN outliers
        round_number = round_number + 1
    END IF
END WHILE
```

> **Une boucle explicite doit être justifiée, et celle-ci l'est.** Le rejet est réellement
> itératif : retirer une aberrante change la moyenne, ce qui peut en révéler une autre. Il
> n'existe pas de forme close.
>
> **Critère d'arrêt** : un tour qui n'écarte rien. **Nombre maximal de tours** : `P-02` = 5.
> **En cas de non-convergence** : le calcul n'échoue **pas** — il publie ce qu'il a, et
> `RG-060` étiquette le résultat `SUSPECT`. S'arrêter sans le dire serait le vrai défaut.

> **Pourquoi `>` et non `≥`.** Une lecture exactement à `P-01` écarts-types est **gardée**.
> La borne doit tomber d'un côté, et le responsable du réseau a choisi l'inclusif : à la
> limite, la lecture n'est pas une preuve de panne.

### RG-040 — Statistiques

```
retained_count   = COUNT OF retained
rejected_count   = COUNT OF usable − COUNT OF retained
mean_temperature = ROUND( MEAN OF temperature OVER retained, P-05, P-04 )
min_temperature  = MINIMUM OF temperature OVER retained
max_temperature  = MAXIMUM OF temperature OVER retained
```

Seule la moyenne publiée est arrondie. Le minimum et le maximum sont des lectures, et une
lecture se publie telle qu'elle a été mesurée.

> **La moyenne publiée se calcule en arithmétique décimale exacte.** C'est ici la seule
> grandeur à laquelle s'applique une décision d'arrondi, et une décision d'arrondi prise
> sur un nombre à un cheveu de l'égalité part du mauvais côté : une moyenne vraie de 0,25
> accumulée en binaire flottant peut atterrir à 0,2500000000000001, que `HALF_EVEN` envoie
> sur 0,3 au lieu de 0,2. Les lectures portent une décimale : leur somme est exacte en
> décimal et la division arrondit juste par construction. Les statistiques de `RG-030` ne
> sont **pas** concernées : elles alimentent une comparaison à un seuil, pas un nombre
> publié. Voir `EX-01`.

> **Aucun départage n'est nécessaire ici, et il vaut la peine de le dire.** `MINIMUM` et
> `MAXIMUM` rendent une **valeur**, pas une lecture choisie : si trois lectures partagent la
> température la plus basse, le minimum est cette température, quelle que soit celle qu'on
> regarde. Un départage ne serait nécessaire que si la sortie était *quelle* lecture était
> la plus froide.

### RG-050 — Épisodes de gel

Les lectures sont mises dans l'ordre chronologique, puis découpées en suites maximales de
lectures consécutives strictement inférieures à `P-06` :

```
LET chronological = SORT retained BY recorded_at ASCENDING
LET frost_runs    = GROUP chronological BY consecutive runs where temperature < P-06

FOR EACH run IN frost_runs
    started_at    = recorded_at OF THE FIRST OF run
    ended_at      = recorded_at OF THE LAST OF run
    reading_count = COUNT OF run
END FOR
```

> **Strictement inférieur, et ça compte.** Une lecture à exactement 0,0 °C n'est **pas** du
> gel. L'eau ne gèle pas instantanément à zéro, et le réseau publie des alertes de gel : un
> seuil à `≤` en déclencherait une chaque nuit d'automne qui frôle zéro.
>
> **`THE FIRST` et `THE LAST` n'ont besoin d'aucun départage, parce que la suite est
> ordonnée et ses instants sont uniques.** `RG-050` trie par `recorded_at`, et `E-WTH-001`
> rejette deux lectures partageant un instant — « la première de la suite » désigne donc une
> lecture et une seule. Retirez l'un des deux et le superlatif redevient ambigu.
>
> **L'ordre vient de `recorded_at`, jamais de l'ordre dans l'entrée.** Deux stations
> envoient leurs lectures dans des ordres différents, et un relevé qui en dépendrait ne
> serait pas reproductible.

### RG-060 — Étiquette de qualité

| Convergence (`RG-030`) | Lectures rejetées | **`quality_label`** |
|---|---|---|
| non atteinte en `P-02` tours | — | **`SUSPECT`** |
| atteinte | 0 | **`GOOD`** |
| atteinte | ≥ 1 | **`ACCEPTABLE`** |

La table est complète : les trois combinaisons couvrent tous les cas possibles, et aucune ne
recouvre une autre.

### Table de couverture

| Règle | Couverte par |
|---|---|
| `RG-005` | CT-01, CT-02, CT-04, CT-07 |
| `RG-010` | CT-01, CT-02, CT-03 |
| `RG-020` | CT-03 |
| `RG-030` | CT-01, CT-02, CT-04, CT-07 |
| `RG-040` | CT-01, CT-02, CT-04, CT-07 |
| `RG-050` | CT-01, CT-02 |
| `RG-060` | CT-01 (`ACCEPTABLE`), CT-02 (`GOOD`), CT-04 (`SUSPECT`) |

Chaque paramètre doit en outre être **arbitré** par au moins un cas — un cas dont le
résultat publié changerait si le paramètre changeait. Dire qu'un cas « couvre `RG-040` » ne
dit rien de ce qu'il décide de `P-04` :

| Paramètre | Arbitré par |
|---|---|
| `P-01` facteur de rejet | CT-01, CT-04 |
| `P-02` nombre maximal de tours | CT-04 |
| `P-03` nombre minimal de lectures | CT-03 |
| `P-04` mode d'arrondi | **CT-07 seul** |
| `P-05` décimales publiées | CT-01, CT-02, CT-04, CT-07 |
| `P-06` seuil de gel | CT-01, CT-02, CT-04, CT-07 |

---

## 8. Invariants

| Id | Énoncé |
|---|---|
| <a id="inv-01"></a>`INV-01` | `retained_count + rejected_count = COUNT OF usable`. Aucune lecture ne disparaît sans être comptée quelque part. |
| <a id="inv-02"></a>`INV-02` | `min_temperature ≤ mean_temperature ≤ max_temperature`, à l'arrondi de la moyenne près. |
| <a id="inv-03"></a>`INV-03` | Les épisodes de gel sont **disjoints** et par `started_at` croissant. Deux épisodes ne se recouvrent jamais : une suite est maximale par construction. |
| <a id="inv-04"></a>`INV-04` | Le résultat est **invariant par permutation** des lectures d'entrée. `RG-050` trie par `recorded_at`, donc l'ordre d'arrivée ne change rien. |
| <a id="inv-05"></a>`INV-05` | `retained_count ≥ P-03`, toujours. En dessous, `RG-020` a rejeté avant qu'aucune statistique ne soit calculée. |

## 9. Cas d'erreur métier

| Id | Condition | Comportement |
|---|---|---|
| <a id="e-wth-001"></a>`E-WTH-001` | Deux lectures portent le même `recorded_at` | Rejet : l'ordre chronologique de `RG-050` ne serait pas défini |
| <a id="e-wth-002"></a>`E-WTH-002` | Un `recorded_at` sort de `observation_date` | Rejet : le relevé mélangerait deux jours |
| <a id="e-wth-003"></a>`E-WTH-003` | Moins de `P-03` lectures exploitables | Rejet, en indiquant combien étaient exploitables |

> **La non-convergence ne figure pas dans cette table**, et c'est tout le sens de `RG-060`.
> C'est un résultat à étiqueter, pas une défaillance à signaler.

---

## 10. Jeu d'essai

Tous les cas portent sur la station `STA-001` le 2026-01-15, lectures horaires à partir de
00:00 UTC, avec les paramètres ci-dessus.

### CT-01 — Une aberrante, convergence, un épisode de gel

| Heure | Température | Indicateur |
|---|---|---|
| 00:00 | 2,0 | VALID |
| 01:00 | 1,5 | VALID |
| 02:00 | 0,5 | VALID |
| 03:00 | −0,5 | VALID |
| 04:00 | −1,0 | VALID |
| 05:00 | 0,0 | VALID |
| 06:00 | 1,0 | VALID |
| 07:00 | 45,0 | VALID |

**Tours de rejet.** Tour 1 : moyenne 6,0625, écart-type 15,7649, seuil ±31,5297 → **45,0
écartée**. Tour 2 : moyenne 0,5000, écart-type 1,0801, seuil ±2,1602 → rien d'écarté,
**convergé**.

| Sortie | Valeur |
|---|---|
| `retained_count` | **7** |
| `rejected_count` | **1** |
| `mean_temperature` | **0,5 °C** |
| `min_temperature` | **−1,0 °C** |
| `max_temperature` | **2,0 °C** |
| `frost_episodes` | **un** : 03:00 → 04:00, 2 lectures |
| `quality_label` | **`ACCEPTABLE`** |

**Deux choses sont tranchées ici d'un coup.** La lecture à 45,0 °C était signalée `VALID`
par la chaîne — seul `RG-030` l'attrape. Et 05:00 à exactement 0,0 °C n'est **pas** dans
l'épisode de gel, qui s'arrête à 04:00.

### CT-02 — Aucune aberrante

Les huit mêmes lectures **sans** celle de 07:00. Le tour 1 n'écarte rien : convergence
immédiate.

| Sortie | Valeur |
|---|---|
| `retained_count` | **7** · `rejected_count` **0** |
| `mean_temperature` | **0,5 °C** |
| `frost_episodes` | un : 03:00 → 04:00, 2 lectures |
| `quality_label` | **`GOOD`** |

**La moyenne est la même qu'en CT-01, 0,5 °C.** C'est la preuve que le rejet a fonctionné :
une implémentation qui sauterait `RG-030` publierait 6,1 °C pour CT-01.

### CT-03 — Pas assez de lectures exploitables

Huit lectures, mais cinq signalées `FAULTY` et une `ABSENT` : deux exploitables, sous
`P-03` = 6. Rejeté par `E-WTH-003`, **avant** tout calcul statistique.

### CT-04 — Non-convergence

Douze lectures horaires, toutes `VALID` et toutes dans la plage physique :

| Heure | 00:00 | 01:00 | 02:00 | 03:00 | 04:00 | 05:00 | 06:00 | 07:00 | 08:00 | 09:00 | 10:00 | 11:00 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| °C | 0,0 | 0,1 | 0,1 | 0,2 | 0,4 | 0,8 | 1,6 | 3,2 | 6,4 | 12,8 | 25,6 | 51,2 |

Chaque tour écarte exactement la plus grande valeur, et cinq tours n'y suffisent pas :

| Tour | Moyenne | Écart-type | Écartée |
|---|---|---|---|
| 1 | 8,533 | 15,437 | 51,2 |
| 2 | 4,655 | 7,972 | 25,6 |
| 3 | 2,560 | 4,122 | 12,8 |
| 4 | 1,422 | 2,134 | 6,4 |
| 5 | 0,800 | 1,105 | 3,2 |

Après `P-02` = 5 tours, le processus n'a pas convergé. Le relevé **est publié quand même**,
sur les 7 lectures restantes :

| Sortie | Valeur |
|---|---|
| `retained_count` | **7** · `rejected_count` **5** |
| `mean_temperature` | **0,5 °C** |
| `min_temperature` | **0,0 °C** · `max_temperature` **1,6 °C** |
| `frost_episodes` | **aucun** — aucune lecture n'est strictement sous 0,0 °C |
| `quality_label` | **`SUSPECT`** |

**C'est le cas qui documente la sortie de boucle.** Sans lui, personne ne saurait ce qui se
passe quand l'itération s'épuise — et chaque implémenteur déciderait pour lui-même.

> **Les valeurs doublent, et elles restent dans `−90 .. +60`.** La première version de ce
> cas employait 0, 1, 2, … 1024 °C, que `RG-010` aurait écartées avant que `RG-030` ne les
> voie : les tours publiés étaient arithmétiquement justes et ne pouvaient pas se produire.
> La suite est simplement divisée par dix, ce qui ne change rien à la structure des tours —
> le rejet compare des écarts à un multiple de l'écart-type, il est donc invariant
> d'échelle.

### CT-05 — Deux lectures au même instant

Sept lectures, dont deux à 00:00. Rejet par `E-WTH-001` : avec deux lectures au même
instant, « la première de la suite » de `RG-050` en désigne deux, et la règle cesse d'être
une règle.

### CT-06 — Une lecture d'un autre jour

Sept lectures du 2026-01-15, la dernière datée du 2026-01-16 à 00:00. Rejet par
`E-WTH-002` : le relevé décrirait deux jours en prétendant en décrire un.

### CT-07 — Une moyenne pile sur la moitié

| Heure | 00:00 | 01:00 | 02:00 | 03:00 | 04:00 | 05:00 | 06:00 | 07:00 |
|---|---|---|---|---|---|---|---|---|
| °C | 0,0 | 0,1 | 0,2 | 0,3 | 0,4 | 0,3 | 0,2 | 0,5 |

Rien n'est écarté — le tour converge immédiatement. Les huit lectures somment à 2,0 °C, donc
la moyenne vaut **exactement 0,25 °C**, pile sur l'égalité.

| Sortie | Valeur |
|---|---|
| `retained_count` | **8** · `rejected_count` **0** |
| `mean_temperature` | **0,2 °C** |
| `min_temperature` | **0,0 °C** · `max_temperature` **0,5 °C** |
| `frost_episodes` | **aucun** |
| `quality_label` | **`GOOD`** |

**C'est le seul cas où `P-04` décide de quelque chose.** `HALF_EVEN` publie 0,2 ; `HALF_UP`
publierait 0,3. Toutes les autres moyennes sont insensibles au mode d'arrondi.

> **Et c'est le cas qui a mis `EX-01` en défaut.** Avec la moyenne accumulée en double
> précision, cette entrée publiait 0,2 ou 0,3 **selon l'ordre d'arrivée des lectures** —
> une violation franche de `INV-04`, trouvée par la propriété de permutation et par rien
> d'autre. La double précision suffit pour les statistiques ; elle ne suffit pas pour un
> nombre qu'on arrondit. `EX-01` le dit depuis la v1.1.0, et `RG-040` dit quoi en faire.

### Provenance et validation

| | |
|---|---|
| **Provenance** | Tours recalculés pas à pas en arithmétique exacte, indépendamment de toute implémentation |
| **Comment ils ont été examinés** | Chaque tour a été recalculé : moyenne, écart-type d'échantillon au diviseur `n − 1`, seuil, ensemble écarté. Les épisodes de gel ont été vérifiés lecture par lecture contre la borne stricte `<` |
| **Ce que l'examen a produit** | CT-04 a été **construit** pour épuiser la boucle : aucun jeu naturel ne le faisait, et sans lui la branche de non-convergence de `RG-060` n'était couverte par rien. CT-05, CT-06 et CT-07 ont été ajoutés en v1.1.0 : les deux premiers parce que `E-WTH-001` et `E-WTH-002` étaient énoncés et exercés par aucun cas, le troisième parce qu'aucun cas n'arbitrait `P-04` — et parce que le construire a mis `EX-01` en défaut |
| **Un document par cas** | [`../tests/`](../tests/) — ce que chaque cas existe pour attraper, et ce qu'il laisserait passer |
| **Validé par** | Responsable du réseau d'observation, 2026-08-22 |

---

## 11. Contraintes et exigences

| Id | Énoncé | Source | Propriétaire | Vérification |
|---|---|---|---|---|
| <a id="ex-01"></a>`EX-01` | La double précision suffit **pour les statistiques** — les lectures portent une décimale et une incertitude de ±0,2 °C, très au-dessus de tout bruit numérique. Elle ne suffit **pas** pour la moyenne publiée : ce nombre est arrondi, et une décision d'arrondi prise sur une valeur à un cheveu de l'égalité part du mauvais côté. `mean_temperature` se calcule en arithmétique décimale exacte (`RG-040`) | Spécification capteur, et le défaut trouvé sur `CT-07` | Architecture SI | Revue de conception, et la propriété de permutation `INV-04` |
| <a id="ex-02"></a>`EX-02` | 1 440 lectures par station et par jour, 3 000 stations, un relevé par station et par jour | Dimensionnement du réseau | Responsable exploitation | Mesure en exploitation |
| <a id="ex-03"></a>`EX-03` | Le relevé est **rejouable à l'identique** depuis les lectures archivées, pendant 30 ans | Archivage climatique `CLI-ARC-1` | Assurance qualité | Rejeu annuel sur archive |

## 12. Questions ouvertes

| Id | Question | Décideur | Échéance | Statut |
|---|---|---|---|---|
| <a id="q-01"></a>`Q-01` | Une station dont le relevé est `SUSPECT` trois jours de suite doit-elle sortir automatiquement du réseau ? | Responsable du réseau d'observation | 2026-12-01 | Ouverte |
| <a id="q-02"></a>`Q-02` | `P-01` doit-il dépendre de la saison ? La dispersion hivernale est naturellement plus grande | Responsable du réseau d'observation | | Fermée |

## 13. Historique et notices de changement

| Version | Date | Changement | Impact sur les résultats | Notice |
|---|---|---|---|---|
| 1.0.0 | 2026-08-22 | Version initiale | — | — |
| 1.1.0 | 2026-08-22 | Ajout de `CT-05`, `CT-06`, `CT-07`. `CT-04` réénoncé avec des données que `RG-010` n'écarte pas. `EX-01` amendé et `RG-040` explicité : la moyenne publiée se calcule en arithmétique décimale exacte | **Une moyenne tombant pile sur l'égalité pouvait être publiée d'un côté ou de l'autre**, selon l'ordre d'arrivée des lectures. C'est corrigé et `INV-04` tient. Aucune autre valeur publiée ne change | Implémenteurs : la moyenne doit s'accumuler en décimal exact, pas en double. Rejouer le jeu de référence — une implémentation qui somme en double échoue `CT-07` sous permutation |

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../../../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `SPEC-WTH-001` | `d016ba2f-3f89-4b82-92cc-e30124eb547f` | document | SPEC-WTH-001 — Relevé quotidien d'une station météo |
| `RG-005` | `154eba7a-a195-45cd-85ce-a25c6488c7f8` | règle | Ce que le relevé identifie |
| `RG-010` | `8c70a047-add6-44ca-be32-bde0cfe2ae3f` | règle | Lectures exploitables |
| `RG-020` | `d94a065b-1bf3-4a0e-bc6e-488713d5da24` | règle | Nombre minimal de lectures |
| `RG-030` | `830ed5d0-0f38-4489-a084-42d3a5e7f00a` | règle | Rejet itératif des valeurs aberrantes |
| `RG-040` | `4fe6018e-437e-401b-b4e1-30cb153b7788` | règle | Statistiques |
| `RG-050` | `1d9ad939-2231-4a94-8e2a-01b72c5d94f8` | règle | Épisodes de gel |
| `RG-060` | `efe07b5d-a903-4138-860d-bd0f6005a17d` | règle | Étiquette de qualité |
| `CT-01` | `bf599ac6-916a-4a89-b7b4-8985a6d093b6` | cas de test | Une aberrante, convergence, un épisode de gel |
| `CT-02` | `a6b6ce38-a651-4269-bdb2-3bea51583d09` | cas de test | Aucune aberrante |
| `CT-03` | `4784e57e-41d9-4424-bd92-2d7fa1cf0152` | cas de test | Pas assez de lectures exploitables |
| `CT-04` | `45f29eca-fd22-4d17-8b9d-e9817c3f52f8` | cas de test | Non-convergence |
| `CT-05` | `45f1832b-1beb-40a2-a8b2-c88c2449d8ec` | cas de test | Deux lectures au même instant |
| `CT-06` | `2a66100f-29c1-4056-92af-82321c933d82` | cas de test | Une lecture d'un autre jour |
| `CT-07` | `658ca7f7-95c5-4968-a548-68297db0fd34` | cas de test | Une moyenne pile sur la moitié |
| `P-01` | `8abfb819-5321-4a6a-a86b-acb6e3767455` | paramètre | Facteur de rejet des aberrantes |
| `P-02` | `66f9866a-bd25-4d1c-9bdc-f2a97a1c63af` | paramètre | Nombre maximal de tours de rejet |
| `P-03` | `fa573126-d4e0-433c-8836-d48b19b123dd` | paramètre | Nombre minimal de lectures pour un relevé |
| `P-04` | `ab868262-f8a7-4149-a84a-f316b2d26c6f` | paramètre | Mode d'arrondi de la moyenne publiée |
| `P-05` | `5eb6502c-2198-41c1-8161-4eb05309c758` | paramètre | Décimales de la moyenne publiée |
| `P-06` | `8b586410-483c-49d4-b1ba-3c78341b8593` | paramètre | Seuil de gel |
| `EX-01` | `f9ce6fbe-3f85-4bb0-8a4d-f7ec0d535568` | exigence | La double précision suffit **pour les statistiques** — les lectures po |
| `EX-02` | `245000d6-c226-4d14-b56a-8ff7be643e9c` | exigence | 1 440 lectures par station et par jour, 3 000 stations, un relevé par |
| `EX-03` | `745def66-32a8-4ca1-a2b5-6ba8acdb612b` | exigence | Le relevé est **rejouable à l'identique** depuis les lectures archivée |
| `INV-01` | `53654d26-cf42-4131-9b28-a5eb4a831652` | invariant | `retained_count + rejected_count = COUNT OF usable`. Aucune lecture ne |
| `INV-02` | `df4d5585-6cf0-4547-8f5e-0d288572b1fa` | invariant | `min_temperature ≤ mean_temperature ≤ max_temperature`, à l'arrondi de |
| `INV-03` | `3f8c34ce-99ef-485f-8fc6-2e9609889fa8` | invariant | Les épisodes de gel sont **disjoints** et par `started_at` croissant. |
| `INV-04` | `6edd9e11-c50d-4a91-971c-6a73a451c191` | invariant | Le résultat est **invariant par permutation** des lectures d'entrée. |
| `INV-05` | `87b0a976-8188-4f50-bded-49672142f3ae` | invariant | `retained_count ≥ P-03`, toujours. En dessous, `RG-020` a rejeté avant |
| `E-WTH-001` | `7738b38a-880e-4e81-b601-a3ba78e78d3a` | cas d'erreur | Deux lectures portent le même `recorded_at` |
| `E-WTH-002` | `8342a539-8aff-457a-aab7-838f3cf5de88` | cas d'erreur | Un `recorded_at` sort de `observation_date` |
| `E-WTH-003` | `ea4adb82-d849-49ff-9724-1c18491649a7` | cas d'erreur | Moins de `P-03` lectures exploitables |
| `Q-01` | `bad2cdd9-e127-4b3c-9ad7-5275df9e6cd1` | question | Une station dont le relevé est `SUSPECT` trois jours de suite doit-ell |
| `Q-02` | `9beb66b7-d8f8-4e69-b944-8f26f600394f` | question | `P-01` doit-il dépendre de la saison ? La dispersion hivernale est nat |
