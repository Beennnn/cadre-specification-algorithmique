# Analyse de l'outillage

*Passe d'analyse de la méthode, axe outils. Mesurée le 2026-08-24 sur le dépôt à
l'état `ef7091b`. Toute affirmation de ce document est reproductible par les commandes
qui l'accompagnent.*

---

## Ce que l'axe promet

Le dépôt engage l'outillage sur trois affirmations, et elles sont fortes :

| Où | L'affirmation |
|---|---|
| [`README.md`](../README.md) | Le script passe en premier « parce qu'il est gratuit et sûr », et son verdict est « **certain** — un constat désigne un défaut, pas une suspicion » |
| [`guides/5-VALIDER.md`](../guides/5-VALIDER.md) | L'étage 1 nettoie **avant** les humains — « l'ordre n'est pas négociable » |
| [`REGLES-DE-CONTROLE.md`](REGLES-DE-CONTROLE.md) | « Une règle `C-xx` qui n'est pas mécanisable n'en est pas une : c'est un `H-xx` » |

Cette analyse mesure l'écart entre ces trois phrases et ce que les outils font
réellement. Elle ne juge pas le catalogue de règles, qui est l'actif : elle juge la
**couverture effective** de ce catalogue par ce qui tourne.

## Comment c'est mesuré

Quatre instruments, aucun jugement :

1. **Recomptage** des règles mécanisées sur les appels à `constat` de `Verifier.java`,
   confronté au catalogue.
2. **Rejeu** du verdict figé sur le corpus de défauts connus.
3. **Régénération** des artefacts engendrés — rapports de couverture, `registre.json` —
   et comparaison avec ce qui est commité.
4. **Campagne de mutation** : un défaut injecté à la main dans un document **réel** —
   pas dans la fixture — puis vérification que le contrôle correspondant se déclenche.
   C'est le seul instrument qui distingue un contrôle qui marche d'un contrôle qui
   n'a jamais eu l'occasion de se tromper.

---

## Ce qui tient

Cinq mesures, toutes conformes. Il faut le dire avant le reste : le socle est sain.

| Mesure | Attendu | Obtenu |
|---|---|---|
| Verdict sur le dépôt | 0 échec, 0 avertissement | **conforme** — 86 fichiers, 12 spécifications, 1,9 s |
| Verdict sur le corpus de défauts | 28 échecs, 3 avertissements | **conforme, constat par constat** |
| Décompte annoncé « 25 des 41 » | 25 règles distinctes citées en `constat` | **exact** — recompté, 41 règles au catalogue |
| Les trois rapports de couverture commités | identiques à leur régénération | **conformes**, les trois |
| `registre.json` | stable par régénération | **stable** — 246 identités, aucun écart |

Et la campagne de mutation confirme que les contrôles ne sont pas décoratifs : sur les
neuf contrôles mécanisés que le corpus de défauts n'exerce pas, **huit se déclenchent**
sur un défaut injecté dans un document réel.

| Contrôle | Défaut injecté | Verdict |
|---|---|---|
| `C-02` | une sortie du contrat renommée, plus employée par aucune règle | détecté |
| `C-11` | le départage explicite de « the first » retiré du fil rouge | détecté |
| `C-13` | le mot déclarant le nombre maximal de tours retiré | détecté |
| `C-15` | `CT-99` cité en table de couverture, défini nulle part | détecté |
| `C-29` | l'UUID d'une règle recopié sur une règle d'une autre spécification | détecté |
| `C-35` | une étape `ET-03` qui consomme une grandeur inexistante | détecté |
| `C-36` | une étape qui produit une grandeur que personne ne consomme | détecté |
| `C-42` | une identité retirée de la traduction française | détecté |
| `C-43` | une seconde itération ajoutée dans la même règle | **non détecté** |

