# SPEC-THM-001 — Prévision du refroidissement d'une boisson chaude

| | |
|---|---|
| **Identifiant** | SPEC-THM-001 |
| **Version** | 1.0.0 |
| **Statut** | Acceptée |
| **Auteur métier** | *(rôle : Responsable modèles physiques)* |
| **Valideur métier** | *(rôle : Direction produit)* |
| **Co-auteur technique** | *(rôle : Architecte applicatif)* |
| **Date d'effet** | 2026-01-01 |
| **Glossaire de référence** | interne — §3 de ce document |

> **Note de lecture.** Exemple pédagogique du cadre décrit dans [CADRE.md](../CADRE.md),
> appliqué à un **modèle physique**. Le phénomène — une boisson chaude qui refroidit —
> est connu de tout le monde et se calcule avec une loi que tout le monde a croisée au
> lycée. C'est précisément ce qui en fait un bon étalon : **aucune connaissance de
> domaine ne peut masquer une ambiguïté de rédaction.**
>
> Cet exemple illustre les trois adaptations du cadre au calcul scientifique
> (CADRE.md §2.8) : **spécifier un modèle et une tolérance, pas une méthode de
> résolution**, **distinguer reproductibilité, justesse et validité du modèle**, et
> **traiter la convergence comme une décision métier**.

---

## 1. Objectif et contexte

À partir de l'état initial d'une boisson chaude et de son environnement, prévoir :

1. sa **température à un instant donné** ;
2. l'**instant auquel elle atteint une température cible** (« buvable »).

Le calcul tient compte d'un éventuel **ajout d'un liquide froid** (lait, eau) à un
instant déclaré.

**Deux usages, une seule spécification** — et c'est l'intérêt du cas :

- **Embarqué** : dans une application grand public, exécuté sur le téléphone, **sans
  réseau**, plusieurs fois par seconde pendant que l'utilisateur déplace un curseur.
- **Laboratoire** : dans un outil pédagogique, où l'on veut comparer le modèle à des
  mesures réelles et faire varier la température ambiante au cours du temps.

Ces deux usages ont des contraintes très différentes (§11) et **conduiront à deux
implémentations différentes de la même spécification**. C'est la démonstration que la
spécification décrit bien *ce qui est calculé*, pas *comment*.

## 2. Périmètre

**Dans le périmètre :**
- Le modèle de refroidissement et ses hypothèses.
- Le mélange de deux liquides à températures différentes.
- L'instant d'atteinte d'une température cible, et les conditions de son existence.
- Les cas où la cible n'est pas atteignable ou pas atteinte dans l'horizon.

**Hors périmètre :**
- L'**estimation** du coefficient de refroidissement à partir de mesures
  (`SPEC-THM-002`). Il est ici une donnée d'entrée.
- Le rayonnement thermique et l'évaporation : le modèle retenu ne les représente pas
  explicitement (`RG-010`, hypothèses). Voir `Q-01`.
- Les gradients internes : la boisson est supposée à température homogène.
- Toute recommandation faite à l'utilisateur (« attendez 4 minutes ») — c'est de
  l'affichage, pas du calcul.

## 3. Glossaire

| Terme | Définition |
|---|---|
| **Boisson** | Une masse de liquide à température homogène, dans un récipient donné. |
| **Température ambiante** | La température de l'air autour du récipient, supposée constante sur toute la durée prévue. |
| **Coefficient de refroidissement** | Le paramètre `k` du modèle (`RG-010`). Il caractérise le couple récipient + environnement, pas le liquide. |
| **Constante de temps** | `1 ÷ k`. Durée au bout de laquelle l'écart à la température ambiante a été divisé par `e` (≈ 2,718). |
| **Capacité thermique massique** | L'énergie nécessaire pour élever d'un degré un kilogramme du liquide. |
| **Ajout** | L'introduction d'un second liquide, à un instant déclaré. |
| **Température cible** | La température à laquelle on souhaite savoir quand la boisson arrive. |
| **Horizon** | La durée maximale sur laquelle la prévision a un sens. Au-delà, on ne répond pas. |

## 4. Entrées

```
demande :
    boisson :
        masse                 : Masse(kg, > 0, 4 décimales)
        temperature_initiale  : Température(°C, 2 décimales)
        capacite_massique     : CapacitéThermique(kJ·kg⁻¹·K⁻¹, > 0, 3 décimales)
    temperature_ambiante      : Température(°C, 2 décimales)
    coefficient_refroidissement : Taux(min⁻¹, > 0, 5 décimales)
    ajout                     : Ajout — facultatif
    instant_demande           : Durée(min, ≥ 0, 4 décimales) — facultatif
    temperature_cible         : Température(°C, 2 décimales) — facultatif

Ajout :
    masse             : Masse(kg, ≥ 0, 4 décimales)
    temperature       : Température(°C, 2 décimales)
    capacite_massique : CapacitéThermique(kJ·kg⁻¹·K⁻¹, > 0, 3 décimales)
    instant           : Durée(min, ≥ 0, 4 décimales)
```

**Préconditions :**
- Au moins l'un de `instant_demande` et `temperature_cible` est fourni.
- `coefficient_refroidissement` est strictement positif.
- Les masses et capacités massiques sont strictement positives, sauf la masse de
  l'ajout qui peut être nulle (`CT-07`).

> **Note sur l'unité de température.** Les températures sont exprimées en degrés
> Celsius. Le modèle de `RG-010` ne fait intervenir que des **différences** de
> température : il est donc invariant par changement d'origine de l'échelle
> (`INV-04`), et un calcul mené en kelvins donne exactement le même instant
> d'atteinte (`CT-06`). Cette propriété est écrite **parce qu'elle cesserait d'être
> vraie** si l'on ajoutait un terme de rayonnement, qui dépend de la température
> absolue (`Q-01`). C'est le genre de conséquence qu'une spécification doit anticiper.

