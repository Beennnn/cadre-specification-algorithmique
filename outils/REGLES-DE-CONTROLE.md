# Règles de contrôle d'une spécification

*Écrites une fois, pour trois lecteurs : un relecteur humain qui les applique, un script
qui les exécute ([`Verifier.java`](Verifier.java)), une IA à qui on les donne en consigne
([`PROMPT-RELECTURE-IA.md`](PROMPT-RELECTURE-IA.md)).*

**Ce qui est mécanisé.** `Verifier.java` met en œuvre `C-01` à
`C-04`, `C-08`, `C-10`, `C-11`, `C-13` à `C-15`, `C-17`, `C-19` à `C-21`, `C-23`, `C-24`,
`C-26`, `C-28`, `C-29`, `C-35`, `C-36`, `C-38`, `C-39` — soit **23 des 39**. Les seize
restants (`C-05` à `C-07`, `C-09`, `C-12`, `C-16`, `C-18`, `C-22`, `C-25`, `C-27`, `C-30`
à `C-32`, `C-37`, `C-40`, `C-41`) demandent un jugement ou une lecture que le script ne
fait pas.

> **Ce décompte est vérifiable.** Il a été faux : le dépôt a longtemps annoncé « 31 des
> 41 », en citant des règles `C-33` et `C-34` qui n'existent pas et en omettant `C-39`.
> Une prose de couverture ne se relit pas, elle se recompte — sur les appels à `constat`
> des deux implémentations.

`C-11` est **heuristique** : il ne se déclenche que sur un superlatif portant sur une
collection, un superlatif entre deux scalaires étant sans ambiguïté. Il peut donc laisser
passer un cas, et il est classé en avertissement à ce titre.

`C-03` est **heuristique** lui aussi, dans l'autre sens. Il ne considère comme grandeur
qu'un identifiant contenant un `_` : c'est la convention `snake_case` (`C-38`) qui le
distingue du français ordinaire entourant le pseudo-code, faute de quoi « ligne » ou
« que » seraient signalés. Un fantôme nommé d'un seul mot lui échappe donc. Un nom est
tenu pour déclaré s'il est introduit dans les règles — `LET`, `FOR EACH`, ou membre
gauche d'une affectation, champ compris — ou s'il apparaît ailleurs dans le document.

> **Ce contrôle a fait ses preuves le jour où il a été écrit.** Passé sur nos propres
> exemples, il a trouvé deux fantômes réels : `instant_ajout`, que l'algorithme employait
> alors que le contrat déclarait `ajout.instant` — la même grandeur sous deux noms — et
> `energie_cumulee_avant`, employé sans avoir jamais été défini. Les deux avaient
> traversé la relecture humaine.

Deux familles :

- **`C-xx` — contrôles mécaniques.** Vérifiables sans connaître le domaine. Verdict
  binaire. Un échec est un défaut, pas une opinion.
- **`H-xx` — contrôles humains.** Demandent un jugement. Une IA peut les pré-instruire,
  elle ne peut pas les trancher.

---

## Cohérence des données

C'est la famille qui rapporte le plus, parce que c'est celle que la relecture humaine rate
le plus souvent : personne ne vérifie spontanément que le vingt-troisième paramètre sert
à quelque chose.