> **Le neuvième mérite d'être lu correctement.** `C-43` n'est pas cassé : le même
> défaut, injecté cette fois **à l'intérieur d'un bloc `### RG-xxx`**, est signalé
> aussitôt. Ce qui a échappé au contrôle, c'est **l'endroit** où le défaut avait été
> mis — le bloc de l'algorithme intégré. La mutation ne révèle donc pas un contrôle
> défaillant : elle révèle une zone du document où cinq contrôles ne vont pas. C'est
> le premier défaut, et le plus grave.

---

## Ce qui ne tient pas

### 1. L'angle mort du §7.1 — l'algorithme intégré n'est lu par aucun contrôle

Les cinq contrôles qui analysent le pseudo-langage — `C-08`, `C-10`, `C-11`, `C-13`,
`C-43` — ne lisent que les blocs de code situés sous un titre `### RG-xxx`. Le bloc
« l'algorithme en un seul morceau », qui ouvre la section des règles, n'est examiné
par aucun d'eux.

Ce n'est pas un détail de portée. [`CADRE.md` §3.2](../CADRE.md) énonce que toute
spécification porte **deux vues du même calcul, et que les deux sont exigées** — et
que la vue intégrée est celle sur laquelle « le relecteur technique répond *oui, je
peux coder ça* ». C'est aussi elle qui porte **l'ordre d'enchaînement**, dont le
cadre dit expressément qu'il « fait partie de la spécification, pas de
l'implémentation ».

**Le vérificateur lit la vue traçable et ignore la vue vérifiable.**

Ce que ça représente, sur les six spécifications normatives :

| Spécification | Lignes lues | Lignes ignorées | Part ignorée |
|---|---|---|---|
| `SPEC-SPD-001.en` | 4 | 7 | 64 % |
| `SPEC-THM-001.en` | 48 | 0 | 0 % |
| `SPEC-NRG-001.en` | 46 | 17 | 27 % |
| `SPEC-MAS-001.en` | 18 | 14 | 44 % |
| `SPEC-AST-001.en` | 29 | 28 | 49 % |
| `SPEC-WTH-001.en` | 33 | 54 | 62 % |
| **Total** | **178** | **120** | **40 %** |

Et comme la vue intégrée **duplique** le contenu des règles, rien ne garantit que
les deux disent la même chose. La démonstration tient en trois lignes :

```bash
# on contredit la vue intégrée : le seuil devient 2 × P-01, la règle RG-030 garde P-01
sed -i '136s/P-01 × spread/2 × P-01 × spread/' exemples/weather-summary/spec/SPEC-WTH-001.en.md
java outils/Verifier.java
# → 86 fichier(s) examiné(s) — 0 échec(s), 0 avertissement(s)
```

Une spécification qui **se contredit elle-même** sur le seuil de rejet des valeurs
aberrantes passe l'étage 1 sans une remarque. Un développeur qui code la vue
intégrée et un développeur qui code les règles produisent deux programmes différents,
tous deux conformes au document.

> C'est le défaut le plus grave de l'axe, parce qu'il touche exactement la promesse
> du dépôt : ce n'est pas un contrôle qui manque, c'est un contrôle qui existe et
> qu'on croit appliqué là où il ne l'est pas.

### 2. L'oracle de non-régression n'exerce que 16 des 25 contrôles mécanisés

Le corpus de défauts connus est présenté comme « l'oracle de non-régression de
l'implémentation restante ». Il exerce `C-01`, `C-03`, `C-04`, `C-08`, `C-10`,
`C-14`, `C-17`, `C-19`, `C-20`, `C-21`, `C-23`, `C-24`, `C-26`, `C-28`, `C-38`,
`C-39` et le contrôle des liens — **16 règles sur les 25 mécanisées**.

Les neuf autres — `C-02`, `C-11`, `C-13`, `C-15`, `C-29`, `C-35`, `C-36`, `C-42`,
`C-43` — ne sont retenues par rien. Une refonte qui les casserait laisserait le
verdict figé intact, et le dépôt continuerait d'afficher `0 échec`.