## 5. Sorties

```
resultat :
    temperature_a_instant      : Température(°C) — si instant_demande fourni
    instant_atteinte_cible     : Durée(min)      — si temperature_cible fournie et atteinte
    cible_deja_atteinte        : Booléen         — vraie si atteinte dès l'instant 0
    cible_inatteignable        : Booléen         — vraie si la cible est hors du domaine (RG-050)
    cible_hors_horizon         : Booléen         — vraie si atteinte au-delà de P-04
    temperature_apres_ajout    : Température(°C) — si un ajout a eu lieu
    iterations_utilisees       : Entier(≥ 0)     — 0 si résolution exacte
```

> Les trois indicateurs `cible_deja_atteinte`, `cible_inatteignable` et
> `cible_hors_horizon` ne sont pas redondants : ils décrivent **trois situations
> physiquement distinctes** qu'une valeur absente seule confondrait. Un résultat
> scientifique dont on ne peut pas dire *pourquoi* il est absent n'est pas exploitable.

## 6. Paramètres

| Id | Libellé | Valeur | Unité | Qui peut le changer | Circuit de validation | Fréquence observée | Date d'effet |
|---|---|---|---|---|---|---|---|
| `P-01` | Coefficient de refroidissement — tasse en céramique ouverte | 0,03000 | min⁻¹ | Responsable modèles | Validation Direction produit | 1 à 2 fois par an | 2026-01-01 |
| `P-02` | Coefficient de refroidissement — mug isotherme fermé | 0,00600 | min⁻¹ | Responsable modèles | Validation Direction produit | 1 à 2 fois par an | 2026-01-01 |
| `P-03` | Température cible de dégustation par défaut | 55,00 | °C | Direction produit | Validation Direction | 1 fois par an | 2026-01-01 |
| `P-04` | Horizon de prévision | 120,0 | min | Direction produit | Validation Direction | rare | 2026-01-01 |
| `P-05` | Tolérance de convergence sur l'instant | 0,01 | min | Responsable modèles | Validation Direction produit | rare | 2026-01-01 |
| `P-06` | Nombre maximal d'itérations | 100 | — | Responsable modèles | Validation Direction produit | rare | 2026-01-01 |
| `P-07` | Écart minimal à la température ambiante pour que la cible soit réputée atteignable | 0,10 | °C | Responsable modèles | Validation Direction produit | rare | 2026-01-01 |

> `P-01` et `P-02` sont des **valeurs mesurées**, pas des constantes physiques : elles
> dépendent du récipient et de l'environnement, et elles seront réestimées quand la
> gamme de produits changera. Elles ne peuvent donc pas vivre dans le code.
> Les capacités thermiques massiques, elles, sont des **entrées** et non des
> paramètres, parce qu'elles dépendent du liquide que l'utilisateur choisit.

## 7. Règles

### RG-010 — Le modèle de refroidissement

```
SOIT k           = coefficient_refroidissement
SOIT T_ambiante  = temperature_ambiante
SOIT T(0)        = boisson.temperature_initiale

La température de la boisson suit la loi de refroidissement de Newton :

    dT/dt = − k × ( T(t) − T_ambiante )

T_ambiante est constante sur toute la durée prévue (hypothèse H-2).
```

**Hypothèses assumées par le métier**, chacune fausse dans l'absolu et acceptable dans
le domaine d'emploi visé :

| # | Hypothèse | Conséquence si elle est violée |
|---|---|---|
| H-1 | La boisson est à température homogène | Sous-estimation de la température de surface, donc du ressenti |
| H-2 | La température ambiante est constante | Dérive lente sur les durées longues (voir `Q-02`) |
| H-3 | Le coefficient `k` ne dépend ni de la température, ni du volume | Le modèle surestime légèrement le refroidissement aux fortes températures, où le rayonnement et l'évaporation dominent |
| H-4 | Le mélange (`RG-030`) est instantané et sans perte | Négligeable pour un ajout de quelques secondes |

> **La spécification énonce l'équation, pas sa méthode de résolution.** Pour `k` et
> `T_ambiante` constants, cette équation admet une solution en forme fermée ; une
> implémentation peut l'utiliser, ou intégrer numériquement. **Les deux sont conformes**
> dès lors qu'elles respectent les tolérances du §8.2. Ce choix appartient au
> développeur — et le §11 montre que les deux usages du produit ne le trancheront pas de
> la même façon.
>
> Écrire « intégrer par Runge-Kutta d'ordre 4 avec un pas de 1 s » serait une faute :
> cela figerait une méthode, interdirait la solution exacte, et deviendrait faux le jour
> où `T_ambiante` varierait.

### RG-020 — Température à un instant donné

```
temperature_a_instant = la valeur en t = instant_demande de la solution de RG-010,
                        partant de l'état initial et intégrant l'ajout éventuel (RG-040)
```

Si `instant_demande` dépasse `P-04`, la valeur n'est pas produite et
`cible_hors_horizon` ne s'applique pas — c'est l'erreur `E-HORIZON-001`.

### RG-030 — Mélange de deux liquides

```
Lors d'un ajout, la température résultante est celle qui conserve l'énergie
thermique de l'ensemble :

SOIT capacite_boisson = boisson.masse × boisson.capacite_massique
SOIT capacite_ajout   = ajout.masse   × ajout.capacite_massique

    temperature_apres_ajout =
        ( capacite_boisson × T(instant_ajout) + capacite_ajout × ajout.temperature )
        ÷ ( capacite_boisson + capacite_ajout )

Le mélange ne modifie pas la boisson : il en produit une nouvelle.

    boisson_melangee.masse             = boisson.masse + ajout.masse
    boisson_melangee.capacite_massique = ( capacite_boisson + capacite_ajout )
                                         ÷ boisson_melangee.masse
    boisson_melangee.temperature       = temperature_apres_ajout
```

