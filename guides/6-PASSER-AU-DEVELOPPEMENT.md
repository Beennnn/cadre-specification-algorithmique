# Guide 4 — Passer au développement

*Ce que le développement reçoit, et comment il s'en sert pour choisir l'architecture et
le langage, coder, optimiser et qualifier.*

---

## Le dossier de passation

Ce que le développement reçoit, ni plus ni moins :

| # | Élément | Où |
|---|---|---|
| 1 | Le **découpage** du périmètre : les fonctions, leurs valideurs métier, leurs niveaux | [guide 1](1-DECOUPER.md) |
| 2 | Le **glossaire** du domaine, verrouillé | [guide 2](2-GLOSSAIRE.md) |
| 3 | La **spécification** de chaque fonction du lot, acceptée | [modèle](../templates/MODELE-SPECIFICATION.md) |
| 4 | Les **questions ouvertes** restantes, avec décideur et échéance | §12 de chaque spécification |
| 5 | Le nom du **répondant métier** joignable, et sous quel délai | — |

Et ce qu'il ne reçoit pas : de consigne technique, de proposition d'architecture, de
préférence de langage. Si le dossier en contient, le métier est sorti de son mandat.

## La règle d'or : une spécification se transmet, elle ne s'envoie pas

> **45 minutes, l'auteur déroule le cas riche du §10 devant l'équipe, à voix haute.**

Ce moment vaut trois relectures. En déroulant `CT-03` étape par étape, l'auteur découvre
lui-même les endroits où il doit ajouter une phrase orale pour être compris — et chacune
de ces phrases est une lacune du document, à corriger séance tenante.

C'est aussi là que le développement pose les questions qui ne viennent pas à l'écrit.

## Le guide de lecture, par ce que vous cherchez à faire

Vous êtes développeur, vous ouvrez la spécification. Selon ce que vous voulez faire,
vous ne lisez pas la même chose.

| Ce que vous cherchez à faire | Ce que vous lisez |
|---|---|
| **Choisir l'architecture** | §11 fiche de contraintes, en entier |
| **Choisir le langage** | §11, lignes *exactitude*, *déterminisme*, *latence*, *mémoire*, *durée de vie* |
| **Coder** | §3 glossaire · §4-5 contrat · §7 règles · §6 paramètres |
| **Optimiser** | §8 invariants · §10 jeu d'essai · §11 lignes *déterminisme* et *latence* |
| **Qualifier** | §10 jeu d'essai · §8 invariants · §9 cas d'erreur |

### Choisir l'architecture

Quatre couples de lignes du §11 décident de presque tout :

| Couple | Ce qu'il tranche |
|---|---|
| **Mode d'appel + latence** | Service synchrone, traitement par lots, ou flux ; où le calcul s'exécute |
| **Volumétrie + profil de charge** | Dimensionnement, élasticité, parallélisation — et si elle est possible |
| **Rejouabilité + auditabilité** | Ce qu'il faut archiver, versionner et horodater — souvent la contrainte la plus structurante |
| **Fréquence de changement + qui modifie** | Code figé, configuration externalisée, ou moteur de règles |

> Sur le fil rouge, c'est le couple *latence + mémoire* qui a écarté, à bord, tout
> environnement à ramasse-miettes. Et c'est la ligne *qui modifie* qui a sorti le barème
> de température du programme. Voir [l'annexe technique de
> SPEC-NRG-001](../exemples/fil-rouge/5-SPEC-NRG-001-autonomie.md).

**Ce que la spécification ne vous dira pas, et que vous devez décider seul** : le
découpage en services, le stockage, le protocole, la stratégie de déploiement, le cache.
Aucun de ces choix ne peut trahir une règle — c'est précisément pour cela qu'ils sont à
vous.

### Choisir le langage

Trois lignes du §11 pèsent plus que toutes les préférences d'équipe :

- **Exactitude.** « Exact au centime » impose un décimal exact et écarte le flottant
  binaire. « Tolérance relative de 10⁻⁶ » rend la double précision largement suffisante.
  « Reproductibilité 10⁻⁹ entre deux implémentations » exclut la simple précision.
- **Déterminisme.** Un déterminisme strict interdit les réductions en ordre non garanti,
  donc certaines bibliothèques parallèles, dont le résultat dépend du nombre de fils.
- **Latence, mémoire, durée de vie.** Un calcul embarqué en moins de 20 ms sans allocation
  dynamique n'a pas le même profil qu'un service serveur ; un logiciel prévu pour quinze
  ans n'a pas le même que celui qu'on jettera dans deux.

> **Les trois exemples de ce dépôt aboutissent à trois conclusions différentes** —
> décimal exact, double précision confortable, double précision indispensable — à partir
> de la même méthode. C'est la démonstration que c'est la spécification qui décide, et
> non l'habitude.

### Coder

- **La correspondance des noms est mécanique et systématique.** La spécification écrit
  `snake_case` en ASCII ; vous appliquez la convention de votre langage —
  `montant_net_ht` → `montantNetHt` en Java, `MontantNetHt` en C#, inchangé en C ou en
  Python ([CADRE §2.4](../CADRE.md)). La règle est déclarée une fois, par cible : c'est
  ce qui permet de retrouver un nom de la spécification dans le code sans le chercher.
- Le **glossaire est votre nomenclature.** Les noms des règles deviennent les noms des
  fonctions, les termes du domaine deviennent les noms des types et des variables. Si
  votre code emploie un mot qui n'est pas au glossaire, ou l'inverse, l'un des deux a
  tort.