La campagne de mutation montre que huit d'entre elles fonctionnent **aujourd'hui**.
Elle ne dit rien de demain : c'est précisément le travail d'un oracle, et il n'est
pas fait. Quant au neuvième, ce n'est pas le contrôle qui manquait mais la zone où
le défaut avait été mis — et c'est le défaut 1, que personne n'était en mesure de
voir tant que rien n'exerçait ces neuf contrôles.

### 3. Les artefacts engendrés ne sont gardés par rien

`registre.json` est l'index des identités durables — 246 entrées, commité. La méthode
le présente comme ce qui survit aux renommages et aux déplacements. Rien ne vérifie
qu'il correspond aux documents :

```bash
python3 -c "import json;d=json.load(open('registre.json'));json.dump(d[:-20],open('registre.json','w'))"
java outils/Verifier.java
# → 226 identités au lieu de 246, et : 0 échec(s), 0 avertissement(s)
```

Même constat sur les trois `COVERAGE-REPORT.md` : ils sont **conformes aujourd'hui**,
mais leur conformité tient à la discipline de celui qui commite, pas à un contrôle.

### 4. Les identités des documents qui ne sont pas des spécifications ne sont pas contrôlées

`Verifier.java` ne tient un fichier pour une spécification que s'il contient un titre
`### RG-xxx`. Les autres n'obtiennent ni `C-28` ni `C-29`. Or quatre documents du
dépôt portent des identités enregistrées sans être des spécifications :

| Document | Identités | `C-28`/`C-29` |
|---|---|---|
| `exemples/fil-rouge/1-DECOUPAGE.md` | 12 | non |
| `exemples/fil-rouge/3-DONNEES.md` | 11 | non |
| `exemples/fil-rouge/4-FN-004-planifier-les-recharges.md` | 5 | non |
| `guides/7-VERSIONNER.md` | 4 | non |

**32 des 246 identités du dépôt — 13 % — ne sont vérifiées par personne.** On peut
recopier l'UUID de `SPEC-SPD-001` sur la donnée `D-007` du catalogue : le vérificateur
reste muet. C'est exactement le défaut que `C-29` existe pour empêcher, et dont le
catalogue dit qu'il est « pire » que l'absence de traçabilité.

### 5. Les outils qui écrivent n'ont pas d'oracle

| Outil | Écrit dans le dépôt | Corpus de référence |
|---|---|---|
| `Verifier.java` | non | **oui** — verdict figé à 28/3 |
| `Identites.java` | **oui** — dans les spécifications elles-mêmes | non |
| `Couverture.java` | **oui** — `reports/COVERAGE-REPORT.md` | non |

L'asymétrie est exactement à l'envers de ce que le risque commande. Le seul outil qui
ne touche à rien est le seul qui soit gardé ; les deux qui modifient les documents ne
le sont pas. Et `Identites.java` a **déjà causé un incident réel** — 15 UUID fantômes
attribués dans les rapports engendrés, réémis à chaque exécution
([`CHANTIER.md`](../CHANTIER.md)). La correction a été faite par quatre filtres
d'exclusion, écrits **deux fois** dans le fichier, une fois par mode. Rien n'empêche
les deux copies de diverger, et rien ne le détecterait.

Point mineur mais de même famille : `java outils/Couverture.java <exemple>` **écrit**
un rapport dans l'arbre de travail. Interroger l'état de couverture modifie le dépôt ;
il n'existe pas de mode qui se contente d'afficher.

### 6. La documentation de l'outillage contredit l'outillage