| Id | Règle | Ce qu'un échec révèle |
|---|---|---|
| `C-01` | **Toute entrée déclarée est employée** dans au moins une règle. | Une entrée **morte** : déclarée sans emploi. Soit une règle manque, soit l'entrée est inutile — et on demande à l'appelant de fournir une donnée pour rien |
| `C-02` | **Toute sortie déclarée est produite** par au moins une règle. | Une promesse non tenue : le contrat annonce un résultat que rien ne calcule |
| `C-03` | **Toute grandeur employée dans une règle est déclarée** quelque part : entrée, sortie, paramètre, ou variable locale introduite par `LET`. | Un **fantôme** circule : une grandeur employée que rien ne déclare, dont le développeur devra inventer l'origine. **L'inverse de `C-01`** — voir l'encadré ci-dessous |
| `C-04` | **Tout paramètre `P-xx` déclaré est employé** dans au moins une règle. | Un paramètre mort : il sera maintenu, documenté, versionné, pour rien — et un jour quelqu'un le changera en croyant agir |
| `C-05` | **Toute valeur littérale dans une règle est justifiée** : soit c'est un paramètre `P-xx`, soit une constante mathématique nommée, soit sa présence est expliquée sur place. | Une valeur magique. Personne ne saura jamais s'il faut la changer ni qui a le droit de le faire |
| `C-06` | **Toute grandeur déclare sa famille de type, sa précision et sa plage** ; toute grandeur dimensionnée déclare en outre sa dimension, son **unité pivot** et son **unité d'usage**. | La cause la plus fréquente d'écarts, et la plus tardive à se découvrir |
| `C-30` | **Tout symbole structuré déclare le type de ses éléments et l'étendue de chaque axe**, ainsi que la signification de chaque indice. | Une étendue non bornée interdit de dimensionner et ouvre un risque d'épuisement ; un indice non défini rend une transposition invisible |
| `C-31` | **Toute entrée d'un calcul devant restituer une précision déclare son incertitude, et les corrélations entre entrées sont explicites.** | Traiter comme indépendantes deux grandeurs issues du même capteur **sous-estime** l'incertitude — l'erreur va toujours dans le sens optimiste |
| `C-35` | **Toute grandeur consommée par une étape est une entrée du contrat, un paramètre, ou le produit d'une étape antérieure.** | Une grandeur qui sort de nulle part : le développeur devra inventer d'où elle vient |
| `C-37` | **Aucune grandeur de portée interne n'apparaît dans un contrat, dans une sortie, dans le catalogue des données, ni dans la chaîne inter-étapes.** | Une grandeur interne exposée est une fuite d'implémentation dans le contrat : elle sera consommée, puis on ne pourra plus la changer |
| `C-36` | **Toute grandeur produite par une étape est consommée plus loin ou déclarée en sortie.** | Un produit mort : du calcul fait pour rien, ou une sortie oubliée au contrat |
| `C-32` | **Un résultat n'est pas restitué avec plus de chiffres significatifs que son incertitude n'en autorise.** | `106,017 km ± 3 km` est une faute d'expression qui donne une fausse confiance |
| `C-38` | **Tout identifiant est en ASCII strict et en `snake_case`** : ni accent, ni symbole grec, ni majuscule. | Deux identifiants visuellement identiques peuvent différer par leur normalisation Unicode — `C-01` ne les rapproche pas et `--tracer` en perd un, sans que personne ne comprenne pourquoi |
| `C-07` | **Aucune grandeur ne change d'unité** entre sa déclaration et son emploi sans conversion explicite. | Erreur d'un facteur 3,6 — ou 13 quand elle passe au carré |

> **Deux défauts opposés, à ne pas confondre — c'est la confusion la plus courante à la
> lecture du catalogue.**
>
> | | **Déclaré, mais jamais employé** | **Employé, mais jamais déclaré** |
> |---|---|---|
> | Le nom | **mort** — entrée morte (`C-01`), sortie morte (`C-02`), paramètre mort (`C-04`) | **fantôme** (`C-03`) |
> | Ce qu'on lit | le contrat annonce quelque chose dont aucune règle ne se sert | une règle se sert de quelque chose que le contrat n'annonce pas |
> | Ce que ça révèle | soit une règle manque, soit la donnée est inutile — on fait fournir pour rien | le développeur devra **inventer d'où elle vient** |
> | Où se trouve le défaut | plutôt dans les **règles**, qui sont incomplètes | plutôt dans le **contrat**, qui est incomplet |
>
> Moyen mnémotechnique : **mort = déclaré sans emploi ; fantôme = employé sans
> déclaration.** Le premier est un poids inutile, le second est un trou.

