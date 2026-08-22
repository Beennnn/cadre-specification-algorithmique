# Les exemples

*Six exemples, délibérément différents. Aucun n'est décoratif : chacun existe pour montrer
un **régime** que les autres ne montrent pas. Le critère pour en ajouter un septième est
là-dessus, pas sur le domaine.*

---

## En un coup d'œil

| Exemple | Taille | Jusqu'où il va | Ce qu'il montre et que les autres ne montrent pas |
|---|---|---|---|
| **[La vitesse moyenne d'un trajet](average-speed/)** | ~170 l. | besoin → spec → code → rapports | La **prise en main**. Deux fonctions, deux paramètres, trois règles |
| **[Le relevé d'une station météo](weather-summary/)** | ~450 l. | spec, deux langues | **Tout le pseudo-langage**, et une **boucle qui peut s'épuiser** |
| **[Le bilan de masse d'un lot](mass-balance/)** | ~470 l. | besoin → spec → contrat → code → **écarts** | La **chaîne complète**, jusqu'à l'analyse des écarts |
| **[Le refroidissement d'une boisson](SPEC-THM-001-refroidissement.md)** | ~630 l. | spec | Le **continu et l'approché** : équation différentielle, incertitude |
| **[Le lever du Soleil](SPEC-AST-001-lever-coucher-du-soleil.md)** | ~510 l. | spec | Une **équation sans solution** hors des cercles polaires |
| **[L'autonomie d'un véhicule](fil-rouge/)** | ~840 l. | besoin → découpage → glossaire → données → spec | Le **passage à l'échelle** : douze fonctions, dix étapes |

**Par où commencer** : la vitesse moyenne si vous découvrez la méthode, le relevé météo si
vous voulez voir le pseudo-langage au travail, le bilan de masse si vous voulez voir ce que
le document devient une fois passé aux développeurs.

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

---

## Les cinq autres, et ce qu'ils apportent

### La vitesse moyenne — la prise en main

Deux fonctions, trois règles, lu en cinq minutes. Il est minuscule et porte quand même un
vrai faux ami : **la vitesse moyenne n'est pas la moyenne des vitesses**. 60 km à 30 km/h
puis 60 km à 60 km/h donnent 40 km/h, pas 45.

Son détail le plus instructif : `CT-02` est gardé **bien qu'il ne discrimine pas**. Il
montre qu'une implémentation fausse passe un test plausible, et le rapport de test le dit
cas par cas. C'est aussi le seul exemple avec un **rapport de couverture outillé**, qui
relie chaque point de la spec au fichier et à la ligne qui l'implémentent.

### Le bilan de masse — la chaîne complète

Le seul qui va **jusqu'à l'analyse des écarts**. Le code s'exécute, rejoue les données de
référence, et l'étape 5 conclut que **le code est conforme et que la spécification est en
défaut** : un composant reçoit +180 % de sa part visée alors que la conservation de la
masse est parfaite. La correction remonte au document, versionnée.

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
| **Deux langues** | `<nom>.en.md` fait foi, `<nom>.fr.md` est la traduction. `C-42` vérifie qu'elles portent les mêmes objets et les mêmes identités |
| **Une seule langue pour le reste** | jeu de données, code, tests, rapports : **anglais**. Ce sont des artefacts d'exécution, pas de relecture |
| **Deux vues du même calcul** | l'algorithme intégré, qui se juge d'un coup, et les règles numérotées, qui sont adressables |
| **Des chiffres vérifiés** | tout résultat publié a été recalculé indépendamment de toute implémentation |
| **Les 25 contrôles** | `java outils/Verifier.java` finit à zéro échec sur les six |

## Ajouter un exemple

Un exemple mérite d'exister s'il montre un **régime** que les autres ne montrent pas : une
autre nature de grandeur, un autre type de faux ami, une autre échelle. Un septième exemple
de calcul discret et exact n'apprendrait rien de plus que le premier.

```bash
java outils/Verifier.java                      # les contrôles, sur tout le dépôt
java outils/Couverture.java exemples/<nom>     # le rapport de couverture spec ↔ code
```