Si `ajout.masse = 0`, la formule laisse la boisson inchangée : le cas est **couvert par la règle
générale**, il n'a pas besoin d'être traité à part. Une spécification qui ajoute une
branche `SI masse = 0 ALORS ...` pour un cas déjà couvert crée deux chemins là où un
seul suffit — et un jour, ils divergeront.

### RG-040 — Ordre des opérations : quand l'ajout a lieu

```
L'instant de l'ajout est une DONNÉE D'ENTRÉE. La spécification n'impose pas
de convention par défaut : le demandeur doit le déclarer.

Le calcul se déroule en deux phases :
    1. refroidissement seul, de t = 0 à t = instant_ajout          (RG-010)
    2. mélange à t = instant_ajout, produisant temperature_apres_ajout  (RG-030)
    3. refroidissement de boisson_melangee, à partir de t = instant_ajout  (RG-010)
```

> **C'est le faux ami central de cette spécification, et il est contre-intuitif.**
>
> Ajouter le lait **tôt** puis attendre, ou attendre puis ajouter le lait **tard**, ne
> donne pas la même température finale — alors que la quantite_commandee de lait, sa température
> et la durée totale sont identiques. Le cas `CT-02` le chiffre : à 15 minutes,
> l'ajout immédiat donne **55,18 °C** et l'ajout différé **54,52 °C**. Ajouter le lait
> tout de suite laisse donc la boisson **plus chaude**.
>
> L'explication tient en une phrase : le refroidissement est proportionnel à l'écart à
> la température ambiante, donc une boisson déjà refroidie par le lait perd ensuite sa
> chaleur **plus lentement**.
>
> Aucun développeur ne peut deviner cela, et aucun ne doit avoir à en décider. Sans
> `RG-040`, deux implémentations honnêtes produiraient deux réponses différentes à la
> question « quelle sera la température dans un quart d'heure ? », et l'écart —
> deux tiers de degré — serait mis sur le compte des arrondis.

### RG-050 — Instant d'atteinte de la température cible

```
L'instant d'atteinte est le PLUS PETIT t ≥ 0 tel que T(t) ≤ temperature_cible.

Conditions d'existence, évaluées dans cet ordre :

  SI boisson.temperature_initiale ≤ temperature_cible ALORS
     instant_atteinte_cible = 0,00 ; cible_deja_atteinte = vrai ; on s'arrête

  SINON SI temperature_cible ≤ temperature_ambiante + P-07 ALORS
     cible_inatteignable = vrai ; aucun instant n'est produit ; on s'arrête

  SINON SI l'instant d'atteinte est supérieur à P-04 ALORS
     cible_hors_horizon = vrai ; aucun instant n'est produit

  SINON
     instant_atteinte_cible est produit, à P-05 près
  FIN SI
```

> **Pourquoi la marge `P-07` et non une comparaison à la température ambiante seule.**
> La température tend vers l'ambiante sans jamais l'atteindre. Une cible fixée à
> 20,001 °C pour une ambiante de 20,00 °C est *mathématiquement* atteignable — au bout
> de plusieurs heures — et *physiquement* dénuée de sens, le modèle n'ayant aucune
> validité à cette échelle. La marge transforme une asymptote en une décision
> explicite. Elle est fixée par le métier, pas par le développeur.

### RG-060 — Convergence

Si l'implémentation résout `RG-050` par une méthode itérative :

```
Le calcul s'arrête lorsque l'encadrement de l'instant est inférieur à P-05,
ou après P-06 itérations.

SI P-06 itérations sont atteintes sans satisfaire P-05 ALORS
   SIGNALER ERREUR E-CONV-001
   aucun instant n'est produit
SINON
   instant_atteinte_cible est produit, et le nombre d'itérations utilisées est rapporté
FIN SI

Le nombre d'itérations réellement utilisées est rapporté.
Une implémentation qui résout RG-050 exactement rapporte 0 itération.
```

> **Les trois éléments d'un calcul itératif, et celui qu'on oublie toujours.** Un
> critère d'arrêt : presque toujours écrit. Un nombre maximal d'itérations : souvent
> écrit. **Ce qui est rapporté en cas de non-convergence : presque jamais écrit** — et
> c'est celui-là qui produit les résultats faux silencieux, quand le programme renvoie
> son dernier itéré comme s'il avait convergé. Ici, le métier a tranché : on ne renvoie
> rien, on signale. Décision métier, pas décision technique.

## 8. Invariants et critères d'acceptation numérique

### 8.1 Invariants

| Id | Propriété |
|---|---|
| `INV-01` | Si `T(0) > T_ambiante`, alors `T(t)` est strictement décroissante |
| `INV-02` | `T(t)` reste strictement comprise entre `T_ambiante` et `T(0)` |
| `INV-03` | `T(t)` tend vers `T_ambiante` quand `t` croît, sans jamais l'atteindre |
| `INV-04` | **Invariance par changement d'origine de l'échelle** : ajouter une constante à *toutes* les températures (initiale, ambiante, ajout, cible) laisse l'instant d'atteinte inchangé — un calcul en kelvins donne le même résultat qu'en degrés Celsius (`CT-06`) |
| `INV-05` | **Encadrement du mélange** : `T_melange` est comprise entre les deux températures mélangées, et vaut exactement la plus chaude si la masse ajoutée est nulle |
| `INV-06` | **Monotonie vis-à-vis de `k`** : à conditions égales, un `k` plus grand donne un instant d'atteinte plus court |
| `INV-07` | **Monotonie vis-à-vis de la cible** : une cible plus basse donne un instant d'atteinte plus tardif |
| `INV-08` | Le calcul est déterministe : deux exécutions sur la même entrée donnent le même résultat, indicateurs compris |