> **Lire un avertissement `C-01` ou `C-02`.** Le contrôle cherche le nom exact du champ
> dans les règles. Deux causes possibles à un signalement, et elles appellent des
> traitements opposés : soit le champ est réellement inutile — on le retire ; soit les
> règles le désignent **par un autre nom** que le contrat — et c'est alors le vocabulaire
> qui diverge, ce qui est un défaut plus sérieux. Un avertissement se tranche à la main,
> jamais en le faisant taire.

## Complétude logique

| Id | Règle | Ce qu'un échec révèle |
|---|---|---|
| `C-08` | **Tout `IF` a son `ELSE`.** | Le comportement du cas non traité est décidé par le compilateur |
| `C-09` | **Toute table de décision est complète et non redondante** : chaque combinaison des entrées apparaît dans exactement une ligne. | Incomplète, elle laisse un trou ; redondante, elle se contredit |
| `C-10` | **Tout arrondi précise** le nombre de décimales, le sens, et l'étape à laquelle il intervient. | Deux implémentations correctes divergent d'un centime |
| `C-11` | **Tout superlatif a une règle de départage** : « le plus petit », « le meilleur », « le premier » — et s'il y en a deux ? | Le résultat dépend de l'ordre de lecture des données, donc de l'implémentation |
| `C-12` | **Toute entrée facultative a un comportement défini en cas d'absence**, et le sens du repli est explicite. | Le développeur choisit le repli, presque toujours le plus commode plutôt que le plus prudent |
| `C-13` | **Tout calcul itératif déclare** son critère d'arrêt, son nombre maximal d'itérations, **et ce qui est rapporté en cas de non-convergence**. | Le troisième point est presque toujours oublié : le programme renvoie son dernier itéré comme s'il avait convergé |

| `C-40` | **Aucune contrainte d'implémentation ne figure dans la spécification** : nom de bibliothèque, structure de données, stockage, cache, ordre d'exécution imposé sans justification métier. | Le métier sort de son mandat et ferme des solutions meilleures. Se repère mieux à la lecture — humaine ou par IA — qu'avec une liste de mots-clés |
| `C-41` | **Aucune formulation qui repousse la décision** : « etc. », « le cas échéant », « en général », « si nécessaire », « on gère les cas particuliers ». | Chacune est une question que quelqu'un devra trancher plus tard, sans mandat |

## Traçabilité

| Id | Règle | Ce qu'un échec révèle |
|---|---|---|
| `C-14` | **Chaque règle `RG-xxx` figure dans la table de couverture.** | Une règle non testée : soit elle est inutile, soit il manque un cas |
| `C-39` | **Le jeu d'essai porte la provenance et la validation de ses résultats attendus** : d'où ils viennent, comment ils ont été examinés, par qui, quand, pour quelle version. | Sans cette trace, on ne sait pas dans deux ans si un chiffre est un engagement opposable ou un vestige — et la non-régression ne protège plus rien |
| `C-15` | **Chaque `CT-xx` cité existe**, et chaque `CT-xx` défini est cité. | La table de couverture ment |
| `C-16` | **Chaque cas d'erreur `E-xxx` est couvert** par un cas de test, ou explicitement noté non couvert avec une question ouverte. | Le comportement de rejet n'est pas vérifié |
| `C-17` | **Aucun identifiant n'est défini deux fois** dans un document. | Deux règles portent le même nom : les références deviennent ambiguës |
| `C-18` | **Tout terme métier employé figure au glossaire**, et tout terme du glossaire est employé quelque part. | Vocabulaire flottant d'un côté, terme mort de l'autre |

## Version et statut

