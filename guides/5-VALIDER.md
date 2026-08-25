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

## Avant tout : figer une version

**On ne relit pas un document qui bouge.** Avant d'ouvrir la relecture, l'auteur fige une
version : numéro arrêté, étiquette posée ([guide 7](7-VERSIONNER.md)), plus aucune
modification jusqu'au verdict.

Sans ce gel, trois choses se produisent, et elles suffisent à ruiner la relecture : deux
relecteurs commentent deux textes différents ; l'auteur corrige au fil de l'eau, si bien
que personne ne sait plus ce qui a été relu ; et l'approbation ne porte sur rien de
précis, donc n'engage personne.

Les corrections issues de la relecture produisent une **nouvelle version**, dont
l'incrément suit la règle habituelle — et si l'une d'elles change un résultat, la
validation est rejouée.

## La validation se fait en trois étages

| | **Étage 1 — contrôles mécaniques** | **Étage 2 — relecture qualité métier** | **Étage 3 — relecture qualité technique** |
|---|---|---|---|
| Qui | un **script**, puis une **IA** — deux passes distinctes, voir ci-dessous | le métier et le test | **un développeur qui ne connaît pas le domaine** |
| La question | y a-t-il des incohérences formelles ? | les règles sont-elles justes et complètes ? | **puis-je coder cela sans reposer de question ?** |
| Verdict | binaire, reproductible | argumenté | **binaire** |
| Coût | quelques secondes | quelques heures | une à deux heures |
| Quand | à chaque modification | une fois l'étage 1 vert | **en dernier**, une fois l'étage 2 clos |

> **L'ordre n'est pas négociable.** Faire relire par des humains un document qui contient
> encore un paramètre déclaré et jamais utilisé, c'est gaspiller la ressource la plus
> rare de la démarche. Les contrôles mécaniques nettoient d'abord ; les humains se
> concentrent ensuite sur ce que seule une lecture humaine peut voir.

## Étage 1 — les contrôles mécaniques

Il se déroule en **deux passes**, qui n'attrapent pas la même chose :

| | Ce qu'elle attrape | Son verdict |
|---|---|---|
| **1ʳᵉ passe — le script** [`Verifier.java`](../outils/Verifier.java) | ce qui se **compte** : entrées orphelines, paramètres morts, règles non couvertes, identifiants dupliqués, versions incohérentes | **certain**, en quelques secondes, sans faux positif |
| **2ᵉ passe — l'IA** | ce qui se **lit** : une règle floue, une contrainte d'implémentation glissée dans le texte, un « etc. » qui repousse une décision, un vocabulaire qui dérive | **à vérifier** — chaque constat doit citer un passage exact |

Le script vient en premier parce qu'il est gratuit et sûr ; l'IA ensuite, sur un document
déjà propre, où elle n'a plus à signaler ce qu'un contrôle mécanique aurait dû trouver.


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
| `C-03` | une grandeur **employée que rien ne déclare** — le fantôme qui circule. L'inverse de `C-01` : là une donnée est fournie pour rien, ici une donnée sort de nulle part |
| `C-04` | un paramètre `P-xx` déclaré et **jamais employé** |
| `C-05` | une **valeur littérale** au milieu d'une règle : soit c'est un paramètre, soit c'est une valeur magique |
| `C-06` | une grandeur **sans unité**, sans échelle ou sans domaine |
| `C-07` | une donnée qui **change d'unité** entre sa déclaration et son emploi |

**Complétude logique** — `C-08` à `C-13` : un `IF` sans `ELSE`, une table de décision
incomplète, un arrondi sans sens, un « le plus petit » sans départage, une entrée
facultative sans comportement d'absence, une itération sans critère d'arrêt.

**Traçabilité** — `C-14` à `C-18` : une règle absente de la table de couverture, un cas de
test cité qui n'existe pas, un identifiant employé deux fois, un terme métier absent du
glossaire.

**Version et statut** — `C-19` à `C-22` : l'en-tête qui ne correspond pas à l'historique,
un impact déclaré sur les résultats sans incrément majeur, une question ouverte sans
décideur, un statut « Acceptée » avec une question bloquante.

