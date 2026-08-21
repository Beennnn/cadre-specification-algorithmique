# Guide 5 — Valider avant de développer

*Le jalon. Une spécification non validée ne part pas en développement — c'est la règle,
et elle n'a pas d'exception.*

---

## Pourquoi un jalon explicite

Sans jalon, la spécification part « quand elle a l'air prête ». Personne n'a dit oui,
personne ne s'est engagé, et les défauts se découvrent en développement — c'est-à-dire au
moment où ils coûtent le plus cher et où on est le moins disposé à revenir en arrière.

Le jalon a trois effets, et le troisième est le plus important :

1. Il **date** l'engagement : à partir de là, la spécification fait foi.
2. Il **nomme** ceux qui ont dit oui, et sur quoi.
3. Il **force la relecture à avoir lieu**. Un document qui doit être validé est un
   document qui est lu.

## La validation se fait en deux étages

| | **Étage 1 — les contrôles mécaniques** | **Étage 2 — la relecture qualité** |
|---|---|---|
| Qui | un script, ou une IA relectrice | des humains, trois casquettes |
| Ce qu'on cherche | des incohérences **formelles** : orphelins, doublons, oublis | de la compréhensibilité et de la justesse métier |
| Verdict | binaire, reproductible | argumenté |
| Coût | quelques secondes | quelques heures |
| Quand | **avant** l'étage 2, à chaque modification | une fois l'étage 1 vert |

> **L'ordre n'est pas négociable.** Faire relire par des humains un document qui contient
> encore un paramètre déclaré et jamais utilisé, c'est gaspiller la ressource la plus
> rare de la démarche. Les contrôles mécaniques nettoient d'abord ; les humains se
> concentrent ensuite sur ce que seule une lecture humaine peut voir.

## Étage 1 — les contrôles mécaniques

Les règles sont écrites **une fois**, dans
[`outils/REGLES-DE-CONTROLE.md`](../outils/REGLES-DE-CONTROLE.md), et servent trois
lecteurs : un développeur qui les lit, un script qui les exécute, une IA à qui on les
donne en consigne.

Ce qu'elles attrapent, réparti en quatre familles :

**Cohérence des données** — c'est le cœur, et ce que la relecture humaine rate le plus.

| | |
|---|---|
| `C-01` | une entrée déclarée que **aucune règle n'emploie** — entrée morte |
| `C-02` | une sortie déclarée que **aucune règle ne produit** |
| `C-03` | une grandeur employée dans une règle et **déclarée nulle part** — le paramètre fantôme qui circule |
| `C-04` | un paramètre `P-xx` déclaré et **jamais employé** |
| `C-05` | une **valeur littérale** au milieu d'une règle : soit c'est un paramètre, soit c'est une valeur magique |
| `C-06` | une grandeur **sans unité**, sans devise ou sans domaine |
| `C-07` | une donnée qui **change d'unité** entre sa déclaration et son emploi |

**Complétude logique** — `C-08` à `C-13` : un `SI` sans `SINON`, une table de décision
incomplète, un arrondi sans sens, un « le plus petit » sans départage, une entrée
facultative sans comportement d'absence, une itération sans critère d'arrêt.

**Traçabilité** — `C-14` à `C-18` : une règle absente de la table de couverture, un cas de
test cité qui n'existe pas, un identifiant employé deux fois, un terme métier absent du
glossaire.

**Version et statut** — `C-19` à `C-22` : l'en-tête qui ne correspond pas à l'historique,
un impact déclaré sur les résultats sans incrément majeur, une question ouverte sans
décideur, un statut « Acceptée » avec une question bloquante.

```bash
python3 outils/verifier.py exemples/fil-rouge/5-SPEC-NRG-001-autonomie.md
```

## Étage 2 — la relecture qualité

Ce que les contrôles mécaniques ne verront jamais. Trois casquettes, chacune avec sa
[liste de vérification](../templates/CHECKLIST-RELECTURE.md), et **cinq questions
transverses** qui font le cœur de la relecture qualité :

| | |
|---|---|
| `H-01` | **L'algorithme est-il compréhensible ?** Un développeur qui ne connaît pas le domaine peut-il l'implémenter sans deviner ? *Le relecteur compte les endroits où il devrait deviner. Chacun est un commentaire.* |
| `H-02` | **La logique des entrées et sorties tient-elle ?** Les sorties découlent-elles réellement des entrées ? Manque-t-il une donnée pour produire ce qui est promis ? |
| `H-03` | **Les règles décrivent-elles un résultat, ou un parcours ?** Une boucle explicite non justifiée est un choix d'implémentation qui s'est glissé dans la spécification |
| `H-04` | **Les hypothèses sont-elles explicites ?** Ce que le modèle ne représente pas doit être écrit, sinon on le découvrira en production |
| `H-05` | **Le cas riche du jeu d'essai est-il suivable de bout en bout ?** À la calculatrice, sans rien deviner |