> `INV-04` et `INV-06` sont des **propriétés de symétrie et de monotonie**. Elles se
> testent automatiquement sur des milliers d'entrées générées et attrapent la classe
> d'erreurs la plus pernicieuse en calcul physique : celle où un résultat dépend
> silencieusement de l'unité ou de l'origine choisies.

### 8.2 Trois niveaux d'exactitude, à ne jamais confondre

| | **Reproductibilité** | **Justesse numérique** | **Validité du modèle** |
|---|---|---|---|
| Question | Deux implémentations conformes donnent-elles le même nombre ? | Le nombre est-il la vraie solution de l'équation `RG-010` ? | L'équation décrit-elle la réalité ? |
| S'évalue contre | La définition de cette spécification | La **solution analytique** de `RG-010` | Des **mesures réelles** |
| Tolérance exigée | **10⁻⁹ relatif** sur les températures et instants ; **égalité stricte** sur les indicateurs | **10⁻⁶ relatif** sur la température ; **`P-05`** sur l'instant | **± 2 °C** sur 60 min, sur le banc de mesure de référence |
| Un dépassement signifie | Une implémentation n'est pas conforme, **ou la spécification est ambiguë** | La méthode de résolution ou son pas est inadapté | Les hypothèses `H-1` à `H-4` sont sorties de leur domaine — **ce n'est pas un défaut du programme** |
| Se contrôle | En recette, à chaque livraison | En recette, sur les cas de synthèse | En **validation de modèle**, périodiquement, avec le métier |

> Confondre ces trois niveaux est l'erreur la plus commune sur un logiciel scientifique.
> Un écart de 1,5 °C entre la prévision et un thermomètre réel peut être parfaitement
> normal — c'est le modèle qui est approché, pas le programme qui est faux. Un écart de
> 10⁻⁶ entre deux implémentations, lui, ne l'est jamais.
>
> **Cette distinction est ce qui protège l'équipe de développement.** Sans elle, chaque
> écart de mesure devient un ticket de bogue, et l'équipe passe son temps à chercher un
> défaut dans du code correct.

### 8.3 Aléatoire

Cette spécification ne comporte aucune étape stochastique. Si une version ultérieure en
introduit une — estimation de `k` par ajustement, propagation d'incertitude par
Monte-Carlo —, **l'algorithme du générateur pseudo-aléatoire et sa graine deviennent des
entrées de la spécification**, parce que la reproductibilité d'un résultat en dépend.

## 9. Cas d'erreur métier

| Code | Condition | Conséquence | Message |
|---|---|---|---|
| `E-PARAM-001` | `coefficient_refroidissement ≤ 0` | Aucun résultat | « Le coefficient de refroidissement doit être strictement positif. » |
| `E-PARAM-002` | Une masse ou une capacité massique de la boisson est nulle ou négative | Aucun résultat | « Les caractéristiques de la boisson sont invalides. » |
| `E-ENTREE-001` | Ni `instant_demande` ni `temperature_cible` n'est fourni | Aucun résultat | « Précisez un instant ou une température cible. » |
| `E-HORIZON-001` | `instant_demande > P-04` | Aucun résultat | « La prévision ne dépasse pas 2 heures. » |
| `E-CONV-001` | `P-06` itérations sans convergence à `P-05` près | Aucun instant produit | « Le calcul n'a pas convergé. » |

## 10. Jeu d'essai

### Provenance et validation des résultats attendus

| | |
|---|---|
| **Provenance** | **Solution analytique** de l'équation de `RG-010` |
| **Comment ils ont été examinés** | Recalculés à la calculatrice ; l'écart entre les deux variantes de `CT-02` vérifié dans les deux sens |
| **Validés par** | *(rôle : Direction produit)* |
| **Le** | 2026-01-01 |
| **Pour la version** | 1.0.0 |

*Ces résultats sont des **données de référence** : ils qualifient le code et servent
de base de non-régression. Ils ne se modifient que par une revalidation métier datée
([CADRE §5](../CADRE.md)).*

**Construction de l'oracle.** Pour `k` et `T_ambiante` constants, `RG-010` admet la
solution analytique `T(t) = T_amb + (T₀ − T_amb)·exp(−k·t)`, et `RG-050` la solution
`t = ln((T_cible − T_amb) ÷ (T₀ − T_amb)) ÷ (−k)`. **La vérité de référence est donc
analytique** : elle ne provient d'aucune implémentation, et n'importe qui peut la
recalculer avec une calculatrice scientifique.

Sauf mention contraire : `T_ambiante = 20,00 °C`, `k = P-01 = 0,03000 min⁻¹`,
boisson de `0,2000 kg`, capacité massique `4,180 kJ·kg⁻¹·K⁻¹`.

### Vue d'ensemble

| Id | Ce qu'il exerce | Résultat attendu |
|---|---|---|
| `CT-01` | Refroidissement simple, température et instant d'atteinte | `T(30) = 46,4270 °C` ; `t(55 °C) = 20,6346 min` |
| `CT-02` | **Ordre des opérations** : ajout tôt vs ajout tard (`RG-040`) | `55,1833 °C` vs `54,5160 °C` |
| `CT-03` | Cible sous la température ambiante (`RG-050`) | `cible_inatteignable = vrai` |
| `CT-04` | Cible déjà atteinte à l'instant 0 | `t = 0,00` ; `cible_deja_atteinte = vrai` |
| `CT-05` | Cible atteinte au-delà de l'horizon | `cible_hors_horizon = vrai` |
| `CT-06` | **Invariance d'échelle** : mêmes données en kelvins (`INV-04`) | `t = 20,6346 min`, identique à `CT-01` |
| `CT-07` | Masse ajoutée nulle (`RG-030`) | Température inchangée : `85,0000 °C` |
| `CT-08` | Coefficient de refroidissement négatif | erreur `E-PARAM-001` |

