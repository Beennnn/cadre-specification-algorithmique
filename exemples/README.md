# Les exemples

*Six exemples, délibérément différents. Aucun n'est décoratif : chacun existe pour montrer
un **régime** que les autres ne montrent pas. Le critère pour en ajouter un septième est
là-dessus, pas sur le domaine.*

---

## En un coup d'œil

| Exemple | Taille | Jusqu'où il va | Ce qu'il montre et que les autres ne montrent pas |
|---|---|---|---|
| **[La vitesse moyenne d'un trajet](average-speed/)** | **260 l.** | spec → un doc par cas → code → rapports | La **prise en main**. Deux fonctions, deux paramètres, trois règles |
| **[Le bilan de masse d'un lot](mass-balance/)** | 450 l. | besoin → spec → contrat → cas → code → rapports → **écarts** | La **chaîne complète**, jusqu'à l'analyse des écarts |
| **[Le relevé d'une station météo](weather-summary/)** | 540 l. | spec → un doc par cas → code → rapports | **Tout le pseudo-langage**, et une **boucle qui peut s'épuiser** |
| **[Le lever du Soleil](sunrise/)** | 465 l. | spec, deux langues | Une **équation sans solution** hors des cercles polaires |
| **[Le refroidissement d'une boisson](cooling/)** | 580 l. | spec, deux langues | Le **continu et l'approché** : équation différentielle, incertitude |
| **[L'autonomie d'un véhicule](fil-rouge/)** | 770 l. | besoin → découpage → glossaire → données → spec | Le **passage à l'échelle** : douze fonctions, dix étapes |

---

## Commencez par la vitesse moyenne

**C'est le plus petit exemple du dépôt, et il est complet.** Deux fonctions, deux
paramètres, trois règles, six cas de test. La spécification se lit en cinq minutes ; chaque
cas tient sur une page courte ; le code s'exécute en deux commandes.

Il est minuscule et porte quand même un vrai faux ami : **la vitesse moyenne n'est pas la
moyenne des vitesses.** 60 km à 30 km/h puis 60 km à 60 km/h donnent 40 km/h, pas 45 — on
passe deux heures sur le segment lent et une sur le rapide, donc le lent pèse deux fois plus.

Deux détails valent le détour, et ils tiennent en deux phrases :

- **`CT-02` est gardé bien qu'il ne discrimine pas.** Il montre qu'une implémentation fausse
  passe un test plausible — et le rapport de test le dit, cas par cas.
- **`CT-06` a été ajouté parce qu'aucun cas ne décidait le mode d'arrondi.** `P-01` était
  validé, daté, implémenté, déclaré couvert — et vérifié par rien.

> **Si vous ne lisez qu'un exemple, lisez celui-là.** Les cinq autres ajoutent chacun un
> régime que celui-ci ne montre pas ; aucun ne le remplace comme point d'entrée.

---

## Celui qui montre le pseudo-langage : le relevé d'une station météo

**C'est l'exemple à ouvrir pour juger le pseudo-langage.** Son algorithme intégré fait
**65 lignes** et emploie **33 des 35 éléments** du lexique — non par étalage, mais parce
que le calcul en a besoin.

Le cas : une station enregistre des températures ; on publie en fin de journée la moyenne,
les extrêmes et les épisodes de gel. Deux choses le compliquent — les capteurs tombent en
panne, et une sonde au soleil affiche 45 °C en janvier.

```
    -- RG-010 · ne garder que ce qui est digne de confiance
    LET usable = FILTER request.readings
                 WHERE temperature ≠ ABSENT
                   AND quality_flag = VALID
                   AND temperature BETWEEN -90.0 AND 60.0

    -- RG-030 · rejet itératif des aberrantes, borné par P-02
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

    -- RG-050 · épisodes de gel, dans l'ordre chronologique
    LET chronological = SORT retained BY recorded_at ASCENDING
    LET frost_runs    = GROUP chronological BY consecutive runs where temperature < P-06

    FOR EACH run IN frost_runs
        episode.started_at    = recorded_at OF THE FIRST OF run
        episode.ended_at      = recorded_at OF THE LAST OF run
        episode.reading_count = COUNT OF run
    END FOR
```

**Trois choses à y regarder de près.**

**La boucle peut s'épuiser, et ce n'est pas une erreur.** Le rejet est réellement itératif :
retirer une aberrante change la moyenne, ce qui peut en révéler une autre. Après `P-02`
tours sans convergence, le relevé **est publié quand même**, étiqueté `SUSPECT`. Le cas
`CT-04` a été *construit* pour épuiser la boucle — aucun jeu naturel ne le faisait, et sans
lui personne ne saurait ce qui se passe quand l'itération s'arrête.

**Une borne stricte porte une conséquence métier.** Une lecture à exactement 0,0 °C n'est
pas du gel. Avec `≤`, le réseau alerterait chaque nuit d'automne qui frôle zéro.

**`ABSENT` remplace la convention `−999`.** Les chaînes d'acquisition encodent
traditionnellement une lecture manquante par un nombre impossible. Cette convention a
produit assez d'hivers à −999 °C pour être bannie.

> **Ce que l'exemple prouve en creux.** `CT-01` et `CT-02` publient la même moyenne,
> **0,5 °C**. C'est la preuve que le rejet a fonctionné : une implémentation qui sauterait
> `RG-030` publierait 6,1 °C pour `CT-01` — et passerait pourtant tous les autres cas.

**Et ce qu'il a fini par trouver.** En écrivant le code, `CT-07` — une moyenne qui tombe
pile sur l'égalité d'arrondi — publiait 0,2 ou 0,3 **selon l'ordre d'arrivée des lectures**.
L'addition en double précision n'est pas associative, et l'arrondi n'a aucune tolérance :
il transforme un écart de 10⁻¹⁶ en un écart de 0,1 °C. C'était un défaut de l'exigence
`EX-01`, qui déclarait la double précision suffisante. Il a fallu **les deux dispositifs**
pour le voir — la propriété de permutation, qui teste sur des ordres engendrés, et le cas
sur l'égalité, seul endroit où cette propriété a des dents.

---

## Les quatre autres, et ce qu'ils apportent

### Le bilan de masse — la chaîne complète

Le seul qui va **jusqu'à l'analyse des écarts**. Le code s'exécute, rejoue les données de
référence, et l'étape 5 conclut que **le code est conforme et que la spécification est en
défaut** : un composant reçoit +180 % de sa part visée alors que la conservation de la
masse est parfaite. La correction remonte au document, versionnée.

> **Il montre aussi qu'un invariant ne remplace pas un jeu de référence.** `CT-03` et
> `CT-10` sont passés par les quatre invariants **alors que les masses pesées sont
> fausses** : une allocation dépendante de l'ordre et un mauvais mode d'arrondi conservent
> tous deux la masse. Ce qui les attrape, ce sont les masses validées par le métier.

### Le refroidissement — le continu et l'approché

Une équation différentielle, une incertitude de mesure, et un résultat dont on discute la
**justesse du modèle** — pas celle du calcul. Confondre les deux transforme chaque écart de
mesure en rapport de bogue.

### Le lever du Soleil — l'équation sans solution

Au-delà du cercle polaire, le Soleil peut ne jamais se lever : `cos ω` sort de `[−1, 1]` et
il n'existe aucun instant de lever. **Ce n'est pas une erreur, c'est un résultat.** Et les
deux cas polaires ne diffèrent que par le **signe** — une implémentation qui testerait
`|cos ω| > 1` sans le regarder confondrait jour et nuit polaires.

### L'autonomie d'un véhicule — le passage à l'échelle

Douze fonctions, un catalogue de données partagé, une chaîne de traitement en dix étapes
dont l'outil dérive les **niveaux d'exécution** et le **chemin critique**.

---

## Le contraste qui porte le plus

Le bilan de masse et le refroidissement traitent des grandeurs de nature opposée, et c'est
**délibéré** :

| | Bilan de masse | Refroidissement |
|---|---|---|
| Les grandeurs | **discrètes et exactes** — une masse pesée existe au pas de la balance | **continues et approchées** — une température est mesurée à 2 °C près |
| Le résultat | juste ou faux ; la conservation en fait une propriété **vérifiable** | approché ; on discute reproductibilité, justesse numérique, validité du modèle |
| Le type numérique imposé | `Scaled` — décimal exact, binaire flottant exclu | `Real` — la double précision suffit largement |
| Le faux ami central | l'arrondi et le **sort du résidu** | l'**ordre des opérations** |

> **La même méthode, appliquée à deux domaines, produit deux conclusions opposées sur le
> type numérique.** C'est le meilleur argument qu'elle ne présuppose rien : ce sont les
> exigences chiffrées qui décident, pas l'habitude du développeur.

---

## Ce que chaque exemple respecte

| | |
|---|---|
| **Deux langues** | `<nom>.en.md` fait foi, `<nom>.fr.md` est la traduction. `C-42` vérifie qu'elles portent les mêmes objets et les mêmes identités. Les **six specs** du dépôt la respectent |
| **Une seule langue pour le reste** | jeu de données, code, tests, rapports : **anglais**. Ce sont des artefacts d'exécution, pas de relecture |
| **Deux vues du même calcul** | l'algorithme intégré, qui se juge d'un coup, et les règles numérotées, qui sont adressables |
| **Des chiffres vérifiés** | tout résultat publié a été recalculé indépendamment de toute implémentation |
| **Les 25 contrôles** | `java outils/Verifier.java` finit à zéro échec sur les six |
| **Une structure de projet** | `spec/`, `tests/`, `code/` en disposition Maven, `reports/` — pour les trois exemples qui vont jusqu'au code |

## Ajouter un exemple

Un exemple mérite d'exister s'il montre un **régime** que les autres ne montrent pas : une
autre nature de grandeur, un autre type de faux ami, une autre échelle. Un septième exemple
de calcul discret et exact n'apprendrait rien de plus que le premier.

```bash
java outils/Verifier.java                      # les contrôles, sur tout le dépôt
java outils/Couverture.java exemples/<nom>     # le rapport de couverture spec ↔ code
```
