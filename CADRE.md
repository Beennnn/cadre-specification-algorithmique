# Cadre de spécification algorithmique métier

*Version 1.0 — document de référence*

---

## Sommaire

- [0. En une page](#0-en-une-page)
- [1. Le principe](#1-le-principe)
  - [1.1 Le problème qu'on cherche à résoudre](#11-le-problème-quon-cherche-à-résoudre)
  - [1.2 Le principe : le métier écrit la loi, l'IT écrit la machine](#12-le-principe--le-métier-écrit-la-loi-lit-écrit-la-machine)
  - [1.3 Le critère d'acceptation : le test de la double implémentation](#13-le-critère-dacceptation--le-test-de-la-double-implémentation)
  - [1.4 La frontière métier / technique](#14-la-frontière-métier--technique)
  - [1.5 Les faux amis](#15-les-faux-amis)
- [2. Le pseudo-langage](#2-le-pseudo-langage)
  - [2.9 Adapter le cadre au calcul scientifique](#29-adapter-le-cadre-au-calcul-scientifique)
- [3. L'anatomie d'une spécification](#3-lanatomie-dune-spécification)
- [4. La fiche de contraintes : ce qui permet de choisir le langage et l'architecture](#4-la-fiche-de-contraintes--ce-qui-permet-de-choisir-le-langage-et-larchitecture)
- [5. Le jeu d'essai : l'oracle](#5-le-jeu-dessai--loracle)
- [6. Le processus et la gouvernance](#6-le-processus-et-la-gouvernance)
- [7. Mise en place progressive](#7-mise-en-place-progressive)
- [8. Anti-patterns](#8-anti-patterns)
- [9. Les exemples : lecture guidée](#9-les-exemples--lecture-guidée)
- [Annexe A — Aide-mémoire du pseudo-langage](#annexe-a--aide-mémoire-du-pseudo-langage)

---

## 0. En une page

**Ce qui change.** L'expert métier ne livre plus un classeur Excel, un notebook ou un
script. Il livre une **spécification algorithmique** : un document versionné, en
markdown, dans le dépôt Git, écrit dans un pseudo-langage contraint.

**Ce que la spécification contient obligatoirement.**

1. Le **vocabulaire** du domaine (glossaire), utilisé partout de la même façon.
2. Le **contrat** : les entrées exigées et les sorties garanties, typées avec unité,
   devise, fuseau, précision et domaine de validité (§3.1).
3. Les **règles** numérotées (`RG-010`, `RG-020`…), chacune écrite en pseudo-langage,
   chacune traçable jusqu'au code et jusqu'aux tests.
4. Les **paramètres** (seuils, taux, barèmes) séparés des règles, avec leur
   qui peut les changer, leur circuit de validation et leur date d'effet.
5. Les **invariants** — ce qui doit rester vrai quoi qu'il arrive.
6. Le **jeu d'essai** : les cas nominaux, les cas aux limites, les cas d'erreur, avec
   les résultats attendus calculés à la main.
7. La **fiche de contraintes** : volumes, latence, précision, rejouabilité,
   auditabilité, disponibilité, fréquence de changement — chiffrés, en unités métier.
8. Les **questions ouvertes**, nommées, avec un décideur et une échéance.

**Ce que la spécification ne contient jamais.** Un nom de langage, de base de données,
de bibliothèque, de serveur. Une structure de données technique. Une optimisation. Un
« il faudra que ce soit rapide ». Un `try / except`.

**Le contrat.** Le métier est souverain sur *ce qui est calculé*. L'IT est souveraine
sur *comment c'est calculé*. Quand un développeur découvre un cas non prévu, il
n'invente pas : il ouvre une **question** dans la spécification, et le métier tranche.

---

## 1. Le principe

### 1.1 Le problème qu'on cherche à résoudre

Dans beaucoup d'organisations, la connaissance algorithmique est détenue par des gens
dont le métier n'est pas de développer : actuaires, analystes de risque, ingénieurs
process, tarificateurs, chargés de conformité, chercheurs, contrôleurs de gestion. Ces
personnes codent quand même — parce que c'est le seul moyen de vérifier qu'une idée
tient. **Ce travail est légitime, et souvent excellent** : il remplit exactement l'office
qu'on lui demande, démontrer qu'un raisonnement fonctionne.

La difficulté n'apparaît qu'ensuite, quand ce code doit servir à autre chose.

#### Ce qu'exige un code de production, et à qui cette expertise appartient

Un logiciel qui part en production doit tenir un **niveau de qualité assuré dans la
durée** : il devra être repris par d'autres, testé, corrigé sous contrainte de temps,
porté sur une autre plate-forme, tenir la montée en charge, être exploité, tracé,
sécurisé, et rester modifiable après le départ de ses auteurs.

Ces exigences ne s'improvisent pas. **Elles constituent le métier de développeur et
d'architecte logiciel** — un métier qui a ses techniques, ses arbitrages et son
apprentissage, au même titre que l'actuariat ou la métrologie.

> **On ne peut pas raisonnablement attendre cette expertise de personnes dont le métier
> est de concevoir et de maintenir les besoins algorithmiques métier** — pas plus qu'on
> n'attendrait d'un développeur qu'il maîtrise la tarification d'un contrat ou la
> physique d'un capteur.

Il ne s'agit donc pas de compétences manquantes, mais de **deux expertises distinctes**,
qu'aucune organisation ne devrait demander aux mêmes personnes de cumuler.

#### Les trois trajectoires habituelles, et leur coût

| Trajectoire | Ce qui se passe | Le coût, pour tout le monde |
|---|---|---|
| **Le prototype passe en production** | Il devait tenir « en attendant » ; il tourne encore huit ans plus tard | Il n'a jamais été conçu pour être maintenu par d'autres : ni jeu de tests, ni trace des décisions. La charge repose sur une seule personne, qui ne peut plus s'en détacher |
| **Le prototype est réimplémenté** | L'IT le reprend avec les exigences de production | Faute d'un énoncé des règles indépendant du code, la réimplémentation doit **deviner l'intention à partir de la mise en œuvre**. Les écarts se découvrent tard, souvent par un client ou un auditeur |
| **Le prototype sert de point de départ** | Le développeur repart d'une note de quelques pages | Les décisions que le prototype avait tranchées sans les écrire — arrondis, ordres d'application, valeurs par défaut, départages — sont reprises au jugé |

**Aucune de ces trajectoires ne relève d'une négligence.** Un prototype a pour but de
vérifier qu'un raisonnement tient ; il n'a jamais eu pour but de consigner des décisions
ni d'être maintenu dix ans. On lui demande après coup ce qu'on ne lui avait pas demandé.

#### Le vrai point commun

**Le code mélange indissociablement l'intention et la mise en œuvre.** On ne distingue
plus « on retient le prix le plus bas » — une règle de gestion, décidée, opposable — de
« on trie la liste puis on prend le premier » — un choix d'implémentation, remplaçable.

Et l'effet symétrique est tout aussi coûteux : **quand la spécification est floue, c'est
le développeur qui tranche des questions métier**, sans le savoir et sans mandat. Il
choisit un arrondi, une valeur par défaut sur une donnée absente, un ordre d'application
de deux remises. Ces décisions ont des conséquences comptables, contractuelles et parfois
réglementaires.

Dans un sens comme dans l'autre, **on demande à chacun de faire le métier de l'autre** —
et c'est cela, et cela seul, que la méthode cherche à corriger.

### 1.2 Le principe : le métier écrit la loi, l'IT écrit la machine

> L'expert métier produit un **texte normatif** décrivant *ce qui doit être calculé*.
> Le développeur produit un **programme** décidant *comment le calculer*.

Ce n'est pas une répartition de tâches, c'est une répartition de **souveraineté** :

- Le métier n'a pas à justifier ses règles auprès de l'IT — il les pose, et il en assume les conséquences.
- L'IT n'a pas à justifier ses choix techniques auprès du métier — elle les fait, elle
  en assume les conséquences (coût, délai, exploitabilité).
- **Aucun des deux ne se prononce dans le champ de l'autre.** Une spécification qui dit
  « stocker dans une table indexée sur la référence produit » est aussi fautive qu'un
  développeur qui décide seul d'arrondir au centime inférieur.

#### Ce que chacun décide, et ce qu'il ne décide pas

| | **Le métier décide** | **La technique décide** |
|---|---|---|
| | Les règles, et leur ordre quand il change le résultat | Le langage, l'architecture, les structures de données |
| | Les paramètres, seuils, plafonds, barèmes | L'algorithme retenu, les optimisations, la parallélisation |
| | Les arrondis, leur sens, le sort du reste | Le stockage, le cache, le déploiement, l'observabilité |
| | Le départage des ex æquo | Le découpage du code et son organisation |
| | Le comportement sur donnée absente ou invalide | Comment la qualité tient dans la durée |
| | Le mode dégradé quand une dépendance tombe | |
| | Les contraintes chiffrées en unités métier | |
| | Les données de référence et leur validation | |
| | Si un écart constaté est significatif | |

**Les deux dérives que ce partage corrige** sont symétriques, et aucune n'est volontaire :

| Le métier fait le travail du développeur | Le développeur fait le travail du métier |
|---|---|
| Il écrit du code qui finit en production | Il choisit un arrondi, et son sens |
| Il choisit des structures de données, optimise | Il décide quoi faire d'une donnée absente |
| Il impose une technologie dans la spécification | Il départage deux valeurs ex æquo |
| Il fige un format de stockage, un ordre d'exécution | Il fixe un seuil, une valeur par défaut |
| Il se prononce sur la performance | Il décide du comportement en mode dégradé |

À gauche, on demande une expertise logicielle à qui ne l'a pas. À droite, on laisse
trancher des questions comptables, contractuelles ou réglementaires à quelqu'un qui n'a ni
le mandat ni les éléments — et le plus souvent sans qu'il s'en aperçoive.

L'objectif de forme qui découle du principe : **la spécification doit contraindre le
résultat et libérer le chemin.** Chaque fois qu'on peut décrire un résultat plutôt
qu'un parcours, on le fait — c'est ce qui laisse au développeur la latitude de
vectoriser, paralléliser, déporter dans une base, mettre en cache, sans jamais trahir
la règle.

### 1.3 Le critère d'acceptation : le test de la double implémentation

Une spécification est finie quand :

> **Deux développeurs qui ne se parlent pas, dans deux langages différents, produisent
> des programmes qui donnent le même résultat sur tout le jeu d'essai — et sur les cas
> auxquels personne n'avait pensé.**

C'est un critère opérationnel, pas une figure de style. Il se pratique :

- **En vrai, une fois**, sur la première spécification écrite dans l'organisation : deux
  personnes implémentent, on compare. Les écarts constatés sont la liste exacte de ce
  qui manque au cadre. C'est l'exercice d'étalonnage le plus rentable de la démarche.
- **Par la pensée, à chaque relecture** : le relecteur cherche activement un endroit où
  il pourrait, de bonne foi, coder autre chose que ce que l'auteur avait en tête.

Corollaire utile : *si une variation d'implémentation change le résultat, il manque une
règle. Si elle ne change que le temps ou la mémoire, tout va bien.*

### 1.4 La frontière métier / technique

La règle de partage tient en une phrase :

> **Si modifier ce point change un résultat observable par un client, un comptable, un
> régulateur ou un opérateur → c'est du métier.
> Si ça ne change que le temps, la mémoire, le coût, ou la façon de déployer → c'est de
> la technique.**

| Relève du métier (dans la spécification) | Relève de la technique (hors spécification) |
|---|---|
| Ce qui est calculé, et à partir de quoi | Le langage, le framework, la bibliothèque |
| L'ordre des opérations quand il change le résultat | L'ordre des opérations quand il ne le change pas |
| Les arrondis, la précision, les unités, les devises | Le type numérique concret retenu |
| Le comportement en cas de donnée absente ou invalide | Le mécanisme de validation, la gestion des exceptions |
| Le départage des ex æquo | L'algorithme de tri utilisé |
| Les seuils, plafonds, planchers, barèmes | Le stockage des paramètres |
| La date d'effet d'une règle | Le mécanisme de versionnement |
| Le mode dégradé attendu quand une dépendance tombe | Le mécanisme de reprise, les tentatives, les délais |
| Les volumes, la latence acceptable, la rejouabilité | L'architecture qui les satisfait |
| Ce qui doit être justifiable et conservé | Le format et le support de la trace |

### 1.5 Les faux amis

Ce sont les points qui *ressemblent* à de la technique et qui sont du métier. Ils sont
la cause majoritaire des écarts entre la spécification et le programme. Une
spécification doit les traiter explicitement, toujours :

1. **L'arrondi.** À quelle étape ? À combien de décimales ? Dans quel sens
   (commercial, inférieur, supérieur, au pair) ? Et surtout : *que fait-on du centime
   résiduel* quand la somme des lignes arrondies ne retombe pas sur le total arrondi ?
2. **L'ordre des opérations non commutatives.** Deux remises de 10 % et de 5 € ne
   donnent pas le même résultat selon l'ordre. Le métier tranche, et écrit pourquoi.
3. **Le départage des ex æquo.** « On retient l'offre la moins chère » — et s'il y en a
   deux ? Sans règle de départage, le résultat dépend de l'ordre de lecture des données,
   donc de l'implémentation, donc il n'est pas reproductible.
4. **Les valeurs absentes.** Une donnée manquante : on rejette, on prend une valeur par
   défaut, on ignore la ligne, on dégrade ? Chacune de ces réponses est une décision
   métier différente, aux conséquences différentes.
5. **Le temps.** Quel fuseau ? Quel calendrier de jours ouvrés ? Quelle heure de
   bascule ? Quelle date fait foi : la date de l'événement, celle de sa saisie, ou
   celle du calcul ? Et lors d'un rejeu, on utilise les règles d'aujourd'hui ou celles
   en vigueur à la date de l'événement ?
6. **Les unités et devises.** Et le taux de conversion : quel taux, à quelle date, de
   quelle source, arrondi comment ?
7. **Les bornes.** Un seuil est-il atteint à `≥` ou à `>` ? Un plafond écrête-t-il ou
   rejette-t-il ? Une boucle a-t-elle un nombre maximal d'itérations, et que se
   passe-t-il si on l'atteint ?
8. **Le comportement en cas d'erreur.** « Le service de fidélité est indisponible » :
   on refuse la commande, ou on la passe sans fidélité en prévenant le client ? C'est
   une décision commerciale, pas une décision d'exploitation.

Et symétriquement, les points qui *ressemblent* à du métier et qui n'en sont pas : le
cache, le traitement par lots ou au fil de l'eau, le nombre de tentatives, l'index, la
montée en charge, le choix entre un moteur de règles et du code. Le métier fournit les
**contraintes** (§4) ; il ne choisit pas la solution.

---

## 2. Le pseudo-langage

### 2.1 Ce qu'on cherche

Un pseudo-langage n'est pas un langage de programmation simplifié : c'est du **français
discipliné**. Il doit être lisible par un juriste, un auditeur ou un nouveau venu dans
l'équipe, et exécutable mentalement sans ambiguïté par un développeur.

Trois qualités à tenir, dans cet ordre :

1. **Non ambigu** — une seule lecture possible.
2. **Lisible par un non-informaticien** — c'est ce qui permet la relecture par les pairs
   métier, qui est le vrai contrôle qualité de la démarche.
3. **Neutre technologiquement** — rien qui présuppose un langage ou une structure.

> On résiste à la tentation d'un langage formel complet (B, Alloy, TLA+, Z). Ces
> langages sont supérieurs sur le point 1 et disqualifiés sur le point 2 : une
> spécification que le métier ne peut pas relire est une spécification que le métier
> n'écrit pas.

### 2.2 Le lexique

Le pseudo-langage tient en **une trentaine de mots**. C'est délibéré : un lexique qu'on ne
peut pas mémoriser en une lecture n'est pas adopté.

> **Le lexique est fermé.** Tout mot qui n'y figure pas est du français ordinaire, pas un
> mot-clé. On n'en ajoute un que si son absence a causé une ambiguïté réelle — jamais par
> confort.

#### Structure

| Mot-clé | Rôle |
|---|---|
| `DÉFINIR nom(param : Type) : Type` | déclarer une fonction |
| `ENTRÉES` · `SORTIES` | le contrat |
| `PRÉCONDITIONS` · `POSTCONDITIONS` · `INVARIANTS` | ce qu'on exige, garantit, maintient |
| `SOIT nom = …` | **introduire** un nom — jamais le réaffecter (§2.4) |
| `RETOURNER …` | produire le résultat |
| `SIGNALER ERREUR E-xxx « … »` | signaler une erreur **métier** |

#### Conditions

| Mot-clé | Rôle |
|---|---|
| `SI … ALORS` · `SINON SI … ALORS` · `SINON` · `FIN SI` | l'alternative |

Un `SI` a **toujours** son `SINON` (`C-08`). Au-delà de deux conditions combinées, on
n'imbrique pas : on écrit une **table de décision** (§2.6).

#### Itération

| Mot-clé | Rôle |
|---|---|
| `POUR CHAQUE x DANS c … FIN POUR` | parcourir sans indice |
| `TANT QUE cond … FIN TANT QUE` | itérer — **à justifier**, avec critère d'arrêt, maximum d'itérations et comportement en cas de non-convergence (`C-13`) |

#### Ensembles — le style à préférer

| Mot-clé | Rôle |
|---|---|
| `SOMME` · `MOYENNE` · `MINIMUM` · `MAXIMUM` · `NOMBRE DE` | agréger |
| `FILTRER c OÙ cond` | restreindre |
| `TRIER c PAR a CROISSANT \| DÉCROISSANT` | ordonner, avec départage explicite |
| `REGROUPER c PAR critère` | partitionner |
| `LE PREMIER` · `LE DERNIER` | extraire d'une collection **ordonnée** |
| `IL EXISTE` · `AUCUN` · `TOUS` | quantifier |

**Chaque fois qu'une opération d'ensemble remplace une boucle, on la préfère** : elle
décrit un résultat et laisse le développeur libre de son chemin (§2.5).

#### Opérateurs et valeurs

| | |
|---|---|
| Arithmétique | `+` `−` `×` `÷` `^` |
| Comparaison | `=` `≠` `<` `≤` `>` `≥` · `ENTRE a ET b` |
| Logique | `ET` · `OU` · `NON` |
| Appartenance | `DANS` |
| Valeurs | `VRAI` · `FAUX` · `ABSENT` |
| Fonction imposée | `ARRONDIR(valeur, décimales, mode)` |

`ABSENT` désigne **l'absence métier** d'une valeur facultative. Ce n'est pas un `null`
technique : son traitement est une décision métier, déclarée (`C-12`).

Les fonctions mathématiques usuelles — racine, exponentielle, logarithme, sinus, arc
tangente — s'écrivent avec leur notation habituelle et ne sont pas des mots-clés.

#### Ce que le lexique n'a pas, et pourquoi

| Absent | À la place | Pourquoi |
|---|---|---|
| `SELON` / `CAS` | une **table de décision** | elle rend la complétude vérifiable, un `SELON` non |
| `SORTIR` · `CONTINUER` · `ALLER À` | un critère d'arrêt déclaré | une sortie anticipée cache une condition qui n'a pas été écrite |
| `NULL` · `NIL` | `ABSENT` | l'absence est une notion métier, pas une valeur technique |
| `+=` · `++` · toute réaffectation | un **nouveau nom** | l'immutabilité (§2.4) |
| `ESSAYER` / `ATTRAPER` | `SIGNALER ERREUR` | la spécification dit **quelle erreur métier**, pas comment elle se propage |
| Fonctions anonymes, pointeurs, généricité, héritage | rien | ce sont des moyens d'implémentation |

### 2.3 Types, unités, domaines

**Toute grandeur porte son unité. Toujours.** C'est la règle qui rapporte le plus pour
ce qu'elle coûte.

```
montant_ht        : Montant(EUR, 2 décimales)
poids             : Poids(kg, 3 décimales, ≥ 0)
taux_remise       : Taux(0,0000 .. 1,0000, 4 décimales)
date_commande     : Horodatage(fuseau Europe/Paris)
date_effet        : Date(calendrier grégorien)
quantite_commandee          : Entier(≥ 1)
reference_produit : Identifiant(texte, 3 à 20 caractères, unique)
zone_livraison    : Énuméré { FRANCE_METRO, UE, HORS_UE }
```

#### La ligne exacte entre le type et sa représentation

Il faut être précis ici, parce que la formule courante « pas de types techniques dans une
spécification » est **trop large** et fait perdre de l'information utile.

| Obligatoire dans la spécification | Interdit dans la spécification |
|---|---|
| La **famille de type** : entier, flottant, décimal exact, chaîne, booléen, énuméré, horodatage, durée | La **représentation machine** : `int32`, `float64`, `varchar(50)`, `NUMBER(10,2)` |
| La **précision** : nombre de décimales, de chiffres significatifs, de caractères | Le nombre d'octets, l'encodage, l'ordre des octets |
| La **plage** de valeurs admissibles | La valeur sentinelle choisie pour l'absence |
| La **dimension** physique, quand il y en a une | La bibliothèque de calcul retenue |
| L'**unité pivot** et l'**unité d'usage** | Le format de sérialisation |

> **Le discriminant : la famille de type est une propriété du domaine ; la
> représentation est un choix du développeur.**
>
> « Un entier entre 1 et 999 » est une affirmation métier — c'est le domaine qui dit qu'on
> ne commande pas 2,5 articles ni 10 000. « `int16` » est une décision d'implémentation.
> De même, « décimal exact à 2 décimales » est métier — le [§4](#4-la-fiche-de-contraintes--ce-qui-permet-de-choisir-le-langage-et-larchitecture)
> montre que cette seule ligne peut éliminer des langages entiers ; « `BigDecimal` » est technique.

#### Dimension, unité pivot, unité d'usage

Une grandeur physique porte **trois informations distinctes**, et les confondre est la
première cause d'erreurs d'unité.

| | Ce que c'est | Exemple |
|---|---|---|
| **La dimension** | ce qu'on mesure, indépendamment de toute unité | une vitesse est une **longueur ÷ temps** |
| **L'unité pivot** | celle dans laquelle la valeur circule et se calcule | `m·s⁻¹` |
| **L'unité d'usage** | celle dans laquelle on la saisit et on l'affiche | `km/h` |

*« Pivot » au sens où tout converge vers elle : toute valeur entrante y est convertie une
fois, tout calcul s'y fait, toute valeur sortante en repart. « Usage » au sens de ce que
les gens du métier écrivent et lisent réellement.*

**Règle : l'unité pivot est l'unité SI de la dimension**, sauf décision contraire
**explicitement déclarée** — la température en `°C` plutôt qu'en `K` est l'exception
classique, légitime quand tout le domaine et toutes les références sont en Celsius.

Ce que cela rapporte, et c'est plus que de la propreté :

- **La conversion se fait une fois, à la frontière** — jamais au milieu d'une règle. Un
  `÷ 3,6` qui traîne dans un calcul est une erreur qui attend son tour.
- **La dimension se vérifie.** Une énergie qui serait obtenue en additionnant une force et
  une distance est détectable sans rien connaître au métier.
- **L'affichage cesse d'être une décision d'implémentation.** L'unité d'usage est
  écrite, donc discutée, donc assumée par le métier.

#### Une grandeur n'est jamais un nombre nu

Dans les règles, une grandeur est une **quantite_commandee** : une valeur **et** son unité,
indissociables. Il en découle une petite algèbre, qui se vérifie sans rien connaître au
métier :

| Opération | Ce qui est permis |
|---|---|
| Addition, soustraction, comparaison | **uniquement entre quantite_commandees de même dimension** |
| Multiplication, division | toujours permises — elles **produisent une nouvelle dimension** |
| Élévation à une puissance | permise ; la dimension est élevée à la même puissance |
| Fonction transcendante (exponentielle, logarithme, trigonométrie) | **uniquement sur une quantite_commandee sans dimension** |

La dernière ligne est celle qui attrape le plus d'erreurs : `exp(−k × t)` n'a de sens que
si `k × t` est sans dimension, donc si `k` est l'inverse d'un temps. Écrire un coefficient
de refroidissement « 0,03 » sans unité laisse passer l'erreur ; écrire `0,03 min⁻¹` la rend
impossible.

**La conversion se fait à la frontière, jamais dans une règle.** Un `÷ 3,6` au milieu d'un
calcul est une erreur qui attend son tour : le jour où l'unité d'usage change, il subsiste.

#### La capacité de conversion est une clause du contrat

Une fonction doit pouvoir **accepter et produire les unités d'usage attendues par ses
appelants**. Cette capacité n'est pas un détail d'interface : elle se déclare.

```
vitesse_praticable : Flottant(m·s⁻¹ ▸ km/h, 6 chiffres significatifs, > 0)
                     accepte { m/s, km/h, mph }
```

Ce que cela engage : la fonction reçoit une **quantite_commandee** — une valeur accompagnée de son
unité — et la convertit vers l'unité pivot **à l'entrée**, une fois. Elle produit
symétriquement ses sorties dans l'unité d'usage demandée. Refuser une unité non déclarée
est un cas d'erreur métier, pas une exception technique.

> **Ce que la spécification ne dit pas** : comment les quantite_commandees sont représentées, si la
> conversion est faite par une bibliothèque ou à la main, si le typage du langage porte
> l'unité. Elle dit **quelles unités doivent être acceptées**, et que la conversion a lieu
> une seule fois, au bord.

#### La notation

```
<nom> : <Famille>(<unité pivot>[ ▸ <unité d'usage>], <précision>, <plage>)
        [accepte { <unités admises> }]
        [incertitude <valeur ou loi>]
```

```
vitesse_praticable   : Flottant(m·s⁻¹ ▸ km/h, 6 chiffres significatifs, > 0)
montant_net_ht       : Décimal(EUR, 2 décimales, ≥ 0,00)
quantite             : Entier(sans dimension, 1 .. 999)
taux_remise          : Flottant(sans dimension, 4 décimales, 0,0000 .. 1,0000)
reference_produit    : Chaîne(3 .. 20 caractères, alphanumérique majuscule)
zone_livraison       : Énuméré{ FRANCE_METRO, UE }
date_commande        : Horodatage(UTC ▸ Europe/Paris, à la seconde)
tronque              : Booléen
```

#### Les symboles structurés : listes, tables, matrices

Un symbole structuré n'est jamais déclaré « une liste » tout court. Il déclare **le type
de ses éléments** et, pour chaque axe, **son étendue** et **ce que son indice signifie**.

> **Précision de vocabulaire, imposée par notre propre règle sur les homonymes**
> ([guide 2](guides/2-GLOSSAIRE.md)) : « dimension » est déjà pris par la dimension
> physique. Pour les structures, on dit **rang** (le nombre d'axes) et **axe**, et chaque
> axe a une **étendue** (sa taille). Une matrice de rang 2 a deux axes, chacun d'étendue
> déclarée.

```
lignes        : Liste[Ligne](1 .. 200, ordre significatif, sans doublon de référence)
trajet        : Liste[Segment](1 .. 3 000, ordre significatif)
profil        : Liste[ProfilSegment](= étendue de trajet)
bareme_port   : Table[TrancheDePoids → Décimal(EUR, 2 décimales)](3 entrées, clés disjointes)
sensibilite   : Matrice[Flottant(sans dimension, 6 chiffres significatifs)]
                axe 1 = paramètre   (étendue 4, ordre du §6)
                axe 2 = observation (étendue = étendue de trajet)
```

Cinq déclarations obligatoires, et chacune correspond à une erreur classique :

| Ce qu'on déclare | L'erreur que ça évite |
|---|---|
| **Le type des éléments** | une liste hétérogène dont personne ne sait ce qu'elle contient |
| **L'étendue de chaque axe** — fixe, bornée, ou liée à une autre grandeur | l'impossibilité de dimensionner, et le risque d'épuisement mémoire sur une entrée hostile |
| **La signification de chaque indice** | une transposition qui passe inaperçue : `sensibilite[i][j]` et `sensibilite[j][i]` se ressemblent beaucoup |
| **L'ordre** : significatif ou non | un tri « d'optimisation » qui change le résultat |
| **Les doublons** : admis ou non | deux lignes de même référence, traitées deux fois ou fusionnées selon l'implémentation |

Deux conventions posées une fois pour toutes, valables dans tout le cadre :

- **L'indexation commence à 1**, sauf déclaration contraire explicite.
- Pour une matrice, l'écriture est **ligne × colonne** ; le **stockage** — par lignes, par
  colonnes, creux, dense — reste un choix du développeur et ne figure jamais.

> **Une étendue non bornée est un défaut**, pas une souplesse. « Une liste de segments »
> sans borne supérieure interdit de dimensionner, empêche de tenir une contrainte de
> latence, et laisse une entrée hostile épuiser la mémoire. Si le métier ne sait pas
> borner, c'est une question ouverte, pas une omission.

Toute spécification manipulant des grandeurs physiques porte en outre une **table des
dimensions**, qui donne pour chacune sa dimension, son unité pivot, son unité
privilégiée et le facteur de conversion. C'est elle qu'on ouvre quand un chiffre semble
faux d'un facteur rond.

**Les règles emploient les identifiants du contrat, écrits à l'identique.** Si le contrat
déclare `prix_catalogue_ht`, la règle écrit `prix_catalogue_ht` — pas « le prix
catalogue ». La prose autour peut employer le terme du glossaire ; le pseudo-code, lui,
emploie l'identifiant. Deux raisons :

- le lien entre contrat et règle devient **vérifiable mécaniquement** (`C-01` à `C-03`) ;
- une entrée que plus aucune règle ne nomme devient **visible**, au lieu de survivre
  indéfiniment dans un contrat que personne ne relit.

Quand un champ est imbriqué et que son nom se répète d'une structure à l'autre — `masse`
dans deux objets différents —, on écrit le **chemin complet** : `boisson.masse`,
`ajout.masse`.

Pour les montants, on précise en plus, une fois pour toutes, dans la spécification :
la devise, le nombre de décimales, le mode d'arrondi par défaut, et l'étape à laquelle
l'arrondi intervient.

### 2.4 Nommer : portée globale et immutabilité

Deux règles seulement, et elles font ensemble beaucoup plus que séparément.

#### La portée d'un nom est le périmètre, pas la fonction

> Dans un périmètre donné, **un nom désigne une grandeur et une seule**, dans toutes les
> fonctions. Deux fonctions qui manipulent la même grandeur l'appellent pareil ; deux
> grandeurs différentes ne portent jamais le même nom, même dans deux fonctions qui ne se
> parlent pas.

Chaque nom porte une **description d'une phrase** : dans le contrat pour les grandeurs
locales à une fonction, dans le [catalogue des données](guides/3-DONNEES.md) pour celles
qui traversent une frontière.

Quand deux périmètres échangent, le nom est qualifié par le périmètre :
`energie.energie_disponible`. Quand un champ est imbriqué et que son nom se répète d'une
structure à l'autre, on écrit le chemin complet : `boisson.masse`, `ajout.masse`.

C'est cette règle qui rend **le parcours d'une grandeur suivable de bout en bout** : on
peut demander « où passe `montant_net_ht` ? » et obtenir une réponse, parce que le nom ne
change pas en route.

#### Deux portées, et une seule règle d'unicité

| Portée | Où le nom est visible | Où il est déclaré |
|---|---|---|
| **Périmètre** | dans toutes les fonctions du périmètre | le contrat (§4, §5) ou le [catalogue des données](guides/3-DONNEES.md) |
| **Interne** | dans le corps d'une seule fonction | un bloc **Grandeurs internes**, en tête des règles |

Une **grandeur interne** est un résultat intermédiaire qui ne traverse aucune frontière :
`energie_mecanique`, `assiette_panier`, `force_aerodynamique`. Elle **n'apparaît jamais**
dans un contrat, ni dans une sortie, ni dans une fiche de donnée — ces artefacts ne
décrivent que ce qui franchit une frontière.

> **Elle est décrite exactement comme les autres.** Nom, famille de type, unité pivot,
> précision, plage. La rigueur ne se relâche pas sous prétexte que la grandeur est
> interne : c'est souvent là que les erreurs d'unité se logent, précisément parce que
> personne ne les regarde.

**Et l'unicité du sens reste globale.** La portée restreint la *visibilité*, pas le
*sens* : un même nom ne désigne jamais deux notions différentes, fût-ce dans deux
fonctions qui ne se parlent pas. Sans cela, `--tracer` mentirait et le glossaire perdrait
son autorité.

Conséquence pratique sur la [chaîne de traitement](#32-la-chaîne-de-traitement) : une
grandeur produite et consommée **à l'intérieur d'une même étape** ne figure pas dans la
table — elle est dans la boîte. C'est ce qui distingue `force_totale`, qui passe d'une
étape à l'autre, de `force_aerodynamique`, qui n'existe qu'au sein de `ET-01`.

#### La forme des identifiants

**`snake_case`, en ASCII strict.** Deux décisions, deux raisons distinctes.

**1. ASCII strict — pas d'accent, pas de symbole grec.**

| Ce qu'on écrit | Ce qu'on n'écrit pas |
|---|---|
| `reduction_fidelite_ttc` | `réduction_fidélité_ttc` |
| `alpha`, `rho`, `sigma` | `α`, `ρ`, `σ` |
| `delta_temperature` | `Δtemperature` |

Trois raisons, dont la première est décisive :

- **Deux identifiants visuellement identiques peuvent différer.** `é` s'écrit soit en un
  point de code, soit en deux (`e` + accent combinant). Deux noms indiscernables à l'œil et
  distincts pour la machine : `C-01` ne les rapproche pas, `--tracer` en perd un, et
  personne ne comprend pourquoi.
- **Les homoglyphes grecs sont pires** : `α` et `a`, `ρ` et `p`, `ν` et `v` se confondent
  dans presque toutes les polices.
- **L'ASCII traverse toute la chaîne** sans incident : recherche, comparaison, noms de
  tests, chemins de fichiers, journaux, terminaux.

> **Le corps du texte reste en français accentué**, évidemment. La contrainte ne porte que
> sur les **identifiants** — ce qui est cité, tracé et comparé mécaniquement. Une règle
> peut parfaitement expliquer que `alpha` est l'angle de la pente en radians.

**2. `snake_case`, et non la convention du langage cible.**

C'est une vraie question, puisque l'une des implémentations est en Java et qu'on pourrait
en reprendre les conventions. **Trois arguments s'y opposent, et le dernier est décisif :**

- La spécification **survit au langage**. `SPEC-NRG-001` déclare une durée de vie de
  quinze ans ; adopter la convention d'un langage, c'est y faire entrer une décision
  technique par la fenêtre — exactement ce que le §1.4 interdit.
- Elle a des **lecteurs qui ne codent pas** : métier, audit, conformité. `montant_net_ht`
  se lit mieux que `montantNetHt` pour eux.
- **Il y a deux cibles.** Le fil rouge est implémenté en C embarqué **et** côté serveur.
  Adopter la convention de l'une privilégie une équipe et impose une traduction à
  l'autre. Le jour où une troisième cible apparaît, la question se rouvre.

**L'état de l'art donne la même réponse**, et il a un précédent solide : les langages de
description d'interface — *protobuf* en tête — imposent `snake_case` dans le fichier de
définition, **et chaque générateur applique ensuite la convention de son langage**. C'est
exactement notre situation, et c'est une pratique éprouvée à grande échelle.

**La correspondance est mécanique et fait partie du dossier de passation :**

| Spécification | Java | C | Python | C# |
|---|---|---|---|---|
| `montant_net_ht` | `montantNetHt` | `montant_net_ht` | `montant_net_ht` | `MontantNetHt` |
| `calculer_montant_a_payer` | `calculerMontantAPayer()` | `calculer_montant_a_payer()` | `calculer_montant_a_payer()` | `CalculerMontantAPayer()` |
| `P-07` plafond de remise | `PLAFOND_REMISE` | `PLAFOND_REMISE` | `PLAFOND_REMISE` | `PlafondRemise` |

> **La règle de correspondance est déclarée une fois, par cible, et elle est
> systématique.** C'est ce qui permet à la traçabilité de tenir : partant de
> `montant_net_ht` dans la spécification, on retrouve `montantNetHt` dans le code Java
> sans avoir à chercher.

#### Faut-il encoder la portée, le type et l'unité dans le nom ?

La question se pose, et elle mérite mieux qu'une préférence. **Réponse : non pour les
trois — et pour trois raisons différentes.**

**Ce que dit l'état de l'art.** La notation hongroise, due à Charles Simonyi, existe en
deux versions que tout le monde confond :

| | Ce que le préfixe encode | Verdict |
|---|---|---|
| **Hongroise « système »** | la **représentation** : `dwCount`, `lpszName`, `iIndex` | condamnée sans appel. Elle duplique ce que le type déclare déjà, et **se périme dès que le type change** — les recommandations de conception .NET l'interdisent explicitement |
| **Hongroise « applicative »** | la **nature** que le typage ne distingue pas : `usName` (chaîne non sûre) vs `sName` (sûre), `rwPosition` vs `colPosition` | défendue, notamment par Joel Spolsky (*Making Wrong Code Look Wrong*, 2005) : elle rend **visible à l'œil** un mélange que rien d'autre n'attrape — `rw = col` saute aux yeux |

La leçon est nette : **encoder ce qui est déjà déclaré et vérifiable est nuisible ;
encoder ce que rien ne vérifie peut être utile.**

**Notre arbitrage, point par point :**

| | Décision | Pourquoi |
|---|---|---|
| **La portée** | **non** | Elle est déjà **structurelle** : une grandeur est au contrat, ou dans le bloc des grandeurs internes. C'est sans ambiguïté et mécaniquement vérifiable (`C-37`). L'encoder en plus obligerait à **renommer** une grandeur interne promue en sortie — or un renommage doit être un événement décidé, pas un effet de bord de refactorisation |
| **Le type** | **non** | C'est exactement la hongroise « système ». La déclaration porte la famille, la précision et la plage ; un préfixe les duplique et se périme. Et dans une spécification, il n'y a aucun compilateur pour resynchroniser les deux |
| **L'unité** | **non — et ici c'est particulier** | Écrire `vitesse_kmh` **mentirait** : la grandeur circule en unité **pivot** (`m·s⁻¹`), et `km/h` n'est que son unité d'**usage**, qui varie selon le contexte d'affichage. Le nom porterait une information vraie à un seul endroit du parcours |

> Ce dernier point mérite qu'on s'y arrête, parce qu'il va **contre** une pratique
> répandue et fondée : suffixer les unités (`timeout_ms`, `distance_km`) est
> recommandé en logiciel scientifique, et l'histoire des confusions d'unités — jusqu'à la
> perte d'une sonde en 1999 pour un mélange entre livres-force-seconde et newtons-seconde —
> justifie amplement cette prudence.
>
> **Mais le suffixe d'unité résout un problème que nous avons résolu autrement, et mieux.**
> Là où un nom ne peut que *signaler* l'unité, notre notation la **déclare** (§2.3),
> l'algèbre des quantite_commandees interdit d'additionner deux dimensions différentes, et la
> conversion est confinée à la frontière. Ajouter le suffixe reviendrait à porter la même
> information à deux endroits — dont l'un se périmerait le jour où l'unité pivot change.

**Ce qui, en revanche, mérite pleinement de figurer dans le nom** — et c'est la bonne
moitié de la hongroise applicative : **le stade de transformation**, que ni le type ni
l'unité ne distinguent.

```
remise_panier_brute  →  remise_panier_retenue  →  remise_panier_ligne_ajustee
```

Ces trois grandeurs ont la même famille, la même unité, la même plage. **Rien d'autre
qu'un nom ne peut les distinguer** — et les confondre change le montant facturé. C'est
exactement le cas d'usage que Spolsky défend, et c'est déjà notre règle d'immutabilité.

> **En résumé : on ne code dans le nom que ce qui n'est déclaré nulle part ailleurs.** Ce
> qui est déclaré — portée, type, unité — reste déclaré, à un seul endroit, vérifiable.

#### Une fois valorisé, un nom ne change plus

> **Toute transformation produit un nouveau nom**, qui dit la transformation.

```
✗  remise_panier = 5,00
   remise_panier = remise_panier − écart          ← le nom ment désormais

✓  remise_panier_brute    = 5,00
   remise_panier_retenue  = remise_panier_brute − écrêtement
   remise_panier_ligne    = remise_panier_retenue × part
   remise_panier_ligne_ajustee = remise_panier_ligne + résidu
```

Ce que cela coûte : quelques noms de plus. Ce que cela rapporte, et c'est sans commune
mesure :

| | |
|---|---|
| **La chaîne des noms est la trace du calcul** | on lit l'algorithme dans les noms, sans dérouler les règles |
| **Aucune ambiguïté sur « à quel moment »** | « le montant net » ne veut plus dire trois choses selon l'endroit où on lit |
| **Le parcours devient vérifiable** | un nom valorisé deux fois est un défaut détectable mécaniquement |
| **Les cas de test se lisent** | chaque étape de la trace de calcul porte le nom de son résultat |
| **Le développeur reste libre** | l'immutabilité est une propriété du **texte**, pas de l'implémentation : rien n'interdit de réutiliser une case mémoire |

Pour une accumulation, on n'écrase pas : on **indexe**.

```
✗  energie_cumulee = energie_cumulee + energie_segment

✓  energie_cumulee(i) = energie_cumulee(i − 1) + energie_segment(i)
   avec energie_cumulee(0) = 0
```

> Cette règle est empruntée à une pratique bien établie des compilateurs — l'affectation
> unique — mais elle sert ici un tout autre but : non pas optimiser, mais **rendre le
> texte lisible et le parcours traçable**. Un lecteur qui voit `montant_net_ligne` sait
> qu'il n'existe qu'une seule valeur portant ce nom, et qu'aucune règle plus loin ne la
> changera dans son dos.

### 2.5 Décrire un résultat, pas un parcours

C'est la règle de style la plus importante, parce que c'est elle qui **libère
l'architecture**.

| ✗ On impose un parcours | ✓ On décrit un résultat |
|---|---|
| `POUR i DE 1 À n : total = total + ligne[i].montant` | `SOIT total = SOMME DES montant DES lignes` |
| « on parcourt les offres et on garde la meilleure » | « l'offre retenue est celle de prix minimal ; en cas d'égalité, celle de référence alphabétiquement la plus petite » |
| « on boucle tant qu'il reste du budget » | « on retient le plus grand sous-ensemble de commandes dont la somme n'excède pas le budget, par ordre de priorité décroissante » |

Formulé en résultat, le développeur peut choisir une somme en mémoire, une agrégation
SQL, un calcul distribué ou un cache incrémental : **aucun de ces choix ne peut trahir
la règle**. Formulé en parcours, il croit devoir reproduire la boucle, et la première
optimisation devient un risque de régression métier.

Corollaire : une boucle explicite dans une spécification doit être **justifiée** — en
général parce que l'itération est réellement séquentielle (calcul récursif, application
successive d'un barème par tranches, convergence itérative). Dans ce cas, on précise le
critère d'arrêt **et** le nombre maximal d'itérations **et** ce qui se passe si on
l'atteint.

### 2.6 Les tables de décision

Dès qu'une règle combine plus de deux conditions, on abandonne les `SI` imbriqués pour
une table. Une table de décision a une propriété que le texte n'a pas : **on voit tout
de suite s'il manque une ligne**.

| Code promo | Remise quantité déjà appliquée | Client adhérent | Remise panier appliquée |
|---|---|---|---|
| absent | — | — | aucune |
| non cumulable | oui | — | aucune (`RG-032`) |
| non cumulable | non | — | selon barème |
| cumulable | — | — | selon barème |
| expiré | — | — | erreur `E-PROMO-002` |

Règle de complétude : **toute combinaison possible des colonnes d'entrée apparaît dans
exactement une ligne**, `—` signifiant « quelle que soit la valeur ». Si une
combinaison n'apparaît nulle part, la spécification est incomplète ; si elle apparaît
deux fois, elle est contradictoire. Cette vérification est mécanique — elle peut se
faire en revue en trente secondes, et c'est le meilleur détecteur de trou connu.

### 2.7 Ce qu'on n'écrit pas

- Aucun nom de langage, de bibliothèque, de base de données, de service technique.
- Aucun type technique, aucune structure de données (« tableau », « dictionnaire »,
  « table de hachage »).
- Aucune considération de performance (« il faut que ce soit rapide », « éviter les
  boucles imbriquées ») — elles vont dans la fiche de contraintes (§4), chiffrées.
- Aucune gestion technique d'erreur : pas de tentatives, de délais d'attente, de
  transactions, de journalisation. La spécification dit *quelle erreur métier est
  signalée et ce qui se passe alors*, pas comment on la propage.
- Aucune entrée/sortie : ni lecture de fichier, ni appel réseau, ni affichage. Une
  spécification décrit une **fonction** : mêmes entrées → mêmes sorties.
- Aucun état global mutable.
- Aucun « etc. », « et ainsi de suite », « on gère les cas particuliers », « comme
  d'habitude », « le bon sens ». Ce sont les quatre mots qui coûtent le plus cher.

### 2.8 Identifiants et traçabilité

Chaque règle porte un identifiant **stable et jamais réutilisé** :

- `RG-xxx` — règle de gestion
- `P-xx` — paramètre
- `E-xxx` — cas d'erreur métier
- `INV-xx` — invariant
- `CT-xx` — cas de test
- `Q-xx` — question ouverte

Ces identifiants sont le fil qui relie tout :

```
RG-080 (spécification)
   ├── cité en commentaire dans le code qui l'implémente
   ├── cité dans le nom ou la description des tests automatisés
   └── couvert par CT-04 et CT-07 du jeu d'essai
```

Ce qu'on gagne : quand la règle change, on trouve le code en une recherche ; quand un
test casse, on sait quelle décision métier est en cause ; quand un auditeur demande
« où est implémentée cette règle », la réponse prend dix secondes.

Un identifiant n'est **jamais recyclé**. Une règle supprimée reste dans le document,
barrée, avec sa date de fin d'effet — parce qu'un calcul rejoué sur une période
antérieure doit pouvoir s'y référer.

#### Trois couches d'identité, et une seule est durable

`RG-010` est lisible, mais il n'est unique que **dans son document**. Deux périmètres
emploient tous deux `RG-010` ; le jour où ils fusionnent, ou qu'une règle passe de l'un à
l'autre, la référence casse. Le libellé, lui, change encore plus souvent.

| Couche | Exemple | Change-t-elle ? | À quoi elle sert |
|---|---|---|---|
| **Le libellé** | « Prix unitaire retenu » | souvent | à lire |
| **L'identifiant lisible** | `RG-010` | au déplacement, à la fusion | à citer entre humains |
| **L'identité** | `980c488b-daf2-4834-b00e-c8d45668671a` | **jamais** | à référencer de façon sûre |

> **Tout objet identifié porte un UUID, attribué une fois et jamais modifié** — ni au
> renommage, ni au déplacement, ni au changement d'identifiant lisible. Un UUID n'est
> **jamais réattribué**, même après suppression de l'objet : une référence ancienne doit
> rester résoluble.

Le UUID n'a **aucune sémantique**. On n'y lit ni date, ni auteur, ni ordre de création :
toute tentative de l'interpréter est un défaut.

**Où il vit.** Dans une **annexe « Identités »** en fin de document, pour que le corps du
texte reste lisible. Le [registre](outils/) global — `registre.json` — est **généré** à
partir des annexes, jamais tenu à la main : un registre entretenu manuellement diverge.

```bash
python3 outils/identites.py --attribuer      # complète les identités manquantes
python3 outils/identites.py --registre       # produit registre.json
```

**Ce que cela change concrètement, et c'est considérable :**

| Sans identité durable | Avec |
|---|---|
| Renommer un champ ressemble à une **suppression suivie d'un ajout** — deux ruptures de contrat | Le UUID prouve que c'est **le même objet** : un renommage, pas une rupture |
| Déplacer une règle d'une spécification à une autre casse toutes les citations | La citation par UUID survit au déplacement |
| Deux équipes qui fusionnent doivent renuméroter — et perdre l'historique | Les identités ne collisionnent pas |
| « Est-ce la règle dont on parlait l'an dernier ? » se répond de mémoire | Se répond par une comparaison |

Et un bénéfice qu'on n'attendait pas : **le registre étant versionné, sa comparaison entre
deux versions dit exactement ce qui a été ajouté, renommé, déplacé ou supprimé.** C'est la
matière première de la [notice de changement](guides/7-VERSIONNER.md), produite
mécaniquement plutôt que de mémoire.

Les contrôles `C-28` et `C-29` vérifient que tout objet a une identité et qu'aucune n'est
partagée.

### 2.9 Adapter le cadre au calcul scientifique

Le cadre s'applique tel quel à un algorithme scientifique, mais trois points demandent
une adaptation explicite. Ils sont illustrés dans
[exemples/SPEC-THM-001-refroidissement.md](exemples/SPEC-THM-001-refroidissement.md).

**1. Spécifier le modèle et la tolérance, pas la méthode de résolution.**
« Intégrer par Runge-Kutta d'ordre 4 avec un pas de 1 s » est un choix d'implémentation
déguisé en règle : il interdit la solution exacte quand elle existe, et devient faux dès
que les conditions changent. Ce que l'expert doit écrire, c'est **l'équation, ses
hypothèses, et la tolérance** — puis laisser le développeur choisir. Même chose pour un
filtre ou une régularisation : au lieu de nommer la méthode, on énonce le **contrat**
qu'elle doit honorer (« réduire le bruit d'un facteur 2 sans déplacer le sommet d'un pic
de plus d'un demi-pas ») et le **protocole qui le vérifie**. C'est la contrepartie de la
liberté : on ouvre le choix sans ouvrir le risque.

**2. Dédoubler — voire tripler — le critère d'acceptation.**
Le test de la double implémentation (§1.3) suppose une égalité exacte, qui n'a pas de
sens sur des grandeurs continues. Il faut le scinder :

| | **Reproductibilité** | **Justesse numérique** | **Validité du modèle** |
|---|---|---|---|
| Question | Deux implémentations conformes donnent-elles le même nombre ? | Le nombre est-il la vraie solution des équations ? | Les équations décrivent-elles la réalité ? |
| S'évalue contre | La définition de la spécification | Une solution analytique ou un cas de synthèse | Des mesures réelles |
| Ordre de grandeur | 10⁻⁹ relatif, égalité stricte sur les décomptes et les indicateurs | 10⁻⁶ à quelques pour mille | quelques pour cent, selon le domaine |
| Un dépassement signifie | Une implémentation n'est pas conforme, **ou la spécification est ambiguë** | La méthode ou son pas est inadapté | Les hypothèses sont sorties de leur domaine — **pas un défaut du programme** |
| Se contrôle | En recette, à chaque livraison | En recette, sur les cas de synthèse | En validation de modèle, avec le métier |

Confondre ces niveaux est l'erreur la plus commune sur un logiciel scientifique : un
écart de 2 % à la mesure peut être parfaitement normal, un écart de 10⁻⁶ entre deux
implémentations ne l'est jamais. **C'est aussi ce qui protège l'équipe de
développement** : sans cette distinction, chaque écart de mesure devient un ticket de
bogue, et l'équipe cherche un défaut dans du code correct.

**3. Définir les conventions numériques que le continu masque.**
Trois faux amis s'ajoutent à ceux du §1.5, et ils sont propres au calcul scientifique :

- **L'interpolation.** « L'aire sous la courbe » n'a aucun sens pour un signal
  échantillonné tant qu'on n'a pas dit ce que vaut le signal *entre* deux points.
  Déclarer l'interpolation (affine, spline, autre) rend la grandeur exactement
  calculable ; l'omettre, c'est autoriser trapèzes, Simpson et splines à cohabiter —
  trois résultats différents, tous « corrects ».
- **La convergence.** Tout calcul itératif exige trois éléments dans la spécification :
  le critère d'arrêt, le nombre maximal d'itérations, et **ce qui est rapporté en cas de
  non-convergence** (une erreur ? le dernier itéré, marqué ?). Le premier est toujours
  écrit, le deuxième souvent, **le troisième presque jamais** — et c'est celui-là qui
  produit les résultats faux silencieux, quand le programme renvoie son dernier itéré
  comme s'il avait convergé.
- **Les asymptotes et les domaines de validité.** Une grandeur qui tend vers une limite
  sans l'atteindre rend certaines questions mathématiquement solubles et physiquement
  absurdes. Le métier doit poser la marge qui transforme l'asymptote en décision.
- **L'aléatoire.** Dès qu'une étape est stochastique — rééchantillonnage, Monte-Carlo,
  initialisation aléatoire —, **l'algorithme du générateur et sa graine deviennent des
  entrées de la spécification**, au même titre que les données. Ce n'est pas un détail
  technique : la reproductibilité d'un résultat publié en dépend.

**4. Distinguer la résolution de l'incertitude, et déclarer la propagation attendue.**

« Précision » désigne couramment deux choses sans rapport, et les confondre produit des
résultats affichés avec dix décimales dont deux sont vraies.

| | Ce que c'est | Exemple |
|---|---|---|
| **La résolution** | le pas de la valeur — une propriété de **représentation** | une température stockée à 0,1 °C près |
| **L'incertitude** | l'écart dans lequel se trouve la vraie valeur — une propriété **métrologique** | cette même température connue à ± 2 °C |

Une valeur peut parfaitement avoir une résolution de 0,1 °C et une incertitude de 2 °C :
la seconde ne se déduit pas de la première.

Dès qu'un résultat doit être accompagné de sa précision, **la spécification déclare quatre
choses** — et pas la méthode de propagation :

| Ce que la spécification déclare | Pourquoi c'est indispensable |
|---|---|
| **L'incertitude de chaque entrée**, avec sa nature (étendue, écart-type, loi) | sans elle, rien ne se propage |
| **Les corrélations entre entrées** | deux grandeurs issues du même capteur ne sont pas indépendantes ; les traiter comme telles **sous-estime** l'incertitude du résultat. C'est l'oubli le plus fréquent |
| **Le niveau de confiance exigé** en sortie | « ± 3 km » ne veut rien dire sans dire à quel niveau de confiance |
| **Ce qu'on fait quand l'incertitude dépasse un seuil** | refuser, dégrader, avertir — c'est une **décision métier** |

Et elle **ne dit pas comment propager**. Propagation analytique par dérivées partielles,
simulation de Monte-Carlo, arithmétique d'intervalles : le choix appartient au
développement, dès lors que le résultat respecte la tolérance déclarée. C'est exactement
le même partage que pour le lissage ou l'intégrateur — spécifier l'effet, pas la méthode.

> Le cadre de référence est le **GUM** — *Guide to the expression of uncertainty in
> measurement*, JCGM 100 — et son supplément 1 pour la méthode de Monte-Carlo. On lui
> emprunte le vocabulaire (incertitude-type, incertitude élargie, facteur d'élargissement)
> et la discipline sur les corrélations, pas son appareil complet.

**Une règle de restitution, qui va de soi et qu'on oublie toujours :** un résultat n'est
jamais affiché avec plus de chiffres significatifs que son incertitude n'en autorise.
`106,017 km ± 3 km` est une faute ; `106 km ± 3 km` est un résultat.

Enfin, une propriété de forme spécifique au scientifique : les **invariants de symétrie**
(§5) y sont particulièrement rentables — invariance par translation de l'origine des
temps, homogénéité vis-à-vis d'un changement d'unité, invariance par permutation des
entrées. Ils ne coûtent rien à écrire, se testent sur des milliers d'entrées générées, et
attrapent la classe d'erreurs la plus pernicieuse : celle où le résultat dépend
silencieusement des unités ou de l'origine choisies.

---

## 3. L'anatomie d'une spécification

Le squelette à copier est dans [templates/MODELE-SPECIFICATION.md](templates/MODELE-SPECIFICATION.md).
Voici le rôle de chaque section — et pourquoi aucune n'est facultative.

| # | Section | À quoi elle sert | Ce qui arrive si on la saute |
|---|---|---|---|
| 1 | **En-tête** — identifiant, version, statut, auteur, valideur métier, date d'effet | Savoir qui a validé quoi, et à partir de quand la règle s'applique | On ne sait pas quelle version a produit le chiffre de l'an dernier |
| 2 | **Objectif et contexte** — en cinq lignes, ce que ça calcule et pour qui | Permettre à un lecteur de décider en 30 secondes si ça le concerne | Personne ne lit |
| 3 | **Périmètre / hors périmètre** — explicitement les deux | Fixer la frontière du contrat | Le développeur découvre en fin de course que « bien sûr, il fallait aussi gérer les avoirs » |
| 4 | **Glossaire** — chaque terme du domaine, défini une fois | Donner un vocabulaire unique au métier, au code et aux tests | Trois mots pour la même chose, un mot pour trois choses |
| 5 | **Entrées / Sorties** — typées, avec unités et domaines | Poser le contrat de la fonction | Ambiguïtés d'unité, de devise, de fuseau |
| 6 | **Paramètres** — les valeurs, séparées des règles, avec qui peut les changer et leur date d'effet | Distinguer ce qui change souvent de ce qui change rarement | Un changement de taux devient une livraison logicielle |
| 7 | **Règles** — numérotées, en pseudo-langage | Le cœur | — |
| 8 | **Invariants et propriétés** | Ce qui doit rester vrai quelles que soient les entrées | On ne détecte pas les régressions sur les cas non testés |
| 9 | **Cas d'erreur métier** — code, condition, message, conséquence | Décider du comportement anormal *par le métier* | Le développeur invente le comportement de rejet |
| 10 | **Jeu d'essai** — cas nominaux, limites, erreurs, avec résultats calculés | Rendre la spécification vérifiable (§5) | La recette se fait « à l'œil », par comparaison avec l'ancien système |
| 11 | **Fiche de contraintes** — volumes, latence, précision, rejouabilité… (§4) | Permettre le choix du langage et de l'architecture | L'IT devine, et se trompe d'un facteur 100 |
| 12 | **Questions ouvertes** — `Q-xx`, décideur, échéance | Rendre l'incertitude visible au lieu de la laisser se résoudre en silence | Le développeur tranche à la place du métier |
| 13 | **Historique** — versions, ce qui a changé, impact sur les résultats | Traçabilité réglementaire et comptable | Impossible d'expliquer un écart entre deux exercices |

### 3.0 Le triptyque : rôle, contrat, algorithme

Quel que soit son niveau de maturité, une fonction se décrit par **trois choses, dans cet
ordre**.

| | Ce que ça dit | La question à laquelle ça répond |
|---|---|---|
| **1. Le rôle** | à quoi elle sert, et **ce qu'elle produit** | *pourquoi elle existe* |
| **2. Le contrat** | ce qu'elle exige, ce qu'elle garantit (§3.1) | *ce qu'elle promet* |
| **3. L'algorithme** | les règles `RG-xxx`, en pseudo-langage | *ce qui est calculé* |

**L'ordre n'est pas décoratif** : chacun se lit sans le suivant. On doit pouvoir
comprendre le rôle sans lire le contrat, et le contrat sans lire l'algorithme. Une
fonction dont le rôle ne s'explique qu'en déroulant ses règles est mal découpée.

**Le rôle tient en deux phrases** : ce qu'elle produit, et à partir de quoi. Sans « comment ».

| ✗ | ✓ |
|---|---|
| « Parcourt les segments et cumule l'énergie jusqu'à épuisement du budget » | « Répond à *jusqu'où puis-je aller sur ce trajet ?* — elle produit l'énergie consommée et la distance à laquelle la réserve est atteinte » |

Le premier décrit un parcours, et il devient faux dès qu'on change d'implémentation. Le
second décrit un résultat, et il tiendra quinze ans.

**Le triptyque est exactement ce que gradue l'échelle de maturité**
([guide 1](guides/1-DECOUPER.md)) : le niveau 1 remplit le rôle, le niveau 3 y ajoute le
contrat, le niveau 4 y ajoute l'algorithme. Un niveau n'est donc pas un pourcentage
d'avancement, c'est **une part du triptyque qui est renseignée** — et chacune est utile
seule.

### 3.1 Le contrat — la notion centrale

Les sections 4, 5 et 8 forment ensemble le **contrat** de la fonction. C'est la notion la
plus importante du cadre, et celle qu'on sous-estime le plus souvent en la prenant pour
une simple liste de champs.

> **Le contrat est ce qui est promis, indépendamment de la façon dont la promesse est
> tenue.** Il se fixe avant qu'on sache comment on fera — et c'est précisément ce qui le
> rend utile.

**Trois clauses, empruntées à la programmation par contrat et transposées au métier :**

| Clause | Où | Ce qu'elle dit |
|---|---|---|
| **Ce que j'exige** | §4, préconditions | les entrées, leur type, leur domaine, ce sans quoi je ne calcule pas |
| **Ce que je garantis** | §5, postconditions | les sorties, leur type, leur domaine |
| **Ce qui reste vrai** | §8, invariants | les propriétés que le résultat satisfait toujours |

**La règle de responsabilité qui en découle, et qui vaut à elle seule le formalisme :**

> Si les préconditions sont violées, le résultat n'engage pas la fonction : c'est
> l'appelant qui est en faute. Si elles sont respectées et qu'une postcondition ne l'est
> pas, c'est la fonction qui est en faute.

Cette phrase répartit la responsabilité **sans discussion possible**. Elle remplace les
débats « c'est ton défaut / c'est le mien » par une lecture de deux lignes.

**Pourquoi le contrat est essentiel, en quatre points :**

1. **Il permet de travailler en parallèle.** Dès que le contrat de `FN-002` est fixé, tout
   ce qui en dépend peut être écrit, estimé et même implémenté — sans attendre que
   `FN-002` soit spécifiée. C'est ce qui rend le [niveau 3](guides/1-DECOUPER.md) utile en
   soi, et pas un demi-travail.
2. **Il est l'unité de dépendance.** Une spécification dépend du **contrat** d'une autre,
   jamais de son implémentation ni de son texte complet. Une fonction peut être réécrite
   entièrement sans qu'aucun de ses appelants ne bouge — tant que le contrat tient.
3. **Il survit à tout le reste.** L'implémentation change, le langage change,
   l'architecture change, l'équipe change. Le contrat reste. C'est donc lui qu'on
   versionne avec le plus de soin, et lui dont la rupture se déclare explicitement
   ([guide 7](guides/7-VERSIONNER.md)).
4. **Il est vérifiable.** Un contrat se contrôle mécaniquement : une entrée déclarée que
   nulle règle n'emploie, une sortie promise que rien ne produit, une grandeur employée et
   jamais déclarée — ce sont les contrôles `C-01` à `C-04`
   ([outils](outils/REGLES-DE-CONTROLE.md)).

**Deux exigences de forme, non négociables :**

- **Un contrat sans unité ne vaut rien.** `montant : nombre` n'apporte rien que le code ne
  disait déjà. Toute la valeur est dans l'unité, la devise, le fuseau, la précision et le
  domaine — c'est-à-dire dans ce que le typage d'un langage ne dit pas.
- **Les règles emploient les identifiants du contrat, à l'identique** (§2.3). Si le contrat
  déclare `prix_catalogue_ht` et que la règle parle du « prix catalogue », le lien n'est
  plus vérifiable et le vocabulaire commence à diverger.

**Ce qu'un contrat n'est pas** : une structure de données, une signature de fonction, un
schéma d'échange. Ceux-là dérivent du contrat, dans un langage donné, et changeront
plusieurs fois pendant que le contrat, lui, tiendra.

#### Le contrat a deux vies, et la seconde est la plus utile

| Quand | Ce qu'il sert à faire |
|---|---|
| **Avant le développement** | Il est **validé** : le métier atteste que ce qui est promis est bien ce qu'il veut, le technique atteste que c'est tenable. Rien ne part tant que ce n'est pas fait ([guide 5](guides/5-VALIDER.md)) |
| **Après le développement** | Il sert à **garantir que le résultat est bon** : les tests rejouent les entrées du contrat et comparent les sorties obtenues aux sorties attendues, aux tolérances déclarées près |

C'est le même document qui joue les deux rôles, et ce n'est pas un hasard : **on ne peut
vérifier que ce qu'on a promis**. Un contrat validé mais dont les sorties ne sont pas
comparables — pas de jeu de données attendu, pas de tolérance déclarée, pas de valeurs
intermédiaires exposées — ne sert que la moitié de son office.

D'où une exigence qui se pose à l'écriture, longtemps avant la recette : **le contrat doit
être écrit pour être vérifiable**, pas seulement pour être compris.

### 3.2 La chaîne de traitement

Un calcul un peu long cesse d'être lisible comme une suite de règles. Il se lit alors
comme **une suite de boîtes qui s'enchaînent** — et cette vue se place **avant** les
règles, parce qu'elle dit de quoi on va parler.

Chaque boîte est une **étape** `ET-xx`. Elle déclare exactement trois choses :

| | |
|---|---|
| **Ce qu'elle consomme** | les identifiants des grandeurs qu'elle lit |
| **Ce qu'elle produit** | les identifiants des grandeurs qu'elle écrit — **des noms nouveaux**, par immutabilité (§2.4) |
| **Ce qui la réalise** | la ou les règles `RG-xxx` |

```
| Étape | Consomme | Produit | Règles |
|---|---|---|---|
| `ET-02` Énergie mécanique | force_totale, distance | energie_mecanique | RG-020 |
| `ET-03` Traction et récupération | energie_mecanique, rendement_traction, rendement_recuperation | energie_traction | RG-030 |
```

**Ce que cette vue apporte, et qu'aucune autre ne donne :**

1. **Elle se vérifie mécaniquement.** Toute grandeur consommée est soit une entrée du
   contrat, soit un paramètre, soit produite par une étape antérieure — sinon elle sort de
   nulle part (`C-35`). Toute grandeur produite est soit consommée plus loin, soit une
   sortie du contrat — sinon c'est un **produit mort** (`C-36`). Ces deux contrôles
   attrapent une classe entière d'erreurs de conception que la relecture ne voit pas.
2. **Elle rend le chaînage explicite.** Le produit d'une boîte est le consommable d'une
   autre : la chaîne se lit d'un bout à l'autre, sans dérouler les formules.
3. **Elle borne ce que le développeur peut réordonner et paralléliser.** Deux étapes dont
   aucune ne consomme ce que l'autre produit sont indépendantes. En dérivant les
   **niveaux** du graphe, on obtient les **fils** : ce qui peut s'exécuter de front, et le
   **chemin critique** qui borne la latence quel que soit le nombre de processeurs.

   > **La spécification dit ce qui est indépendant ; elle ne dit pas de paralléliser.**
   > La décision reste au développement — et la contrainte de déterminisme du §4 peut
   > parfaitement l'interdire. Ce que la chaîne apporte, c'est qu'on ne parallélise plus
   > *en espérant* que c'est sûr : on sait lesquelles le sont.
4. **Elle rejoint le parcours des grandeurs.** `--tracer` répond « où passe cette
   grandeur ? » ; la chaîne répond « **qui la crée et qui l'utilise** ».
5. **Elle s'abstrait.** Des étapes interconnectées se regroupent en boîtes `GR-xx` ; ce
   qu'un groupe échange avec l'extérieur se **déduit** de ses membres, les grandeurs
   purement internes disparaissant de la vue. Dix boîtes deviennent trois, sans qu'on ait
   rien à réécrire — et sans qu'aucune règle ne bouge, puisqu'un groupe est une vue et
   non une fonction.

```bash
python3 outils/verifier.py --chaine <spécification>
```

produit d'un seul coup : la table « qui crée / qui utilise » de chaque grandeur, les
contrôles `C-35` et `C-36`, les fils d'exécution avec leur chemin critique, et la vue
groupée.

> **Elle n'impose aucun découpage d'implémentation.** Une boîte n'est pas une fonction du
> code, ni un service, ni une étape d'exécution : c'est une **unité de lecture**. Rien
> n'interdit de tout calculer en un seul passage — la chaîne dit ce qui dépend de quoi,
> pas dans quel ordre exécuter.

Un diagramme accompagne utilement la table quand la chaîne se ramifie ; il ne la remplace
pas, parce que lui n'est pas vérifiable.

---

Sur la section 6, une précision qui a des conséquences architecturales lourdes :

> **Un paramètre n'est pas une règle.** Une règle dit « on applique une remise de
> quantite_commandee au-delà d'un certain seuil ». Un paramètre dit « ce seuil vaut 3 unités
> depuis le 1er janvier ». Les deux ne changent ni à la même fréquence, ni par les
> mêmes personnes, ni selon le même circuit d'approbation. Les mélanger, c'est
> transformer chaque décision commerciale en projet informatique.

Chaque paramètre est décrit par : identifiant, libellé, valeur, unité, **qui peut le
changer** (une personne ou un rôle nommé), **circuit de modification** (qui valide),
**fréquence de changement observée**, **date d'effet**. Ces trois derniers points sont
lus directement par l'architecte (§4).

---

## 4. La fiche de contraintes : ce qui permet de choisir le langage et l'architecture

### 4.1 Les contraintes métier

C'est la partie que les experts métier oublient toujours, et c'est celle sans laquelle
le développeur ne peut rien décider. **Le métier n'y exprime pas des solutions : il y
exprime des contraintes, en unités métier, chiffrées.** L'IT en déduit la solution.

| Dimension | Ce que le métier écrit | Ce que l'IT en déduit |
|---|---|---|
| **Volumétrie** | Nombre d'exécutions par jour, taille typique et maximale d'une entrée, croissance attendue à 3 ans | Dimensionnement, traitement unitaire ou massif, coût |
| **Profil de charge** | Charge moyenne et charge de pointe (facteur et durée), saisonnalité | Élasticité, files d'attente, garde-fous |
| **Mode d'appel** | À la demande / par lots à heure fixe / à la survenue d'un événement | Architecture d'intégration : service, tâche planifiée, flux |
| **Latence** | Temps de réponse acceptable, et **ce qui se passe si on le dépasse** | Synchrone ou asynchrone, mise en cache, précalcul |
| **Fraîcheur des données** | À quelle date les données doivent être arrêtées ; délai toléré | Sourcing, réplication, cohérence |
| **Exactitude** | Précision requise, mode d'arrondi, tolérance acceptée sur un écart | **Choix du type numérique** — décimal exact ou flottant — donc contrainte forte sur le langage et les bibliothèques |
| **Déterminisme** | Deux exécutions identiques doivent-elles donner exactement le même résultat ? | Interdiction du parallélisme non ordonné, du hasard, des dépendances à l'horloge |
| **Rejouabilité** | Doit-on pouvoir recalculer un résultat de l'an dernier et retrouver le même chiffre ? Sur quelle profondeur ? | **Versionnement conjoint des règles, des paramètres et des données** ; archivage ; horodatage |
| **Auditabilité** | Faut-il justifier le résultat pas à pas ? À qui ? Pendant combien de temps ? | Trace de calcul, format de restitution, conservation |
| **Explicabilité** | Faut-il pouvoir dire à un client *pourquoi* il obtient ce résultat ? | Sortie enrichie du détail, interdiction des modèles opaques |
| **Criticité et mode dégradé** | Que fait-on si une donnée ou un service dont dépend le calcul est indisponible ? (refus / valeur par défaut / résultat partiel signalé) | Redondance, isolation des dépendances, circuit de secours |
| **Confidentialité** | Le calcul manipule-t-il des données personnelles ou sensibles ? Lesquelles ? | Hébergement, cloisonnement, anonymisation, durée de conservation |
| **Conformité** | Textes ou normes applicables, obligations de conservation | Contraintes de journalisation et d'archivage |
| **Fréquence de changement de la règle** | Combien de fois par an la règle change-t-elle ? Et les paramètres ? | **Code figé vs configuration externalisée vs moteur de règles** |
| **Qui modifie la règle** | Le métier doit-il pouvoir modifier une valeur sans livraison logicielle ? | Interface d'administration, circuit de validation, tests de garde-fou |
| **Durée de vie** | Combien de temps ce calcul est-il censé vivre ? | Arbitrage entre effort d'industrialisation et jetable assumé |

### 4.2 Les exigences de réalisation

Le tableau ci-dessus exprime des **besoins**, en unités métier : ils *permettent* à l'IT
de choisir. Il existe une seconde famille, de nature opposée : des contraintes que
l'organisation **impose** à la réalisation, et qui *restreignent* le choix.

> **Deux familles, deux valideurs, et il ne faut pas les mélanger.**
>
> | | **Contraintes métier** (§4.1) | **Exigences de réalisation** (§4.2) |
> |---|---|---|
> | Nature | un besoin exprimé | une contrainte imposée |
> | Effet | **permet** de choisir | **restreint** le choix |
> | Qui la valide | le métier | sécurité, architecture, sûreté, conformité |
> | Origine | le calcul lui-même | une politique, une norme, un contrat, une plate-forme |

L'expert métier **ne les invente pas** : il les reçoit et les intègre au document, parce
que c'est le document que le développement lira. Elles portent des identifiants `EX-xxx`.

**Ce sur quoi elles portent, typiquement :**

| Famille | Exemples |
|---|---|
| **Classification et segmentation des données** | niveau de sensibilité de chaque donnée, cloisonnement entre partitions, ce qui ne doit jamais quitter un périmètre, durée de conservation, pseudonymisation |
| **Langages et bibliothèques** | liste technique approuvée, versions minimales, sous-ensembles normatifs imposés, bibliothèques interdites |
| **Règles de codage** | norme applicable, analyse statique bloquante, seuils de couverture, revue obligatoire |
| **Plate-forme cible** | matériel, système, ressources disponibles, absence de ramasse-miettes, déterminisme temporel, partitionnement |
| **Intégration et exploitation** | protocoles et formats imposés, journalisation, supervision, mode de déploiement |
| **Conformité et certification** | normes produit, preuves à produire, traçabilité exigée par un auditeur |

**Le discriminant, et il est sans appel :**

> **Une exigence de réalisation sans source nommée n'est pas une exigence : c'est une
> préférence d'équipe déguisée.**

« Utiliser Java » n'est pas une exigence. « Le logiciel du calculateur respecte le
sous-ensemble MISRA C:2012 — politique de sûreté logicielle `DIR-SUR-004`, direction
Sûreté, vérifié par analyse statique bloquante en intégration » en est une. La différence
n'est pas de forme : la seconde a un responsable nommé et un moyen de prouver qu'elle
est tenue.

Chaque `EX-xxx` porte donc quatre choses, et les quatre sont obligatoires :

| | |
|---|---|
| **L'énoncé** | au présent de l'indicatif ou avec « doit », vérifiable |
| **La source** | le texte, la norme, la politique ou le contrat dont elle découle |
| **Le valideur** | qui atteste qu'elle est juste, et qui peut l'amender |
| **La vérification** | comment on prouve qu'elle est tenue — sans quoi elle est décorative |

**Quand une exigence contredit une contrainte métier**, ce qui arrive plus souvent qu'on
ne croit — une latence de 20 ms face à une plate-forme imposée à ramasse-miettes —, le
conflit **ne se règle pas dans le document** : il devient une question ouverte `Q-xx`,
arbitrée par les deux valideurs. Le résoudre en silence, c'est faire porter à un
développeur un arbitrage entre la sûreté et l'expérience client.

---

Deux commentaires qui valent le détour :

**Le couple « exactitude / déterminisme » décide souvent du langage.** « Les montants
sont exacts au centime, sans erreur de représentation » élimine le flottant binaire et
oriente vers les langages disposant d'un décimal natif ou d'une bibliothèque décimale
éprouvée. « Le résultat doit être identique quel que soit l'ordre de traitement »
interdit certaines parallélisations. Le métier n'a pas besoin de savoir tout cela : il
a juste besoin d'écrire ses deux phrases.

**Le couple « fréquence de changement / qui modifie » décide de l'architecture.** Un
barème modifié une fois tous les cinq ans par la direction, et un barème modifié chaque
semaine par le marketing, produisent deux architectures radicalement différentes — code
en dur d'un côté, référentiel de paramètres versionné et daté de l'autre. C'est la
question la moins technique de la liste, et c'est celle qui a le plus d'impact
technique.

---

## 5. Le jeu d'essai : l'oracle

Une spécification sans jeu d'essai n'est pas une spécification : c'est une intention.

Le jeu d'essai est **arrêté par le métier**. Il couvre **l'ensemble des cas à tester**, et
pour chacun il donne **le jeu de données d'entrée complet et le jeu de données de sortie
attendu** — pas un extrait illustratif. Quand l'analyse d'écart le demandera
([guide 8](guides/8-ANALYSER-LES-ECARTS.md)), il donne aussi les **valeurs intermédiaires**
attendues à chaque étape de la chaîne.

### Ce qui rend un résultat attendu valide

La règle n'est ni « calculé à la main », ni « produit indépendamment ». Ces deux
formulations sont trop étroites : elles écartent des pratiques légitimes.

> **Ce qui fait la validité d'un résultat attendu, ce n'est pas d'où il vient — c'est
> qu'une personne du métier l'ait examiné et accepté en connaissance de cause.**

Un résultat peut parfaitement provenir **du composant à tester lui-même**. C'est même le
seul recours quand il n'existe ni solution analytique, ni étalon, ni moyen raisonnable de
calculer la valeur à l'avance. Ce qui compte alors, c'est **la qualité de l'examen** qui
précède l'acceptation.

| Source du résultat candidat | Ce que l'examen doit établir |
|---|---|
| **Solution analytique** | que la formule employée est bien celle de la règle |
| **Étalon, mesure de référence** | que les conditions de la mesure correspondent au cas |
| **Calcul à la main** | que le calcul est juste, et refait par un second lecteur pour le cas riche |
| **Maquette technique** | que la maquette applique bien les règles, et que ses sorties tiennent cas par cas |
| **Le composant à tester** | qu'il calcule bien ce que la spécification décrit — par **analyse statique et dynamique** |

### Examiner un résultat produit par le composant lui-même

C'est le cas le plus exigeant, et il est parfaitement admissible s'il est mené
sérieusement. Deux moyens, complémentaires :

| | Ce qu'on fait | Ce que ça établit |
|---|---|---|
| **Analyse statique** | On lit ce que le composant fait à chaque étape et on le confronte à la règle correspondante | Que la règle écrite est bien celle qui est appliquée |
| **Analyse dynamique** | On exécute en observant les **valeurs propagées** d'une étape à l'autre ; on fait varier une entrée et on vérifie que la sortie bouge comme la règle le prédit | Que le comportement suit la règle **hors du seul cas observé** |

La seconde est celle qu'on néglige, et c'est la plus probante : constater qu'un chiffre
est plausible ne prouve rien ; constater qu'il **réagit correctement** à une variation
d'entrée prouve beaucoup. Faire varier la température de 5 °C et vérifier que l'autonomie
diminue de l'ordre attendu vaut mieux que d'approuver une valeur isolée.

Une fois examinés et acceptés, ces résultats deviennent la **référence de
non-régression** : toute évolution ultérieure qui les modifie devra être expliquée.

> **La contrepartie, à connaître :** un résultat accepté depuis le composant fige *ce que
> le composant fait*, y compris ses défauts éventuels. L'examen est donc ce qui transforme
> un simple constat en référence — et son ampleur doit être proportionnée à l'enjeu. On
> écrit **comment** il a été mené : lu, rejoué, varié, comparé à quoi, par qui.

### La donnée de référence

Quand une personne du métier a validé les sorties par rapport aux entrées **pour une
fonction déterminée**, le couple (entrées, sorties) cesse d'être un exemple :

> **Il devient une donnée de référence** — un engagement, opposable, réutilisable.

| Avant la validation | Après |
|---|---|
| Un résultat candidat | Un **engagement** |
| Discutable | **Opposable** : toute évolution qui le modifie devra être expliquée |
| Attaché à personne | Attaché à **une fonction, une version de spécification, un valideur, une date** |

Concrètement, un cas de test `CT-xx` dont les résultats ont été examinés et acceptés
**devient** une donnée de référence. Ce n'est pas un objet nouveau : c'est un **statut**
qu'acquiert le cas de test, et qui se trace.

**Trois usages, et le deuxième est celui qui dure :**

| Usage | Ce qu'il apporte |
|---|---|
| **Qualifier le code** | C'est le critère d'acceptation du développement : les données de référence passent, ou le composant n'est pas conforme |
| **Tester la non-régression** | Le jour où quelqu'un optimise, refactorise, change de bibliothèque ou de plate-forme : si les données de référence passent toujours, **le comportement est intact**. C'est ce qui rend l'optimisation sans peur, et donc possible |
| **Comparer deux implémentations** | Le test de la double implémentation, et le cas de deux cibles — embarqué et serveur — qui doivent coïncider |

**La règle de versionnement qui donne son sens à tout le reste :**

Une donnée de référence est attachée à une **version de la spécification**. Quand celle-ci
passe en **majeur** — c'est-à-dire quand un résultat change —, les données de référence
concernées doivent être **revalidées explicitement**, jamais mises à jour en silence. Leur
historique est conservé : on doit pouvoir dire ce qu'on attendait à chaque version, et
pourquoi cela a changé.

> **L'anti-pattern, et c'est le plus tentant de tous : mettre à jour la donnée de référence
> pour faire passer un test qui échoue.**
>
> C'est le jumeau de l'ajustement de tolérance en cours de recette
> ([guide 8](guides/8-ANALYSER-LES-ECARTS.md)). Toujours défendable sur le moment, et il
> supprime d'un coup la seule chose qui protégeait le comportement. Une donnée de
> référence se modifie **par une revalidation métier, datée et motivée** — jamais parce
> qu'un test est rouge.

### Ce qui se trace, pour chaque jeu d'essai

Le §10 d'une spécification porte donc, en tête, **la provenance et la validation** de ses
résultats attendus : d'où ils viennent, comment ils ont été examinés, par qui, à quelle
date, et pour quelle version. Sans cette trace, on ne saura pas, dans deux ans, si un
chiffre est un engagement ou un vestige.

### Le seul cas qui n'est jamais valide

> **Accepter un résultat parce qu'un programme l'a produit, sans l'examiner.**

C'est cela, l'oracle circulaire — et non le fait que la valeur vienne du composant. La
faute n'est pas dans la provenance, elle est dans **l'absence de jugement**. Elle se
reconnaît à une phrase : « c'est ce que ça sort aujourd'hui, on part de là. »

### La maquette technique

Quand on ne peut pas disposer des résultats attendus a priori — trop de cas, calculs trop
lourds, données trop volumineuses — on les fait produire par une **maquette technique**.

> Une maquette est **du code écrit sans contrainte d'industrialisation ni de performance**,
> dans le seul but de produire des résultats de référence. Elle n'a ni tests, ni
> exploitabilité, ni tenue en charge — et c'est précisément ce qui la rend rapide à
> écrire.

**Quatre règles la rendent honnête. Sans elles, elle redevient l'oracle circulaire :**

| # | Règle | Ce qu'elle empêche |
|---|---|---|
| 1 | **Ses sorties ne sont pas l'oracle. L'oracle, ce sont ses sorties une fois validées par le métier**, cas par cas | Qu'on accepte un chiffre parce qu'un programme l'a produit |
| 2 | Elle est écrite par **quelqu'un d'autre** que celui qui développera le composant, et **le code de production n'en dérive pas** | La perte d'indépendance — sans quoi les deux partagent leurs erreurs |
| 3 | Elle est **versionnée avec le jeu d'essai** | Qu'on ne sache plus d'où venaient les chiffres |
| 4 | Elle est **abandonnée** quand le composant est développé : ni maintenue, ni déployée, ni reprise | Qu'elle survive et redevienne le prototype-en-production du §1.1 |

> **La règle 4 doit être écrite noir sur blanc au moment où la maquette est lancée**, pas
> découverte après. Une maquette qu'on garde « au cas où » devient en dix-huit mois un
> second système, non testé et sans propriétaire, dont les résultats contredisent
> périodiquement ceux du vrai.

Bien conduite, la maquette est en réalité une **application du test de la double
implémentation** (§1.3) : deux programmes indépendants doivent donner le même résultat.
Elle en est même la meilleure forme, puisqu'elle est écrite avant, sans contrainte, et
qu'on l'abandonne sans regret.

Ce qu'il contient, dans cet ordre :

1. **Un ou deux cas nominaux**, simples, qu'on peut suivre de tête.
2. **Un cas riche**, qui déclenche le plus de règles possible en même temps, avec sa
   **trace de calcul détaillée** — étape par étape, chaque montant intermédiaire écrit.
   C'est le cas qui sert de référence commune quand un doute apparaît, six mois plus
   tard, sur l'ordre d'application des règles.
3. **Les cas aux limites** : zéro, vide, une seule ligne, valeur exactement égale au
   seuil (`≥` ou `>` ?), ex æquo, montant qui tombe pile sur un demi-centime, plafond
   atteint, date de bascule d'un paramètre, franchissement d'un changement d'année.
4. **Les cas d'erreur** : pour chacun, le code d'erreur attendu et ce qui est retourné
   (rien ? un résultat partiel ?).
5. **Une table de couverture** : pour chaque `RG-xxx`, le ou les cas qui l'exercent. Une
   règle non couverte est soit inutile, soit un test manquant — les deux méritent une
   discussion.

Enfin, on écrit les **propriétés** (§ invariants), qui valent pour *toutes* les entrées
et pas seulement pour les cas listés :

```
INV-01  Le montant à payer est toujours supérieur ou égal à zéro.
INV-02  La somme des montants nets des lignes est égale au montant net total.
INV-03  Ajouter un article à un panier ne peut jamais diminuer le montant avant remise.
INV-04  Le calcul est idempotent : recalculer un panier inchangé donne le même montant.
```

Ces propriétés sont un cadeau au développeur : elles se testent automatiquement sur des
milliers d'entrées générées aléatoirement, et elles trouvent les cas auxquels le métier
n'avait pas pensé. C'est le complément naturel du test de la double implémentation.

---

## 6. Le processus et la gouvernance

### 6.1 Les rôles

| Rôle | Qui | Responsabilité |
|---|---|---|
| **Auteur métier** | l'expert du domaine | Écrit la spécification et la soumet à validation |
| **Valideur métier** | un responsable du domaine, nommé | **Atteste que l'expression fonctionnelle est juste du point de vue métier.** Tranche les questions ouvertes, et revalide tout changement modifiant un résultat |
| **Relecteur métier** | un pair de l'auteur | Vérifie l'exactitude métier et la complétude des cas |
| **Co-auteur technique** | un développeur | Écrit la fiche de contraintes avec l'auteur, pose les questions qui font apparaître les cas non prévus, signale ce qui coûtera cher. **Il ne rédige pas les règles** |
| **Relecteur technique** | un développeur **extérieur** au domaine et à l'écriture | Répond par oui ou par non : « ai-je toutes les informations pour coder cela sans reposer de question ? » ([guide 5](guides/5-VALIDER.md)) |
| **Relecteur test** | testeur / recette | Vérifie que la spécification est vérifiable : jeu d'essai, couverture, invariants |

Trois relecteurs, trois casquettes. Une spécification relue par une seule population
n'est relue par personne.

### 6.2 Le cycle de vie

```mermaid
flowchart LR
    A[Besoin métier] --> B[Rédaction<br/>spécification]
    B --> C{Revue<br/>3 casquettes}
    C -- questions --> B
    C -- acceptée --> D[Implémentation]
    D --> E{Question<br/>découverte ?}
    E -- oui --> F[Q-xx ouverte<br/>dans la spécification]
    F --> G[Arbitrage<br/>valideur métier]
    G --> B
    E -- non --> H[Jeu d'essai<br/>exécuté]
    H -- écart --> I{Erreur de code<br/>ou de spécification ?}
    I -- code --> D
    I -- spécification --> B
    H -- conforme --> J[Mise en production]
    J --> K[La spécification reste<br/>la source de vérité]
```

Le point non négociable du schéma : **le développeur ne corrige jamais une règle
silencieusement**. Quand il rencontre un cas non prévu — et il en rencontrera —, il
ouvre une question `Q-xx` dans le document, et le valideur métier tranche. Une décision
métier prise dans un commit est une décision perdue.

Le second point : **quand le programme et la spécification divergent, c'est la
spécification qui fait foi** — soit le code est corrigé, soit la spécification est
amendée explicitement, avec une nouvelle version et une trace. Jamais de troisième
voie.

### 6.3 La spécification vit dans Git

Même dépôt que le code, ou dépôt dédié — mais Git, en markdown, pas un traitement de
texte partagé :

- **l'historique** répond à « qui a changé cette règle, quand, et pourquoi » ;
- **la revue par demande de fusion** donne un lieu de discussion attaché à la ligne
  concernée ;
- **la comparaison de versions** montre exactement ce qui a bougé entre deux exercices ;
- **les étiquettes de version** permettent de retrouver la spécification exacte qui a
  produit le chiffre d'une date donnée.

Le frein réel est l'inconfort de Git pour un non-développeur. Il se traite : édition
directe dans l'interface web de la forge, modèle pré-rempli, un accompagnement en
binôme sur les deux premières spécifications. Cet inconfort dure deux semaines ; le
bénéfice dure des années.

### 6.4 Le versionnement, en termes métier

*Traité en détail dans le [guide 7 — Versionner](guides/7-VERSIONNER.md), qui couvre le
quadruplet de rejouabilité, la distinction entre version et date d'effet, et le gel des
versions.*

| Incrément | Quand | Conséquence |
|---|---|---|
| **Majeur** (2.0.0) | Un résultat change pour au moins un cas | Nouvelle date d'effet, information des consommateurs, rejeu à prévoir |
| **Mineur** (1.3.0) | Un cas nouveau est couvert, sans changer les résultats existants | Nouveaux tests, pas de rejeu |
| **Correctif** (1.2.4) | Clarification de rédaction, sans aucun impact sur les résultats | Rien |

**Le test décisif est mécanique** : on rejoue le jeu d'essai de la version précédente sur
la nouvelle. Si un seul cas change de résultat attendu, c'est un **majeur**. « Ce n'est
qu'une clarification » est la phrase à surveiller en revue — neuf fois sur dix elle est
vraie, la dixième fois ce test la démasque en trente secondes.

Toute règle porte une **date d'effet** et, si elle est abrogée, une **date de fin
d'effet**. Une règle abrogée n'est pas supprimée du document : elle est barrée et datée,
parce qu'un rejeu sur une période antérieure doit pouvoir s'y référer.

### 6.5 Prêt à développer / prêt à livrer

*Le jalon de validation est traité en détail dans le [guide 5 — Valider](guides/5-VALIDER.md),
avec ses deux étages : contrôles mécaniques outillés, puis relecture qualité humaine.*

**Prêt à développer** (la spécification peut partir en développement) :
- [ ] Toutes les entrées et sorties sont typées, avec unité et domaine
- [ ] Chaque règle a un identifiant et est écrite en pseudo-langage
- [ ] Chaque `SI` a son `SINON`, chaque table de décision est complète
- [ ] Les arrondis, les départages et le traitement des valeurs absentes sont explicites
- [ ] Le jeu d'essai existe, avec résultats calculés à la main, et couvre chaque règle
- [ ] La fiche de contraintes est chiffrée
- [ ] Aucune question ouverte bloquante ne subsiste
- [ ] Les trois relecteurs ont approuvé

**Prêt à livrer** (le développement est fini) :
- [ ] Le jeu d'essai passe intégralement, en automatisé
- [ ] Chaque `RG-xxx` est citée dans le code et dans au moins un test
- [ ] Les invariants sont testés sur des entrées générées
- [ ] Les questions apparues pendant le développement ont été arbitrées et reportées
      dans la spécification
- [ ] Les écarts assumés entre la spécification et l'implémentation sont documentés

### 6.6 Mesurer que ça marche

Quatre indicateurs suffisent, et ils se lisent ensemble :

| Indicateur | Ce qu'il révèle |
|---|---|
| Nombre de questions ouvertes par spécification, **et à quel moment elles apparaissent** | Si elles apparaissent en développement plutôt qu'en revue, la revue est trop faible |
| Nombre d'allers-retours entre développement et métier après acceptation | La qualité réelle des spécifications |
| Nombre d'anomalies de **règle** (et non de code) détectées en production | Le résultat final de la démarche |
| Délai entre l'expression du besoin et la mise en production | Le coût de la démarche — il augmente d'abord, puis baisse |

Prévenir la direction du dernier point : **le délai augmente sur les trois premières
spécifications**. On paie d'avance des discussions qui, autrement, auraient eu lieu en
production. C'est le principe même de la démarche, pas un incident de parcours.

---

## 7. Mise en place progressive

Une démarche de ce type échoue quand on l'annonce comme une norme d'entreprise. Elle
réussit quand elle est adoptée parce qu'elle a visiblement marché une fois.

**Étape 1 — L'exercice d'étalonnage (2 à 3 semaines).**
Choisir un algorithme **existant, connu, moyennement complexe et un peu douloureux** —
pas le plus critique, pas le plus trivial. En écrire la spécification *a posteriori*.
Puis l'épreuve de vérité : faire implémenter par un développeur qui ne connaît pas le
domaine, et comparer au système existant. Chaque écart constaté est une case manquante
du cadre. Ce sont ces écarts, et non le discours, qui convainquent.

**Étape 2 — Outiller léger (1 semaine).**
Un dépôt, le modèle, la liste de vérification, la revue en demande de fusion. Rien
d'autre. Surtout pas d'outil dédié, surtout pas de formalisme supplémentaire : chaque
outil ajouté au départ est une raison de ne pas s'y mettre.

**Étape 3 — Former en faisant (2 h + binômes).**
Un atelier de deux heures sur le principe et les faux amis (§1.5), puis les deux
premières spécifications de chaque auteur écrites **en binôme avec un développeur**.
Le binôme est le vrai vecteur d'apprentissage : c'est en voyant le développeur buter
sur une ambiguïté qu'on comprend ce qu'est une ambiguïté.

**Étape 4 — Rendre la spécification obligatoire là où elle est rentable.**
Pas partout. Les critères : la règle a un impact financier, contractuel ou
réglementaire ; elle va vivre plusieurs années ; plusieurs personnes devront la
comprendre ; elle change régulièrement. Une exploration ponctuelle dans un notebook
n'a pas besoin de spécification — et prétendre le contraire discrédite la démarche.

**Étape 5 — Mesurer et raconter.**
Les indicateurs du §6.6, et surtout : rendre publics les cas où la spécification a
évité une erreur. C'est ce qui fait tenir la démarche quand la pression sur les délais
revient.

**Deux pièges d'adoption :**

- *Le formalisme rampant.* Chaque relecteur veut ajouter une section. Au bout de six
  mois, le modèle fait quinze pages et plus personne n'écrit. Le modèle est un bien
  commun : on n'y ajoute une section que si son absence a causé un incident réel.
- *La spécification écrite après le code.* Le jour où quelqu'un code d'abord et
  documente ensuite « pour être conforme au processus », la démarche est morte : il ne
  reste que le coût. C'est le seul point sur lequel il faut être intransigeant.

---

## 8. Anti-patterns

| Anti-pattern | À quoi on le reconnaît | Pourquoi c'est grave |
|---|---|---|
| **Le pseudo-code déguisé** | `import`, `try/except`, `for i in range`, des types techniques | L'auteur pense encore dans son langage ; il transmet ses choix d'implémentation comme s'ils étaient des règles |
| **Le « etc. »** | « les autres cas se traitent de la même façon », « on gère les cas particuliers » | Le développeur devra deviner — et il devinera |
| **La valeur magique** | `0,85` sans nom, sans source, sans personne habilitée à la changer | Personne ne saura jamais s'il faut la changer, ni qui a le droit de le faire |
| **Le `SI` sans `SINON`** | Une condition sans branche complémentaire | Le comportement dans le cas non traité est décidé par le compilateur |
| **La grandeur nue** | Un montant sans devise, une date sans fuseau, un poids sans unité | Cause classique d'écarts, découverts tard et cher |
| **La performance en prose** | « il faut que ce soit rapide », « éviter les traitements lourds » | Non vérifiable, non actionnable → à remplacer par un chiffre dans la fiche de contraintes |
| **La spécification qui impose la technique** | « stocker dans une table », « utiliser un cache » | Le métier sort de son mandat et empêche des solutions meilleures |
| **L'oracle circulaire** | Un résultat a été accepté **parce qu'un programme l'a produit**, sans examen — « c'est ce que ça sort aujourd'hui, on part de là » | Le test ne mesure plus que la cohérence du programme avec lui-même. La faute est dans l'absence de jugement, pas dans la provenance |
| **La maquette qui survit** | La maquette technique n'a pas été abandonnée après le développement | En dix-huit mois, un second système non testé, sans responsable, dont les résultats contredisent périodiquement ceux du vrai |
| **La spécification rétroactive** | Écrite après le code, pour la forme | On paie le coût de la démarche sans en avoir le bénéfice |
| **Le paramètre enfermé dans la règle** | Un seuil écrit en toutes lettres au milieu du pseudo-code | Chaque décision commerciale devient une livraison logicielle |
| **La question refermée en silence** | Un `Q-xx` qui disparaît du document sans décision tracée | Quelqu'un a tranché sans mandat, et on ne saura pas qui |

---

## 9. Les exemples : lecture guidée

Le **fil rouge** de ce dépôt — *[l'autonomie d'un véhicule
électrique](exemples/fil-rouge/)* — déroule la méthode complète, du découpage à la
spécification détaillée, en passant par le glossaire du domaine. C'est là qu'il faut
aller pour voir l'ensemble s'articuler.

Deux **vignettes** complètent l'illustration, plus courtes et volontairement issues de
domaines opposés. Elles sont volontairement issues de
domaines opposés, parce que **la même méthode y conduit à des conclusions techniques
contraires** — ce qui est la meilleure preuve que c'est bien la spécification qui décide,
et non l'habitude du développeur.

| | [SPEC-PRX-001](exemples/SPEC-PRX-001-montant-a-payer.md) — gestion | [SPEC-THM-001](exemples/SPEC-THM-001-refroidissement.md) — scientifique |
|---|---|---|
| Le calcul | Le montant à payer d'une commande | La température d'une boisson qui refroidit |
| Grandeurs | Montants, taux, quantite_commandees | Températures, durées, masses |
| Exigence d'exactitude | **Exacte au centime** | **Tolérance relative** |
| Type numérique qui en découle | **Décimal exact obligatoire** | **Double précision binaire suffisante** |
| Critère d'acceptation | Égalité stricte | Reproductibilité 10⁻⁹, justesse 10⁻⁶, validité ± 2 °C (§2.8) |
| L'ordre des opérations | Remise ligne, puis panier, puis fidélité | Ajouter le lait tôt ou tard — deux tiers de degré d'écart |
| Le piège central traité | Le centime résiduel d'une répartition au prorata | La non-convergence, et l'asymptote qu'on ne peut pas atteindre |
| Ce que la liberté laissée permet | Jointure, calcul en mémoire ou cache | Forme fermée **ou** intégrateur numérique — les deux conformes |

### Ce qu'il faut regarder dans l'exemple de gestion

**Les paramètres séparés des règles (§6).** Le seuil de franchise de port a un
un responsable habilité à le changer (« Direction commerciale »), une fréquence de changement observée
(« hebdomadaire en soldes ») et une date d'effet. L'architecte y lit immédiatement :
*ces valeurs ne doivent pas être dans le code*.

**Une règle qui ne dit pas comment (`RG-010`).**

```
RG-010  Prix unitaire retenu
   Pour chaque ligne, le prix unitaire retenu est le plus petit
   du prix catalogue et du prix promotionnel en vigueur à la date de commande.
   S'il n'existe aucun prix promotionnel en vigueur, le prix catalogue est retenu.
```

Aucune boucle, aucun tri, aucune structure : le développeur peut résoudre cela par une
jointure en base, un calcul en mémoire ou un cache. Aucun de ces choix ne peut trahir la
règle.

**Un faux ami traité (`RG-085`, le centime résiduel).** Une remise de 5,00 € répartie au
prorata sur trois lignes donne 5,01 € après arrondi. Un développeur laissé seul face à ce
centime prendra une décision — au hasard, et différente d'un développeur à l'autre. La
spécification tranche : *l'écart est imputé à la ligne dont le montant net est le plus
élevé, et en cas d'égalité à celle dont la référence produit est alphabétiquement la plus
petite*. C'est le genre de phrase qui distingue une spécification d'une intention.

**Un mode dégradé qui est une décision commerciale.** « Si le service de fidélité est
indisponible, la commande est calculée sans imputation des points et le client en est
informé ; les points ne sont pas débités. » Ce n'est pas une décision d'exploitation :
c'est un arbitrage entre perdre une vente et mécontenter un client.

### Ce qu'il faut regarder dans l'exemple scientifique

Le phénomène — **une boisson chaude qui refroidit** — est connu de tout le monde et se
calcule avec une loi croisée au lycée. C'est exactement ce qu'on cherche pour un étalon :
aucune connaissance de domaine ne peut y masquer une ambiguïté de rédaction.

**`RG-010` — le modèle, pas la méthode.** La spécification énonce l'équation
différentielle et ses quatre hypothèses assumées. Elle **n'impose ni la solution exacte,
ni un intégrateur numérique** : les deux sont conformes dès lors qu'elles respectent les
tolérances. Écrire « intégrer par Runge-Kutta d'ordre 4 au pas de 1 s » serait une faute
— cela interdirait la forme fermée et deviendrait faux le jour où la température ambiante
varierait.

**`RG-040` — l'ordre des opérations, et il est contre-intuitif.** Ajouter le lait tout de
suite puis attendre un quart d'heure donne **55,18 °C** ; attendre puis ajouter le lait
donne **54,52 °C**. Mêmes ingrédients, même durée, deux tiers de degré d'écart — parce
qu'une boisson déjà refroidie par le lait perd ensuite sa chaleur plus lentement. Aucun
développeur ne peut deviner cela, et aucun ne doit avoir à en décider. C'est le pendant
exact de l'ordre d'application des remises dans l'exemple de gestion.

**`RG-050` — refuser de répondre, de trois façons différentes.** La cible peut être déjà
atteinte, inatteignable (sous la température ambiante), ou atteinte au-delà de l'horizon.
Trois indicateurs distincts, parce qu'une valeur absente seule confondrait trois
situations physiquement différentes — et trois messages différents pour l'utilisateur.

**`RG-060` — la non-convergence est une décision métier.** Le métier tranche : on ne
renvoie rien, on signale. La tentation du développeur — renvoyer le dernier itéré — est
précisément ce qui fabrique les résultats faux silencieux.

**`CT-06` — trois nombres qui valident une symétrie.** Les mêmes données exprimées en
kelvins doivent donner exactement le même instant. Ce cas détecte l'erreur classique
consistant à oublier la température ambiante dans l'exponentielle : plausible en Celsius,
absurde en Kelvin.

**§11 — une spécification, deux fiches de contraintes, deux implémentations.** L'usage
embarqué (moins d'1 ms, hors ligne, sur batterie) et l'usage laboratoire (température
ambiante variable) mènent le même document à **la forme fermée d'un côté et un
intégrateur adaptatif de l'autre** — validés par le même jeu d'essai. C'est la
démonstration la plus nette du cadre : la frontière a été tracée au bon endroit, puisque
deux solutions opposées sont l'une et l'autre conformes.

### Dans les deux cas

Chaque exemple se termine par une **lecture technique** : ce que l'architecte a *déduit*
de la fiche de contraintes. Cette section n'est pas écrite par le métier — elle est la
**réponse** de l'IT, et elle prouve que la spécification contenait bien tout ce qu'il
fallait.

> Si vous ne deviez retenir qu'une chose des deux exemples : ce n'est pas leur longueur,
> c'est que **chaque phrase y répond à une question qu'un développeur se serait posée en
> silence**.

---

## Annexe A — Aide-mémoire du pseudo-langage

À imprimer et à garder à côté du clavier. **Tout le lexique tient ici.**

```
STRUCTURE
  DÉFINIR nom(param : Type) : Type
  ENTRÉES · SORTIES · PRÉCONDITIONS · POSTCONDITIONS · INVARIANTS
  SOIT x = ...            (on introduit un nom, on ne le réaffecte jamais)
  RETOURNER x
  SIGNALER ERREUR E-XXX « message »

CONDITIONS          SI ... ALORS / SINON SI ... ALORS / SINON / FIN SI
                    (au-delà de deux conditions : table de décision)

ITÉRATION           POUR CHAQUE x DANS c ... FIN POUR
                    TANT QUE cond ... FIN TANT QUE   (à justifier)

ENSEMBLES           SOMME · MOYENNE · MINIMUM · MAXIMUM · NOMBRE DE
                    FILTRER c OÙ cond · REGROUPER c PAR critère
                    TRIER c PAR a CROISSANT|DÉCROISSANT
                    LE PREMIER · LE DERNIER · IL EXISTE · AUCUN · TOUS

OPÉRATEURS          + − × ÷ ^     = ≠ < ≤ > ≥     ENTRE a ET b
                    ET · OU · NON · DANS
VALEURS             VRAI · FAUX · ABSENT
IMPOSÉE             ARRONDIR(valeur, décimales, mode)

TYPES               <nom> : <Famille>(<unité pivot> ▸ <unité d'usage>,
                                      <précision>, <plage>)
  Familles : Entier · Flottant · Décimal · Chaîne · Booléen · Énuméré
             Horodatage · Durée · Liste[…] · Table[…] · Matrice[…]

IDENTIFIANTS
  FN fonction · SPEC spécification · RG règle · P paramètre · D donnée
  EX exigence · ET étape · GR groupe · INV invariant · CT cas de test
  E erreur · Q question · SM suggestion · N notice · C/H contrôle
  + un UUID par objet, attribué une fois, jamais réattribué

LES 8 QUESTIONS À SE POSER AVANT DE DIRE « C'EST FINI »
  1. Chaque grandeur a-t-elle son unité, sa devise, son fuseau ?
  2. Chaque SI a-t-il son SINON ? Chaque table est-elle complète ?
  3. Où arrondit-on, à combien, dans quel sens, et où va le résidu ?
  4. Que fait-on des ex æquo ?
  5. Que fait-on d'une donnée absente, nulle, négative, aberrante ?
  6. L'ordre d'application des règles change-t-il le résultat ? Si oui, est-il écrit ?
  7. Chaque règle est-elle couverte par au moins un cas de test calculé à la main ?
  8. Les volumes, la latence, la précision et la rejouabilité sont-ils chiffrés ?
```

---

*Ce cadre est un outil, pas un règlement. Une section qui ne sert jamais doit être
supprimée ; une question qui revient toujours doit être ajoutée à la liste de
vérification. La seule règle non négociable est la première : la spécification s'écrit
avant le code.*