### CT-01 — Refroidissement simple

`T₀ = 85,00 °C`, `T_amb = 20,00 °C`, `k = 0,03000 min⁻¹`, cible `55,00 °C`,
instant demandé `30,0000 min`.

| Grandeur | Calcul | Attendu |
|---|---|---|
| Constante de temps | `1 ÷ 0,03` | 33,33 min |
| `T(30)` | `20 + 65 × exp(−0,9)` | **46,4270 °C** |
| Instant d'atteinte | `ln(35 ÷ 65) ÷ (−0,03)` | **20,6346 min** |
| Contrôle | `T(20,6346)` | 55,0000 °C ✓ |
| `iterations_utilisees` | résolution exacte possible | 0 (ou ≤ `P-06` si itératif) |

### CT-02 — Ordre des opérations : le cas central

Café : `0,2000 kg` à `85,00 °C`, `c = 4,180`.
Lait : `0,0300 kg` à `5,00 °C`, `c = 3,900`.
`T_amb = 20,00 °C`, `k = 0,03000 min⁻¹`. **On demande la température à `t = 15 min`.**

Capacités thermiques : café `0,2000 × 4,180 = 0,8360 kJ·K⁻¹` ;
lait `0,0300 × 3,900 = 0,1170 kJ·K⁻¹` ; total `0,9530 kJ·K⁻¹`.

**Variante A — le lait est ajouté à `t = 0`**

| Étape | Calcul | Résultat |
|---|---|---|
| Mélange (`RG-030`) | `(0,8360 × 85 + 0,1170 × 5) ÷ 0,9530` | 75,1784 °C |
| Refroidissement 15 min | `20 + 55,1784 × exp(−0,45)` | **55,1833 °C** |

**Variante B — le lait est ajouté à `t = 15 min`**

| Étape | Calcul | Résultat |
|---|---|---|
| Refroidissement 15 min | `20 + 65 × exp(−0,45)` | 61,4458 °C |
| Mélange (`RG-030`) | `(0,8360 × 61,4458 + 0,1170 × 5) ÷ 0,9530` | **54,5160 °C** |

> **Écart : 0,6673 °C, en faveur de l'ajout immédiat.** Mêmes ingrédients, même durée,
> même quantite_commandee de lait — deux résultats. C'est exactement ce qu'une spécification doit
> trancher, et c'est exactement ce qu'un développeur ne peut pas deviner.
>
> Ce cas de test est aussi un excellent **détecteur d'implémentation paresseuse** :
> celle qui applique le mélange en fin de calcul « parce que c'est plus simple » donne
> 54,5160 dans les deux variantes, et passe tous les autres cas de test.

### CT-03 — Cible inatteignable

`T₀ = 85,00 °C`, `T_amb = 20,00 °C`, cible `15,00 °C`.

- `15,00 ≤ 20,00 + 0,10` → la cible est sous la température ambiante augmentée de `P-07`.
- **`cible_inatteignable = vrai`**, aucun instant produit, aucune erreur signalée.

> Ce n'est pas une erreur : la demande est légitime, la réponse est « jamais ». La
> distinction compte, parce qu'une erreur interrompt un traitement alors qu'un
> indicateur se propage.

### CT-04 — Cible déjà atteinte

`T₀ = 50,00 °C`, cible `55,00 °C`.

- `T(0) = 50,00 ≤ 55,00` → **`instant_atteinte_cible = 0,00 min`**,
  `cible_deja_atteinte = vrai`.

> La condition est évaluée **en premier** dans `RG-050`. L'ordre d'évaluation des
> conditions fait partie de la règle : intervertir les deux premières branches donnerait
> ici `cible_inatteignable` pour une boisson déjà buvable.

### CT-05 — Cible hors horizon

Mug isotherme : `k = P-02 = 0,00600 min⁻¹`, `T₀ = 85,00 °C`, cible `25,00 °C`.

| Grandeur | Calcul | Attendu |
|---|---|---|
| Instant d'atteinte théorique | `ln(5 ÷ 65) ÷ (−0,006)` | 427,4916 min |
| Comparaison à l'horizon | `427,49 > 120,0` | dépassé |
| Résultat | | **`cible_hors_horizon = vrai`**, aucun instant produit |
| Contrôle | `T(120) = 20 + 65 × exp(−0,72)` | 51,6389 °C — encore loin de 25 °C ✓ |

### CT-06 — Invariance d'échelle (kelvins)

Mêmes données que `CT-01`, exprimées en kelvins :
`T₀ = 358,15`, `T_amb = 293,15`, cible `328,15`.

- Écarts inchangés : `358,15 − 293,15 = 65` et `328,15 − 293,15 = 35`.
- **Instant d'atteinte : `20,6346 min`, strictement identique à `CT-01`.**

> Ce cas tient en trois nombres et vérifie `INV-04`. Il détecte l'erreur classique
> consistant à écrire `T(t) = T₀ × exp(−k·t)` — en oubliant la température ambiante —,
> qui donne un résultat plausible en Celsius et absurde en Kelvin.

### CT-07 — Masse ajoutée nulle

Ajout de `0,0000 kg` à `5,00 °C`, à `t = 0`.