> `H-01` mérite d'être pris au pied de la lettre : **on compte**. « J'aurais deviné trois
> fois » est un verdict utilisable ; « ça me paraît clair » n'en est pas un.

## Faire relire par une IA

Les règles de l'étage 1 étant écrites en langage naturel et non ambiguës, elles peuvent
être données telles quelles à un modèle, avec la spécification, pour obtenir une
pré-relecture. Le prompt prêt à l'emploi est dans
[`outils/PROMPT-RELECTURE-IA.md`](../outils/PROMPT-RELECTURE-IA.md).

**Ce que ça apporte** — une passe exhaustive en quelques minutes, là où un humain fatigue
au bout de dix pages. Une IA ne se lasse pas de vérifier que le vingt-troisième paramètre
est bien employé quelque part.

**Ce que ça n'apporte pas, et il faut être net là-dessus :**

| | |
|---|---|
| Elle **ne valide pas** | son verdict est un avis, pas une signature. La validation reste un acte humain, engageant, daté |
| Elle **ne juge pas la justesse métier** | elle ne sait pas si le seuil devrait être à 3 ou à 4 articles ; personne ne le sait sauf le métier |
| Elle est **plausible même quand elle a tort** | ses constats se vérifient un par un dans le document avant d'être traités |
| Elle **hérite des angles morts** de la liste qu'on lui donne | ce qui n'est pas dans les règles ne sera pas cherché |

> Le bon usage : l'IA en **troisième passe de l'étage 1**, après le script et avant les
> humains. Elle attrape ce que le script ne sait pas formaliser — une règle floue, une
> hypothèse implicite, une réaffectation qui viole l'immutabilité (`H-07`) — et elle libère
> les relecteurs humains pour ce qu'eux seuls peuvent faire.

## L'humain aux commandes

> **Principe : toute production d'une IA est assumée par une personne nommée, qui en
> répond comme si elle l'avait écrite elle-même.**

### Le terme juste

Trois notions circulent, et on les confond parce qu'elles se ressemblent. Elles ne disent
pas du tout la même chose.

| Notion | Ce qu'elle décrit | Ce qu'elle garantit |
|---|---|---|
| **Humain dans la boucle** *(human-in-the-loop)* | l'humain est un **maillon d'exécution** : le système s'arrête et attend son intervention | qu'il intervient — pas qu'il comprend, ni qu'il répond |
| **Humain sur la boucle** *(human-on-the-loop)* | l'humain **supervise** un système qui tourne seul, et peut l'interrompre | qu'il peut intervenir — s'il remarque quelque chose |
| **Humain aux commandes** *(human-in-command)* | l'humain **décide si, quand et comment** le système est employé, **et il en répond** | l'imputabilité |

Ce dont il s'agit ici est le troisième. Ce n'est pas une question de **position dans la
boucle**, c'est une question de **responsabilité**. Un relecteur peut être parfaitement
« dans la boucle » et n'assumer rien du tout — il lui suffit d'approuver sans lire.

> La distinction est posée par le groupe d'experts de haut niveau de la Commission
> européenne (*Ethics Guidelines for Trustworthy AI*, 2019), qui place explicitement
> l'humain **aux commandes** au-dessus des deux autres modes. Le règlement européen sur
> l'intelligence artificielle (2024) en tire les conséquences pratiques à son article 14
> sur la surveillance humaine : comprendre les capacités et les limites de l'outil, être
> conscient du **biais d'automatisation**, savoir interpréter la sortie, et pouvoir décider
> de ne pas l'employer.

### Les trois conditions pour que l'assomption soit réelle

Écrire « un humain valide » ne crée pas de la responsabilité. Trois conditions doivent
être réunies, faute de quoi on n'a nommé qu'un fusible.

| # | Condition | Comment on la tient ici |
|---|---|---|
| **1** | **Vérifiabilité** — le relecteur doit pouvoir contrôler chaque affirmation | la consigne impose à l'IA de **citer un passage exact** pour chaque constat. Un constat non localisable n'est pas traitable, donc pas assumable |
| **2** | **Moyens** — du temps et la compétence de juger | les contrôles mécaniques passent **avant**, pour que le temps humain aille sur ce qui le mérite. Un relecteur à qui on donne deux cents pages en une heure ne valide pas : il tamponne |
| **3** | **Droit de dire non** — refuser la sortie, et refuser d'employer l'outil | la passe IA est **facultative**. Un relecteur peut la déclarer inexploitable et procéder sans elle, sans avoir à s'en justifier |

