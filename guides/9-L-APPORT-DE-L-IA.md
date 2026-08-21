# Guide 9 — Ce que l'IA apporte, et ce qu'elle ne peut pas apporter

*Transverse. Une IA peut intervenir à presque toutes les étapes ; elle ne décide à aucune.*

---

## Le principe, qui ne change pas

> **Une IA n'est jamais un valideur.** Elle instruit, elle propose, elle signale. La
> signature reste humaine, datée, nominative — et **la personne qui a lancé la passe
> assume ce qu'elle en a retenu comme si elle l'avait écrit**.

C'est le principe de [l'humain aux commandes](../REFERENCES.md). Tout ce guide s'y
subordonne : aucun des usages ci-dessous ne déplace la responsabilité.

## Là où elle aide vraiment : le contrat

C'est son terrain le plus rentable, parce que c'est un travail **exhaustif et fastidieux**
que l'humain fait mal — non par incompétence, mais parce qu'il fatigue. Une IA ne se lasse
pas de vérifier que le vingt-troisième paramètre sert à quelque chose.

**Sur la cohérence entrées / sorties :**

| Ce qu'elle détecte | Pourquoi ça compte |
|---|---|
| Une **entrée déclarée qu'aucune règle n'emploie** | Soit une règle manque, soit on fait fournir une donnée pour rien |
| Une **sortie promise que rien ne produit** | Le contrat annonce un résultat que le calcul ne fabrique pas |
| Une **grandeur employée et déclarée nulle part** | Un paramètre fantôme circule : le développeur devra inventer d'où il vient |
| Un **paramètre déclaré et jamais employé** | Maintenu, versionné, documenté — pour rien. Et un jour quelqu'un le changera en croyant agir |
| Une **grandeur sans unité, sans plage, sans précision** | La cause la plus fréquente d'écarts, et la plus tardive à se découvrir |
| Une **incohérence de vocabulaire** entre le contrat, les règles et le glossaire | Le lecteur cherche une nuance qui n'existe pas, et finit par en inventer une |

**Sur la complétude logique :** un `SI` sans `SINON`, une table de décision incomplète, un
arrondi sans sens déclaré, un superlatif sans règle de départage, une entrée facultative
dont l'absence n'est pas traitée, une itération sans nombre maximal.

**Sur ce qui n'a rien à faire là :** une **contrainte qui relève de l'implémentation**,
glissée dans la spécification — un nom de bibliothèque, une structure de données, « stocker
dans une table », « mettre en cache », « boucler sur la liste ». Ce sont des décisions qui
appartiennent au développement, et elles se repèrent bien mieux à la lecture qu'avec une
liste de mots-clés (`C-40`).

**Sur les besoins pas assez clairs :** les formulations qui repoussent la décision —
« etc. », « le cas échéant », « en général », « si nécessaire », « on gère les cas
particuliers », « comme d'habitude ». Chacune est une question que quelqu'un devra
trancher plus tard, sans mandat.

```bash
# la passe outillée d'abord, l'IA ensuite : voir guide 5
python3 outils/verifier.py <la spécification>
```

Puis la consigne prête à l'emploi : [`outils/PROMPT-RELECTURE-IA.md`](../outils/PROMPT-RELECTURE-IA.md).

## Ses apports, étape par étape

| Étape | Ce qu'elle peut faire | Qui décide |
|---|---|---|
| **2–3 Découper, nommer** | Proposer un découpage candidat à partir de documents existants ; repérer les verbes fourre-tout (« gérer », « traiter ») et les noms qui désignent un endroit plutôt qu'un résultat ; proposer des termes de glossaire | Le métier — elle ignore qui décide de quoi dans l'organisation |
| **3 Données** | Repérer les données citées sans source, sans unité, sans comportement d'absence | Le métier |
| **4 Écrire** | Reformuler de la prose en pseudo-langage ; transformer des `SI` imbriqués en table de décision ; signaler une boucle qui pourrait être une opération d'ensemble | Le métier — **elle ne fournit jamais la règle** |
| **5 Jeu d'essai** | **Proposer les cas aux limites qu'on n'a pas pensés** : zéro, vide, un seul élément, valeur exactement au seuil, ex æquo, demi-centime, bascule de date, dépassement de plafond. Calculer les résultats attendus | Le métier — les résultats restent des **candidats jusqu'à examen** |
| **7–8 Valider** | Tout ce qui précède, en une passe exhaustive | Les trois relecteurs |
| **10 Coder et tester** | Produire une implémentation candidate ; transformer chaque `CT-xx` en test nommé ; dériver des tests de propriété depuis les invariants | Le développeur, qui répond de l'architecture, de la qualité dans la durée et de ce qu'il livre |
| **11 Écarts** | Comparer obtenu et attendu **poste par poste le long de la chaîne**, localiser le premier point de divergence, proposer des hypothèses de cause | Le métier — **elle ne juge pas si l'écart est significatif** |
| **Figer les jeux** | Produire, formater et versionner les jeux de données ; générer la trace de provenance | Le métier accepte, et c'est cette acceptation qui en fait des données de référence |

> **Le meilleur apport de la liste est celui de l'étape 5.** Proposer les cas aux limites
> est exactement ce qu'un expert fait mal : il connaît trop bien son domaine pour en voir
> les trous. Une IA n'a pas ce biais — elle n'a pas d'intuition à protéger.

## Ce qu'elle ne peut pas faire

Ces limites ne sont pas des défauts de maturité. Elles sont **structurelles**.

| | |
|---|---|
| **Dire si la règle est la bonne règle** | Elle ne sait pas si le seuil devrait valoir 3 ou 4 articles. Personne ne le sait, sauf le métier (`H-06`) |
| **Juger qu'un écart est acceptable** | C'est un arbitrage entre des conséquences, pas un calcul |
| **Savoir ce qui manque et n'a laissé aucune trace** | **C'est la limite la plus importante.** Une décision métier que personne n'a jamais posée ne manque à aucune règle : elle est invisible. L'IA travaille sur ce qui est écrit — l'atelier de découpage et la relecture humaine sont là pour trouver ce qui ne l'est pas |
| **Assumer** | Elle n'a pas de mandat et ne rend de comptes à personne |

## Trois pièges, et ce qui les désamorce

| Piège | Ce qui le désamorce |
|---|---|
| **La plausibilité** — elle est convaincante même quand elle a tort | Exiger que **chaque constat cite un passage exact** du document, et le vérifier avant de le traiter |
| **Le biais d'automatisation** — à force qu'elle ait raison, on cesse de vérifier | Le même exigence, appliquée sans exception. Et compter les faux positifs : leur disparition totale est suspecte |
| **Le fusible moral** — faire « valider » par un humain une sortie qu'il ne peut pas réellement examiner | Lui donner le temps, la compétence et le **droit de dire non**. Sans ces trois conditions, on n'a pas créé de responsabilité : on a désigné un coupable |

## Ce qu'on trace

Une passe d'IA se déclare, comme tout le reste :

| | |
|---|---|
| **Quel modèle**, quelle version du catalogue de règles | pour pouvoir rejouer et comparer |
| **Qui l'a lancée**, et quand | c'est la personne qui assume ce qui en a été retenu |
| **Combien de constats** retenus, écartés, et pourquoi | les faux positifs récurrents signalent une **règle mal formulée**, pas une IA défaillante |

C'est l'objet du contrôle `C-27`. Une relecture par IA non tracée n'a pas eu lieu.