- `RG-030` : `(0,8360 × 85 + 0 × 5) ÷ (0,8360 + 0)` = **85,0000 °C**.
- Aucune branche particulière : la règle générale suffit (`INV-05`).

### CT-08 — Coefficient négatif

`k = −0,03000` → **erreur `E-PARAM-001`**, aucun résultat.

### Table de couverture

| Règle | Couverte par | | Règle | Couverte par |
|---|---|---|---|---|
| `RG-010` | tous | | `RG-060` | *non couverte — voir `Q-03`* |
| `RG-020` | CT-01, CT-02 | | `INV-04` | CT-06 + tests de propriété |
| `RG-030` | CT-02, CT-07 | | `INV-05` | CT-07 + tests de propriété |
| `RG-040` | **CT-02** | | `INV-06`, `INV-07` | tests de propriété |
| `RG-050` | CT-01, CT-03, CT-04, CT-05 | | `E-HORIZON-001` | *non couverte — `Q-03`* |

> Deux cases vides, nommées plutôt que cachées. `RG-060` (non-convergence) n'est pas
> couverte parce qu'aucune entrée réaliste ne la déclenche avec une résolution exacte —
> ce qui est justement une question à trancher (`Q-03`).

## 11. Contraintes et exigences

### 11.1 Contraintes métier

Deux profils d'usage, **une seule spécification**. C'est la fiche de contraintes qui
diffère, et c'est elle qui produira deux implémentations.

| Dimension | Usage **embarqué** (application grand public) | Usage **laboratoire** (outil pédagogique) |
|---|---|---|
| **Volumétrie** | 2 millions de prévisions par jour, réparties sur les appareils ; jusqu'à 60 recalculs par seconde pendant qu'un curseur est déplacé | quelques centaines de calculs par session, 50 sessions par jour |
| **Mode d'appel** | À la demande, **sur l'appareil, sans réseau** | À la demande, sur poste de travail |
| **Latence** | **Moins de 1 ms par calcul.** Au-delà, le curseur devient saccadé — le produit est jugé de mauvaise qualité | moins de 2 s ; l'utilisateur attend un tracé |
| **Énergie et mémoire** | **Contrainte forte** : le calcul ne doit pas être perceptible sur l'autonomie ; empreinte mémoire de quelques kilo-octets | sans contrainte |
| **Exactitude** | Voir §8.2. Reproductibilité 10⁻⁹ ; justesse numérique 10⁻⁶ ; validité du modèle ± 2 °C à 60 min | identiques, **plus** la capacité de faire varier `T_ambiante` au cours du temps (`Q-02`) |
| **Déterminisme** | Strict (`INV-08`), indicateurs compris | strict |
| **Rejouabilité** | Aucune exigence : le résultat n'est pas conservé | **Les résultats publiés dans un compte rendu doivent être rejouables 5 ans**, avec la version de spécification et le jeu de paramètres de l'époque |
| **Auditabilité** | Aucune | Les entrées, les paramètres et la version de la spécification sont conservés avec chaque tracé |
| **Explicabilité** | L'utilisateur doit comprendre pourquoi aucune réponse n'est donnée : les trois indicateurs de `RG-050` sont affichés en clair | idem, **plus** le nombre d'itérations |
| **Criticité et mode dégradé** | Aucune dépendance externe : le calcul doit fonctionner en mode avion. Si les paramètres embarqués sont plus anciens que la dernière version publiée, le calcul se fait **quand même**, avec les anciens, et l'utilisateur en est informé | un calcul en erreur n'interrompt pas la session |
| **Confidentialité** | Aucune donnée personnelle ; aucune sortie réseau — ce qui est aussi un argument commercial | aucune |
| **Fréquence de changement** | `P-01`, `P-02`, `P-03` : réestimés 1 à 2 fois par an quand la gamme de récipients évolue. Règles : très rares | idem |
| **Qui modifie** | Le responsable modèles doit pouvoir réestimer `P-01` et `P-02` **sans livraison logicielle** — donc les paramètres sont téléchargés, pas compilés | idem |
| **Durée de vie** | 5 ans et plus | 10 ans (support pédagogique) |

### 11.2 Exigences de réalisation

| Id | Exigence | Source | Qui la valide | Vérification |
|---|---|---|---|---|
| `EX-01` | Le calcul embarqué **n'émet aucune requête réseau** : il fonctionne hors ligne et ne transmet rien | Engagement produit « aucune donnée ne quitte l'appareil » | Direction produit | Analyse des flux réseau, à chaque version publiée |
| `EX-02` | Le fichier de paramètres téléchargé est **signé**, et une signature invalide entraîne le maintien de la version précédente | Politique de sécurité applicative `SEC-APP-4` | Sécurité SI | Test de non-régression sur signature invalide |
| `EX-03` | Le code respecte le **standard de codage interne** `STD-DEV-2` | Comité d'architecture | Architecture SI | Intégration continue |

## 12. Questions ouvertes