- **Citez l'identifiant de règle en commentaire** à l'endroit qui l'implémente. C'est ce
  qui rend possible, plus tard, de répondre en dix secondes à « où est implémentée cette
  règle ? ».
- Les règles écrites en **opérations d'ensemble** (`SOMME DES`, `FILTRER`, `REGROUPER
  PAR`) vous laissent explicitement libres : agrégation en mémoire, requête en base,
  vectorisation. Rien de tout cela ne peut trahir la règle.
- **Ne corrigez jamais une règle en silence.** Si vous rencontrez un cas non prévu — et
  vous en rencontrerez —, ouvrez une `Q-xx` dans la spécification. Une décision métier
  prise dans un commit est une décision perdue.

### Optimiser

> **La spécification vous autorise tout ce qui ne change pas un résultat observable.**
> Elle ne vous interdit que deux choses : violer un invariant, et changer une règle.

| Ce que vous pouvez faire sans rien demander | Ce qui exige une `Q-xx` |
|---|---|
| Changer de structure de données, d'algorithme, de bibliothèque | Changer l'ordre d'application de deux règles quand il modifie le résultat |
| Précalculer, mettre en cache, mémoïser | Modifier un arrondi, une borne, un départage |
| Paralléliser — **si** le §11 n'impose pas un ordre déterministe | Introduire une approximation qui sort de la tolérance du §8 |
| Remplacer une boucle par une agrégation en base | Ignorer un cas parce qu'il « n'arrive jamais » |

**Le jeu d'essai est votre garde-fou.** Optimisez autant que vous voulez tant que les
`CT-xx` passent et que les invariants tiennent. C'est exactement pour cela qu'ils
existent.

> **Attention aux optimisations que la spécification interdit sans le dire fort.** Sur le
> fil rouge, `RG-080` précise que l'énergie cumulée **n'est pas monotone** — ce qui
> interdit la recherche dichotomique, qui est pourtant le premier réflexe. La règle le
> dit, `CT-01` le prouve. Sans elle, l'optimisation aurait été faite et le défaut livré.

### Qualifier

> **Le §10 n'est pas un exemple : c'est le plan de recette.** Il couvre l'ensemble des cas
à tester, avec pour chacun le jeu de données d'entrée **et** le jeu de données de sortie
attendu.

| Élément de la spécification | Ce qu'il devient en test |
|---|---|
| Chaque `CT-xx` | Un test automatisé, **nommé par son identifiant** |
| Chaque `INV-xx` | Un test de propriété, exécuté sur des entrées générées |
| Chaque `E-xxx` du §9 | Un test de cas d'erreur, vérifiant **aussi** qu'aucun résultat partiel n'est produit |
| La table de couverture | L'indicateur de complétude : une règle sans test est un trou visible |
| Les niveaux d'exactitude du §8.2 | **Des campagnes distinctes**, pas un seul jeu de tests |

Ce dernier point est celui qu'on rate le plus souvent sur un logiciel scientifique. Trois
questions différentes exigent trois campagnes différentes :

| Campagne | Question | Fréquence |
|---|---|---|
| **Reproductibilité** | Deux implémentations conformes donnent-elles le même nombre ? | à chaque livraison |
| **Justesse numérique** | Le nombre est-il la vraie valeur des formules ? | à chaque livraison |
| **Validité du modèle** | Le modèle décrit-il la réalité ? | périodiquement, **avec le métier** |

Confondre les trois, c'est transformer chaque écart de mesure en rapport de bogue — et
faire chercher à l'équipe un défaut dans du code correct.

## Et après : l'analyse des écarts

Les tests rejouent les entrées du §10 et comparent les résultats obtenus aux résultats
attendus. **Ils ne coïncideront pas toujours** — et c'est une étape à part entière, pas un
incident.

Elle est conduite **par le métier, avec le support des développeurs**, en lisant les
valeurs propagées d'une étape à l'autre : voir
**[guide 8 — Analyser les écarts](8-ANALYSER-LES-ECARTS.md)**.

Une conséquence à anticiper **dès l'écriture** : une fonction dont on devra analyser les
écarts doit exposer en sortie les **valeurs intermédiaires** de sa chaîne de traitement.
Sans elles, on constate que ça diverge sans pouvoir dire où.

## Ce que le développement renvoie au métier

La passation n'est pas à sens unique. Trois retours sont attendus, et ils sont tracés :

1. **Les questions découvertes en développement** (`Q-xx`), avec le cas qui les a
   provoquées. C'est le retour le plus précieux : il dit exactement où la spécification
   était trop courte.
2. **Les contraintes irréalistes, chiffrées.** « La rejouabilité à 10 ans multiplie le
   coût de stockage par 40 : voici trois options et leur prix. » Le métier arbitre.
3. **Les écarts assumés** entre la spécification et l'implémentation, documentés — il y
   en a toujours, et non documentés ils deviennent de la dette invisible.

## La liste de contrôle de la passation

**Avant de partir en développement :**

- [ ] Les trois relecteurs ont approuvé
- [ ] Aucune question ouverte **bloquante** ne subsiste
- [ ] La séance de transmission de 45 min a eu lieu
- [ ] Le répondant métier est nommé, et son délai de réponse est connu
- [ ] La fiche de contraintes est chiffrée, et le développement l'a jugée réaliste

**Avant de déclarer terminé :**

- [ ] Le jeu d'essai passe intégralement, en automatisé
- [ ] Chaque `RG-xxx` est citée dans le code **et** dans au moins un test
- [ ] Les invariants sont testés sur des entrées générées
- [ ] Les questions apparues en développement ont été arbitrées et reportées dans la
      spécification
- [ ] Les écarts assumés sont documentés