### Le piège, et il porte un nom

> **La zone de froissement morale** *(moral crumple zone,* M. C. Elish, 2019*)* : le
> dispositif où un humain, nominalement responsable, absorbe le blâme d'un système qu'il
> n'était pas en mesure de contrôler réellement.

C'est ce qui arrive quand on désigne un valideur sans lui donner les trois conditions
ci-dessus. Le processus **a l'air** responsable ; en pratique il a produit un fusible.

Le risque est accru par le **biais d'automatisation** : on accorde spontanément plus de
crédit à une sortie automatisée qu'à sa propre lecture — d'autant plus qu'elle est bien
formulée. **Une IA écrit bien. C'est précisément ce qui rend son verdict dangereux** :
rien, dans la forme, ne distingue le constat exact du constat inventé.

### Ce qu'on écrit dans le document

L'assomption doit laisser une trace, sinon elle n'existe pas. Au moment du jalon :

| | |
|---|---|
| **L'outil et sa version** | quel modèle, quelle consigne |
| **La date** | |
| **L'opérateur** | la personne qui a lancé la passe **et qui répond de ce qui en a été retenu** |
| **Les constats retenus / écartés** | deux nombres. L'écart entre les deux est ce qui montre qu'il y a eu jugement |

> **La signature est humaine, toujours.** Une IA n'a pas de mandat, ne rend de comptes à
> personne, et ne sera pas là dans trois ans quand un auditeur demandera pourquoi cette
> règle a été acceptée. Le contrôle `C-27` vérifie qu'une passe IA déclarée porte bien un
> opérateur nommé.

## Le jalon lui-même

**Qui valide, et sur quoi :**

| Rôle | Ce qu'il engage |
|---|---|
| **Propriétaire de la règle** | que les règles sont celles que l'organisation veut appliquer |
| **Relecteur métier** | que le domaine est correctement représenté, cas limites compris |
| **Répondant technique** | que c'est implémentable, et que les contraintes du §11 sont réalistes |
| **Relecteur test** | que la spécification est vérifiable : jeu d'essai, couverture, invariants |

**Comment c'est tracé** — l'approbation de la demande de fusion **est** la signature :
elle est datée, nominative, attachée à une version exacte du document, et vérifiable des
années plus tard. Aucun autre mécanisme n'est nécessaire.

Au moment où la validation est prononcée :

1. Le statut passe de `En revue` à **`Acceptée`**.
2. Le numéro de version est figé, et une étiquette est posée
   ([guide 7](7-VERSIONNER.md)).
3. La date d'effet est fixée.

## Les conditions de passage

- [ ] **Étage 1 vert** — `C-01` à `C-22`, aucun échec
- [ ] **Étage 2** — les trois casquettes ont relu, `H-01` à `H-05` traitées
- [ ] Aucune **question ouverte bloquante** ne subsiste
- [ ] Le **jeu d'essai** est complet, calculé à la main, et couvre chaque règle
- [ ] La **fiche de contraintes** est chiffrée et jugée réaliste par le développement
- [ ] Le **glossaire** de référence est nommé, avec sa version
- [ ] La **date d'effet** est fixée
- [ ] Le **répondant métier** est nommé, et son délai de réponse est connu

Tant qu'une case n'est pas cochée, **le développement ne démarre pas**. Ce n'est pas de
la bureaucratie : une spécification qui part avec une case décochée revient toujours, et
elle revient plus tard.

## Ce qui se passe si on découvre un défaut après la validation

Cela arrivera, et ce n'est pas un échec du jalon.

- Le développement ouvre une `Q-xx`, **il ne corrige pas en silence**.
- Le propriétaire arbitre.
- La spécification est amendée, avec une nouvelle version — dont l'incrément est
  déterminé par la règle du [guide 7](7-VERSIONNER.md), pas par le confort.
- Si l'incrément est **majeur**, la validation est **rejouée** : une règle qui change un
  résultat n'est jamais un correctif de rédaction.

## Anti-patterns

| Anti-pattern | Pourquoi c'est grave |
|---|---|
| **La validation par silence** (« sans retour sous 8 jours, c'est validé ») | supprime précisément ce qu'on cherchait |
| **La relecture humaine avant les contrôles mécaniques** | gaspille la ressource la plus rare sur des défauts qu'un script trouve en une seconde |
| **Le développement qui démarre « en parallèle »** | la spécification devient une justification a posteriori |
| **Le verdict d'une IA pris pour une validation** | personne ne s'est engagé, et l'IA n'a pas de mandat |
| **La validation d'un document sans version figée** | on ne saura pas ce qui a été validé |
| **Le relecteur unique portant trois casquettes** | il ne verra que ce que sa vraie casquette voit |