| Où | Ce qui est écrit | Ce qui est vrai |
|---|---|---|
| [`README.md`](README.md), tableau d'ouverture | « le catalogue des règles de contrôle `C-01` à `C-23` (mécaniques) et `H-01` à `H-06` (humaines) » | le catalogue va jusqu'à `C-43`, et `H-07` existe — le même fichier lui consacre un paragraphe quarante lignes plus bas |
| [`README.md`](README.md), « Ce que le script ne fait pas » | mécanisés : `C-01` à `C-04`, `C-14` à `C-17`, `C-19` à `C-21`, `C-23`, `C-24`, `C-26` — soit quinze ; et « les contrôles `C-05` à `C-13` … demandent une lecture du pseudo-langage que le script ne fait pas » | vingt-cinq règles sont mécanisées, dont `C-08`, `C-10`, `C-11` et `C-13`, que cette phrase déclare non mécanisées. Le même fichier annonce « 25 règles sur 41 » deux paragraphes plus haut |
| [`CLAUDE.md`](../CLAUDE.md), « Après toute modification » | `java    outils/Verifier.java   # seconde implémentation : même verdict exigé` | l'implémentation Python a été retirée ; la consigne demande de lancer deux fois le même outil, et l'espacement trahit le `python3` qu'on a remplacé sans relire |

Le catalogue, lui, est juste : son décompte « 25 des 41 » est exact, et il porte même
l'encadré qui explique comment le recompter. C'est le README des outils qui n'a pas
suivi — et l'ironie est nette : **le seul document du dépôt dont la cohérence n'est
vérifiée par aucun contrôle est celui qui décrit les contrôles.**

### 7. L'étage 2 est prescrit, jamais exercé, et son prompt a un trou

`C-27` exige qu'une passe de relecture par IA déclare son opérateur, sa date et le
nombre de constats retenus et écartés. **Aucune spécification du dépôt n'en déclare
une.** L'étage 1 s'arrête donc en pratique au script, alors que le guide 5 le décrit
en deux passes dont la seconde est « celle qui apporte le plus ».

Et cette seconde passe a un trou précis. [`README.md`](README.md) et
[`guides/5-VALIDER.md`](../guides/5-VALIDER.md) désignent tous deux `H-07`
(immutabilité) comme le contrôle **où la passe IA apporte le plus** — « un modèle lit
correctement la structure des branches, un script non ». Or
[`PROMPT-RELECTURE-IA.md`](PROMPT-RELECTURE-IA.md) demande d'instruire « les contrôles
`H-01` à `H-06` ». Le prompt n'a pas été mis à jour quand `H-07` a été ajouté : le seul
contrôle pour lequel l'IA est déclarée indispensable ne lui est pas demandé.

### 8. Rien ne lance le vérificateur

Il n'y a pas de `.github/`. Le code de retour 1 est annoncé « utilisable tel quel en
intégration continue », et personne ne s'en sert. L'étage 1 dépend entièrement de ce
que le contributeur pense à taper — sur un dépôt dont la thèse est qu'on ne doit pas
dépendre de ce qu'un relecteur pense à faire.

### 9. La doctrine se contredit sur les 16 contrôles restants

Le catalogue se termine par : « une règle `C-xx` qui n'est pas mécanisable n'en est
pas une : c'est un `H-xx` ». Seize règles `C-xx` ne sont pas mécanisées.

Il faut être juste : le [`README.md`](../README.md) du dépôt ne se dérobe pas sur le
chiffre. Il assume le compromis, l'explique — « la lisibilité par le métier, donc un
formalisme léger, donc un contrôle partiel » — et va jusqu'à rappeler que le nombre
« se recompte, il ne se relit pas ». Sur cet axe, la page d'accueil est exemplaire.

Le défaut est donc étroit, et il est dans le catalogue seul : sa phrase de clôture
énonce une exigence que ses propres seize règles enfreignent. Ou bien c'est une
exigence, et ces seize règles sont mal classées ; ou bien c'est une cible, et la
phrase doit dire qu'elle en est une. En l'état, elle donne au lecteur pressé une
garantie que le catalogue ne tient pas.

---

## Ce que ça coûterait de reprendre la main

Par ordre de rapport, pas d'effort. Les trois premières lignes ferment la majorité de
l'écart mesuré.