```bash
java outils/Verifier.java exemples/fil-rouge/5-SPEC-NRG-001.en.md
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

## Étage 3 — la relecture qualité technique

C'est le dernier verrou, et le seul qui mesure directement ce que la démarche promet.

**Qui.** Un développeur **qui n'a pas participé à l'écriture** et **qui ne connaît pas le
domaine**. Les deux conditions comptent : quelqu'un qui a assisté aux ateliers comblera
sans s'en rendre compte les trous avec ce qu'il a entendu.

**Le protocole.** Il lit la spécification **seule**, sans discussion, sans accès à
l'auteur, sans le code existant. Puis il répond à une seule question :

> **« Ai-je toutes les informations pour coder cela, sans reposer de question à
> personne ? »**

**Le verdict est binaire.** Pas « globalement oui », pas « à quelques détails près ». Si
la réponse est non, il produit **la liste exacte des questions qu'il aurait dû poser** —
et chacune devient soit une correction immédiate, soit une [suggestion de
modification](#les-suggestions-de-modification) `SM-xxx`.

**Le critère de sortie est zéro question.** Pas « peu de questions ». Une seule question
restante, c'est une décision que le développeur prendra seul, sans mandat — exactement ce
que toute la démarche cherche à éviter.

### Ce qu'on en mesure

Sur les premières spécifications, **on compte les questions**. C'est l'indicateur de
maturité le plus honnête dont on dispose, et il doit décroître d'une spécification à
l'autre.

| Ce qu'on observe | Ce que ça dit |
|---|---|
| 15 questions sur la première, 3 sur la cinquième | la démarche s'installe |
| Un nombre stable d'une spécification à l'autre | la liste de vérification ne couvre pas ce qui manque : **il faut l'enrichir** |
| Zéro question dès la première | le relecteur connaissait le domaine, ou n'a pas lu |
| Les mêmes questions qui reviennent | un manque du modèle de spécification, pas des auteurs |

**L'épreuve ultime**, quand l'enjeu le justifie : le relecteur **code effectivement** la
fonction, en boîte noire, et on compare au jeu d'essai. C'est le [test de la double
implémentation](../CADRE.md) exécuté pour de vrai. Coûteux, à réserver aux premières
spécifications de l'organisation — mais c'est lui qui convainc, là où aucun discours n'y
parvient.

## Les suggestions de modification

Le développement voit des choses que la relecture ne voit pas : une règle impossible à
tenir dans la contrainte de latence, une entrée qui n'existe pas dans les données réelles,
un cas que le jeu d'essai ne couvre pas. **Ces retours ne doivent pas se perdre dans une
conversation.**

Chaque proposition porte un identifiant `SM-xxx` et vit dans la spécification, au §12.2.

| Statut | Ce qu'il signifie | Ce qui est obligatoire |
|---|---|---|
| **En attente** | soumise, pas encore arbitrée | un décideur et une échéance |
| **Acceptée** | la spécification est modifiée comme suggéré | la version qui l'intègre |
| **Acceptée avec adaptation** | modifiée, mais autrement | **la description de ce qui a été retenu, et pourquoi ça diffère** |
| **Refusée** | la spécification ne change pas | **un motif écrit** |

> **Une suggestion n'est jamais fermée sans motif écrit.** C'est la règle qui fait vivre
> la boucle : un développeur dont les suggestions disparaissent sans réponse cesse d'en
> faire au bout de trois, et l'organisation perd son meilleur capteur de défauts de
> spécification.

Une suggestion **acceptée** devient un changement comme un autre : si elle a un impact,
elle porte une [notice `N-<version>`](7-VERSIONNER.md).

Le sens de circulation reste celui du cadre : **le développeur propose, le valideur métier de
la règle dispose.** Une suggestion n'est pas une modification, et elle ne s'applique jamais
directement au document par celui qui la propose.

## Faire relire par une IA

*Ce que l'IA apporte à chacune des treize étapes, et ce qu'elle ne peut structurellement pas
apporter : [guide 9](9-L-APPORT-DE-L-IA.md).*

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
| Elle **ne juge pas la justesse métier** | une règle peut être parfaitement claire, cohérente, complète — et fausse. Qu'on écarte une lecture à deux écarts-types quand le protocole d'étalonnage en vigueur en retient trois ne se lit dans aucun document : cela ne se sait que du métier |
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

## Itérer jusqu'à un niveau de qualité suffisant

La première passe ne conclut presque jamais. Les retours des trois étages — constats
mécaniques, remarques métier, questions du relecteur technique — se collectent, se
tranchent, et produisent une version suivante.

**Ce qui doit rester vrai à chaque tour :**

| | |
|---|---|
| Chaque retour a une **issue écrite** | corrigé, ou refusé avec un motif. Rien ne se perd dans une conversation |
| Chaque tour part d'une **version figée** | sinon on ne sait plus ce qui a été relu |
| Les étages **se rejouent dans l'ordre** | inutile de refaire relire par des humains si l'étage 1 est redevenu rouge |
| Le critère d'arrêt est **écrit d'avance** | zéro échec mécanique, zéro question du relecteur technique, zéro question ouverte bloquante |

**Combien de tours ?** Deux à trois sur les premières spécifications d'une organisation,
un seul quand la pratique est installée. Si le nombre de tours ne décroît pas d'une
spécification à l'autre, ce n'est pas les auteurs qu'il faut reprendre : c'est la liste de
vérification, qui ne couvre pas ce qui manque.

## Le jalon lui-même

**Qui valide, et sur quoi :**

| Rôle | Ce qu'il engage |
|---|---|
| **Valideur métier** | que les règles sont celles que l'organisation veut appliquer |
| **Relecteur métier** | que le domaine est correctement représenté, cas limites compris |
| **Co-auteur technique** | que c'est implémentable, et que les contraintes du §11 sont réalistes |
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
- [ ] **Étage 2** — les casquettes métier et test ont relu, `H-01` à `H-07` traitées
- [ ] **Étage 3** — un développeur extérieur au domaine répond **oui** à « puis-je coder
      sans reposer de question ? », et **aucune question ne subsiste**
- [ ] Les suggestions `SM-xxx` ouvertes sont arbitrées, ou explicitement reportées
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
- Le valideur métier arbitre.
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