| Id | Question | Décideur | Échéance | Statut |
|---|---|---|---|---|
| `Q-01` | Faut-il ajouter un terme de rayonnement et d'évaporation pour les températures supérieures à 80 °C, où `H-3` est la plus fausse ? Cela **casserait `INV-04`** (le modèle cesserait d'être invariant par changement d'origine d'échelle) et imposerait de travailler en kelvins | Responsable modèles | 2026-11-30 | Ouverte |
| `Q-02` | L'usage laboratoire demande une température ambiante variable dans le temps. `RG-010` reste valable, mais la solution en forme fermée disparaît : seule une résolution numérique reste possible. Faut-il l'intégrer à cette spécification ou en faire une variante ? | Direction produit | 2026-12-31 | Ouverte |
| `Q-03` | `RG-060` (non-convergence) et `E-HORIZON-001` ne sont couverts par aucun cas de test. Faut-il les conserver si aucune entrée réaliste ne les déclenche ? | Auteur métier | 2026-10-15 | Ouverte |
| `Q-04` | `P-07` vaut 0,10 °C. Cette marge devrait-elle dépendre de l'écart initial à l'ambiante plutôt que d'être une constante ? | Responsable modèles | 2026-11-30 | Ouverte |
| `Q-05` | *Tranchée le 2025-12-10 :* l'instant d'ajout doit-il avoir une valeur par défaut (0, c'est-à-dire « tout de suite ») ? → **non**. Une valeur par défaut sur `RG-040` masquerait une décision qui change le résultat ; le demandeur doit la déclarer | Direction produit | — | Fermée |

## 13. Historique

| Version | Date | Changement | Impact sur les résultats |
|---|---|---|---|
| 1.0.0 | 2026-01-01 | Version initiale | — |

---

## Annexe — Lecture technique de cette spécification

> Ajoutée par l'architecte après acceptation. Elle montre ce que la spécification a
> permis de décider — et, ici, **pourquoi les deux usages aboutissent à deux
> implémentations différentes de la même spécification**.

| Contrainte lue au §11 | Décision technique qui en découle |
|---|---|
| Embarqué : **< 1 ms, sans réseau, contrainte d'énergie**, `T_ambiante` constante | **La solution en forme fermée** — deux exponentielles et une division, sans allocation ni itération. Aucun intégrateur numérique n'est justifiable ici. `iterations_utilisees` vaut toujours 0 |
| Laboratoire : `T_ambiante` variable attendue (`Q-02`) | **Un intégrateur numérique à pas adaptatif**, dont la tolérance est réglée pour tenir la justesse numérique de 10⁻⁶ du §8.2. La forme fermée sert alors de **cas de non-régression** de l'intégrateur — un luxe rare, et gratuit ici |
| Les deux, mêmes tolérances de reproductibilité (§8.2) | Le **même jeu d'essai `CT-01` à `CT-08`** valide les deux implémentations. C'est le contrôle qui garantit qu'elles ne divergeront pas — et c'est possible **uniquement** parce que la spécification n'a imposé aucune méthode |
| Justesse 10⁻⁶ relatif, validité du modèle ± 2 °C | **La double précision binaire suffit très largement.** Contraste voulu avec [SPEC-PRX-001](SPEC-PRX-001-montant-a-payer.md), où l'exactitude au centime imposait un décimal exact : ici, la grandeur physique est elle-même incertaine à 2 °C près. **La même méthode, appliquée à deux domaines, produit deux conclusions opposées sur le type numérique** |
| Trois niveaux d'exactitude distingués (§8.2) | Trois campagnes de test **séparées** : non-régression à chaque livraison, justesse numérique sur les cas analytiques, validation de modèle sur banc de mesure une fois par an avec le métier. Sans cette séparation, un écart de mesure deviendrait un ticket de bogue |
| Paramètres réestimés 1 à 2 fois par an sans livraison | Les paramètres sont **téléchargés et versionnés**, jamais compilés ; l'application embarquée fonctionne avec la dernière version connue et l'indique. Les capacités massiques, elles, sont des entrées : elles viennent de l'utilisateur |
| Rejouabilité 5 ans côté laboratoire | Entrées, paramètres et version de spécification archivés avec chaque tracé. Côté embarqué : aucune persistance, donc aucun coût |
| `RG-050` : trois indicateurs distincts | Le type de retour n'est pas « un nombre ou rien » mais **un résultat structuré**. Une signature qui renverrait `null` en cas d'échec perdrait la distinction entre « déjà atteinte », « jamais » et « trop tard » — trois messages différents pour l'utilisateur |

**Ce que la spécification n'a volontairement pas dit** : le langage, la méthode de
résolution, le format des paramètres, la stratégie de mise en cache. Deux équipes ont
implémenté ce document de deux façons opposées, et **les deux sont conformes** — c'est
la meilleure preuve que la frontière a été tracée au bon endroit.

## Annexe — Identités

*Chaque objet porte un UUID attribué une fois et jamais modifié. L'identifiant lisible et le libellé sont des étiquettes : ils peuvent changer, l'identité non. Voir [CADRE.md §2.8](../CADRE.md).*

| Identifiant | UUID | Nature | Libellé |
|---|---|---|---|
| `SPEC-THM-001` | `8416d9a7-cdb6-4116-bc37-a9538d3ff520` | document | SPEC-THM-001 — Prévision du refroidissement d'une boisson chaude |
| `RG-010` | `944cf16e-090c-4191-a688-c1696c65cad4` | règle | Le modèle de refroidissement |
| `RG-020` | `a5b55277-3455-44c9-82d8-c7c98d8d1ff3` | règle | Température à un instant donné |
| `RG-030` | `378cb511-d51c-4f98-8ef1-b5f414836038` | règle | Mélange de deux liquides |
| `RG-040` | `dc39074f-2f6b-4c19-a81b-9fc68185e56d` | règle | Ordre des opérations : quand l'ajout a lieu |
| `RG-050` | `a2f67f9d-5d0f-4b38-b3a6-afd875065ea6` | règle | Instant d'atteinte de la température cible |
| `RG-060` | `ea136394-b2ab-4c96-8cd8-794b0601a8b8` | règle | Convergence |
| `CT-01` | `4b06688f-6465-4532-aa52-0db400bee2f4` | cas de test | Refroidissement simple |
| `CT-02` | `cfd6fbbf-0aad-4bc7-8951-4c3a53e87223` | cas de test | Ordre des opérations : le cas central |
| `CT-03` | `0fbe682c-8776-4e0b-8379-ae7aa9658f96` | cas de test | Cible inatteignable |
| `CT-04` | `b4ee9744-60d3-4131-86d6-bd96fe9c3416` | cas de test | Cible déjà atteinte |
| `CT-05` | `37fcab9a-65f1-40e5-920f-e411021411df` | cas de test | Cible hors horizon |
| `CT-06` | `4d6f1ab4-7ded-4604-b857-b74f36df9407` | cas de test | Invariance d'échelle (kelvins) |
| `CT-07` | `1645cc91-0461-4d1f-9226-2800a352d859` | cas de test | Masse ajoutée nulle |
| `CT-08` | `7f647fe8-a8cc-4ad3-9d4c-5b3cf790b824` | cas de test | Coefficient négatif |
| `P-01` | `96718c6e-7ebb-45a8-becc-7dacb44b8db6` | paramètre | Coefficient de refroidissement — tasse en céramique ouverte |
| `P-02` | `6fa6e358-eac1-4052-9fe7-8057f5fa79d8` | paramètre | Coefficient de refroidissement — mug isotherme fermé |
| `P-03` | `87cb7eb4-c0f7-44bd-90b4-9e00c395c621` | paramètre | Température cible de dégustation par défaut |
| `P-04` | `1b4f0d75-6953-45a5-9134-d3e656c3759c` | paramètre | Horizon de prévision |
| `P-05` | `75cfe6fc-7213-4967-a81f-f4e80c55c727` | paramètre | Tolérance de convergence sur l'instant |
| `P-06` | `6ea114e2-f73a-4512-8cd4-9656c42c8060` | paramètre | Nombre maximal d'itérations |
| `P-07` | `6f2b55b1-5c6a-451a-a794-963a915f7b63` | paramètre | Écart minimal à la température ambiante pour que la cible soit réputée |
| `EX-01` | `47db7e61-d39c-4d07-ab94-810c0e0032fd` | exigence | Le calcul embarqué **n'émet aucune requête réseau** : il fonctionne ho |
| `EX-02` | `b21a02b4-bdc9-4b4b-af4e-31fdda02e7b7` | exigence | Le fichier de paramètres téléchargé est **signé**, et une signature in |
| `EX-03` | `e8c9de71-4877-459f-8946-f1ebfd76f1d8` | exigence | Le code respecte le **standard de codage interne** `STD-DEV-2` |
| `INV-01` | `b11ac534-4ee3-4ae5-8395-6f6b65dca68d` | invariant | Si `T(0) > T_ambiante`, alors `T(t)` est strictement décroissante |
| `INV-02` | `7c52110c-fc9a-4bad-8b2e-30511a8b1751` | invariant | `T(t)` reste strictement comprise entre `T_ambiante` et `T(0)` |
| `INV-03` | `53836c25-75aa-4f5c-882f-2a05f28f1882` | invariant | `T(t)` tend vers `T_ambiante` quand `t` croît, sans jamais l'atteindre |
| `INV-04` | `cdc085b1-e472-43e3-be7b-2d28930aeed1` | invariant | Invariance par changement d'origine de l'échelle** : ajouter une const |
| `INV-05` | `6b37cd54-28a1-49d2-a574-60c454308e15` | invariant | Encadrement du mélange** : `T_melange` est comprise entre les deux tem |
| `INV-06` | `67c240de-37c7-4d40-a6cc-330545759eb9` | invariant | Monotonie vis-à-vis de `k`** : à conditions égales, un `k` plus grand  |
| `INV-07` | `3e382a1c-177b-47e7-bb99-0a700f03541a` | invariant | Monotonie vis-à-vis de la cible** : une cible plus basse donne un inst |
| `INV-08` | `c3221013-33d7-482b-bb07-98fa142945ca` | invariant | Le calcul est déterministe : deux exécutions sur la même entrée donnen |
| `E-PARAM-001` | `b19ad839-f72a-4446-a9ec-f02a8aa522a1` | cas d'erreur | `coefficient_refroidissement ≤ 0` |
| `E-PARAM-002` | `b757d637-570b-4594-bad2-040776993e62` | cas d'erreur | Une masse ou une capacité massique de la boisson est nulle ou négative |
| `E-ENTREE-001` | `ef5edef2-6e13-4c75-83e6-e4d82f6dfe6a` | cas d'erreur | Ni `instant_demande` ni `temperature_cible` n'est fourni |
| `E-HORIZON-001` | `089406dd-f578-4bef-8d4b-3acbefc50e82` | cas d'erreur | `instant_demande > P-04` |
| `E-CONV-001` | `1b5551ba-9d1b-44e3-beb3-89aac7d2fb26` | cas d'erreur | `P-06` itérations sans convergence à `P-05` près |
| `Q-01` | `c048bd3f-7a20-4669-b9c7-2b29d366394c` | question | Faut-il ajouter un terme de rayonnement et d'évaporation pour les temp |
| `Q-02` | `6819e7f2-3111-47f1-bc56-070aa8d47524` | question | L'usage laboratoire demande une température ambiante variable dans le  |
| `Q-03` | `cf4f641d-263d-421b-84fa-4869d01a6a03` | question | `RG-060` (non-convergence) et `E-HORIZON-001` ne sont couverts par auc |
| `Q-04` | `aec32bd5-c60a-44a3-9485-5f4187d080fd` | question | `P-07` vaut 0,10 °C. Cette marge devrait-elle dépendre de l'écart init |
| `Q-05` | `cb226d98-6d59-4412-9083-0b7dfb034ca7` | question | Tranchée le 2025-12-10 :* l'instant d'ajout doit-il avoir une valeur p |