| # | Ce qu'on fait | Ce que ça ferme | Effort |
|---|---|---|---|
| 1 | Étendre les cinq contrôles du pseudo-langage au bloc de l'algorithme intégré | défaut 1 — 40 % du pseudo-code | faible : le découpage existe, la portée du `Matcher` change |
| 2 | Ajouter un contrôle de **concordance entre les deux vues** : toute grandeur et tout `RG-xxx` cités dans la vue intégrée existent, et les branches se correspondent | la contradiction interne, aujourd'hui invisible | moyen — c'est une règle **nouvelle** au catalogue, à instruire comme telle |
| 3 | Étendre `C-28`/`C-29` à tout document portant une annexe d'identités | défaut 4 — 13 % des identités | faible : lever la condition « contient `### RG-` » pour ces deux contrôles seuls |
| 4 | Un contrôle « artefact engendré à jour » : régénérer `registre.json` et les rapports, échouer sur écart | défaut 3 | faible, et c'est ce qui donne son intérêt à la ligne 6 |
| 5 | Étendre le corpus de défauts aux neuf contrôles qu'il n'exerce pas | défaut 2 | moyen : neuf défauts à injecter, un verdict à refiger |
| 6 | Une action d'intégration continue qui lance `Verifier.java` et le rejeu du corpus | défaut 8 | très faible |
| 7 | Corriger les deux phrases fausses de `README.md`, la consigne périmée de `CLAUDE.md`, et le prompt IA (`H-01` à **`H-07`**) | défaut 6 et le trou du défaut 7 | trivial — quatre lignes |

Sur les seize contrôles non mécanisés, six le sont avec ce qui est déjà en place, sans
nouvelle convention d'écriture :

| Règle | Ce qu'il faudrait lire | Déjà disponible ? |
|---|---|---|
| `C-16` | chaque `E-XXX-nnn` est couvert par un `CT-xx` | oui — même machinerie que `C-14`/`C-15` |
| `C-41` | « etc. », « le cas échéant », « en général », « si nécessaire » | oui — liste fermée ; **zéro occurrence aujourd'hui**, donc coût nul et garde pour l'avenir |
| `C-27` | l'en-tête d'une passe IA porte opérateur, date, constats | oui — lecture d'en-tête |
| `C-25` | orthographe identique entre contrat et règles | oui — proche de `C-01`/`C-03` ; déjà ouvert en `CHANTIER.md` J |
| `C-06` | chaque champ déclare famille, précision, plage, unité | oui — la grammaire `Type(unité, plage, décimales) [famille]` est régulière |
| `C-05` | tout littéral d'une règle est un `P-xx` ou une constante nommée | oui — en avertissement, comme `C-11` |

`C-22` (« un document accepté n'a pas de question bloquante ») est mécanisable **une
fois le vocabulaire de statut fixé** : le dépôt emploie aujourd'hui quatre valeurs
différentes — *Accepted*, *Approved*, *Acceptée*, *Validée* — et aucun contrôle ne
s'en plaint. C'est un préalable, pas un obstacle.

---

## Ce que cette analyse ne tranche pas

| # | Question | Pourquoi elle revient au titulaire |
|---|---|---|
| i | La vue intégrée doit-elle être **contrôlée** ou **engendrée** à partir des règles ? | Engendrer supprime la duplication à la racine, mais retire au rédacteur la main sur l'ordre — que le cadre déclare pourtant normatif |
| ii | Les seize `C-xx` non mécanisés sont-ils une dette ou un classement à revoir ? | La réponse change la phrase finale du catalogue, qui est une prise de position, pas un détail |
| iii | Faut-il un oracle pour `Identites.java`, ou lui interdire d'écrire ? | Un outil qui modifie les spécifications sans corpus de référence est le point de rupture le plus probable du dépôt |
| iv | L'étage 2 doit-il être exercé **sur les exemples du dépôt** ? | Une méthode qui prescrit une passe qu'elle n'a jamais faite sur ses propres documents s'expose à la première objection venue |

---

*Reproduire les mesures de ce document : les commandes sont dans le corps du texte.
La campagne de mutation s'exécute sur une copie du dépôt — aucune mutation n'a été
commise, et le vérificateur retrouve `0 échec, 0 avertissement` sur l'arbre de
travail.*