| Id | Règle | Ce qu'un échec révèle |
|---|---|---|
| `C-19` | **Le numéro de version de l'en-tête correspond à la dernière ligne de l'historique.** | On ne sait pas ce qui a été validé |
| `C-20` | **Une ligne d'historique déclarant un impact sur les résultats porte un incrément majeur.** | Un changement de comportement livré comme un correctif : les consommateurs ne sont pas prévenus |
| `C-21` | **Toute question ouverte a un décideur nommé et une échéance.** | « La direction tranchera » ne tranchera jamais |
| `C-22` | **Un document au statut « Acceptée » n'a aucune question ouverte bloquante.** | On a validé ce qui n'était pas décidé |
| `C-23` | **L'en-tête nomme le glossaire et le jeu de paramètres de référence, avec leur version.** | Le résultat n'est pas rejouable, même si la spécification l'est |
| `C-24` | **Toute ligne d'historique déclarant un impact renvoie à une notice de changement `N-<version>` présente dans le document.** | On sait qu'il y a eu un changement, on ne sait ni pourquoi, ni qui est touché, ni ce que les contrats sont devenus |
| `C-25` | **Chaque nom employé dans une règle apparaît sous la même orthographe dans le contrat**, chemin complet compris. | Le lien entre contrat et règles cesse d'être vérifiable, et le vocabulaire diverge |
| `C-26` | **Toute exigence de réalisation `EX-xxx` porte un énoncé, une source, un valideur et un moyen de vérification.** | Sans source, c'est une préférence d'équipe déguisée en exigence. Sans vérification, c'est décoratif |
| `C-28` | **Tout objet identifié figure dans l'annexe « Identités » avec un UUID valide.** | Sans identité durable, un renommage ou un déplacement casse toutes les références |
| `C-29` | **Aucun UUID n'est porté par deux objets**, dans tout le dépôt. | Deux objets confondus : la traçabilité devient fausse au lieu d'être absente, ce qui est pire |
| `C-27` | **Une passe de relecture par une IA déclarée porte un opérateur nommé**, la date, et le nombre de constats retenus et écartés. | Sans opérateur nommé, personne n'assume : on a produit un fusible, pas une validation ([guide 5](../guides/5-VALIDER.md)) |

## Contrôles humains

Ce que seul un lecteur peut juger. Une IA peut les pré-instruire et signaler des candidats ;
**elle ne les tranche pas**.

| Id | Question | Comment on répond |
|---|---|---|
| `H-01` | **L'algorithme est-il compréhensible ?** Un développeur qui ne connaît pas le domaine peut-il l'implémenter sans deviner ? | **On compte.** « J'aurais deviné trois fois, ici, ici et ici » est un verdict utilisable. « Ça me paraît clair » n'en est pas un |
| `H-02` | **La logique des entrées et sorties tient-elle ?** Les sorties découlent-elles réellement des entrées déclarées ? Manque-t-il une donnée pour produire ce qui est promis ? | On prend chaque sortie et on remonte la chaîne jusqu'aux entrées |
| `H-03` | **Les règles décrivent-elles un résultat, ou un parcours ?** | Toute boucle explicite non justifiée est un choix d'implémentation qui s'est glissé dans la spécification |
| `H-04` | **Les hypothèses du modèle sont-elles explicites ?** | Ce que le modèle ne représente pas doit être écrit, sinon on le découvrira en production |
| `H-05` | **Le cas riche du jeu d'essai est-il suivable de bout en bout**, à la calculatrice, sans rien deviner ? | On le refait |
| `H-06` | **Les règles sont-elles celles que l'organisation veut appliquer ?** | Seul le valideur métier peut répondre. C'est la seule question que ni script ni IA n'approchera jamais |
| `H-07` | **L'immutabilité est-elle respectée ?** Un nom valorisé est-il réaffecté plus loin, au lieu qu'un nouveau nom porte la transformation ? | Se lit, ne se mécanise pas : deux branches d'un même `IF` qui affectent le même nom sont légitimes, une réaffectation séquentielle ne l'est pas. **C'est le contrôle où la passe IA apporte le plus** — un modèle lit correctement la structure des branches, un script non |

---

## Ajouter une règle

Ce catalogue est un bien commun. On y ajoute une règle **quand son absence a causé un
incident réel**, jamais par précaution. Une règle ajoutée doit préciser :

1. son identifiant, dans la bonne famille ;
2. sa formulation, en une phrase, sans ambiguïté ;
3. **ce qu'un échec révèle** — sans quoi personne ne saura quoi faire du verdict ;
4. si elle est mécanisable, et si oui, son implémentation dans `Verifier.java`.

Une règle `C-xx` qui n'est pas mécanisable n'en est pas une : c'est un `H-xx`.
