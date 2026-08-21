# Règles de contrôle d'une spécification

*Écrites une fois, pour trois lecteurs : un relecteur humain qui les applique, un script
qui les exécute ([`verifier.py`](verifier.py)), une IA à qui on les donne en consigne
([`PROMPT-RELECTURE-IA.md`](PROMPT-RELECTURE-IA.md)).*

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
| `C-01` | **Toute entrée déclarée est employée** dans au moins une règle. | Une entrée morte : soit une règle manque, soit l'entrée est inutile — et on demande à l'appelant de fournir une donnée pour rien |
| `C-02` | **Toute sortie déclarée est produite** par au moins une règle. | Une promesse non tenue : le contrat annonce un résultat que rien ne calcule |
| `C-03` | **Toute grandeur employée dans une règle est déclarée** : entrée, sortie, paramètre, ou variable locale introduite par `SOIT`. | Un paramètre fantôme circule dans l'algorithme. Le développeur devra inventer d'où il vient |
| `C-04` | **Tout paramètre `P-xx` déclaré est employé** dans au moins une règle. | Un paramètre mort : il sera maintenu, documenté, versionné, pour rien — et un jour quelqu'un le changera en croyant agir |
| `C-05` | **Toute valeur littérale dans une règle est justifiée** : soit c'est un paramètre `P-xx`, soit une constante mathématique nommée, soit sa présence est expliquée sur place. | Une valeur magique. Personne ne saura jamais s'il faut la changer ni qui a le droit de le faire |
| `C-06` | **Toute grandeur porte son unité**, sa devise ou son domaine de validité. | La cause la plus fréquente d'écarts, et la plus tardive à se découvrir |
| `C-07` | **Aucune grandeur ne change d'unité** entre sa déclaration et son emploi sans conversion explicite. | Erreur d'un facteur 3,6 — ou 13 quand elle passe au carré |

> **Lire un avertissement `C-01` ou `C-02`.** Le contrôle cherche le nom exact du champ
> dans les règles. Deux causes possibles à un signalement, et elles appellent des
> traitements opposés : soit le champ est réellement inutile — on le retire ; soit les
> règles le désignent **par un autre nom** que le contrat — et c'est alors le vocabulaire
> qui diverge, ce qui est un défaut plus sérieux. Un avertissement se tranche à la main,
> jamais en le faisant taire.

## Complétude logique

| Id | Règle | Ce qu'un échec révèle |
|---|---|---|
| `C-08` | **Tout `SI` a son `SINON`.** | Le comportement du cas non traité est décidé par le compilateur |
| `C-09` | **Toute table de décision est complète et non redondante** : chaque combinaison des entrées apparaît dans exactement une ligne. | Incomplète, elle laisse un trou ; redondante, elle se contredit |
| `C-10` | **Tout arrondi précise** le nombre de décimales, le sens, et l'étape à laquelle il intervient. | Deux implémentations correctes divergent d'un centime |
| `C-11` | **Tout superlatif a une règle de départage** : « le plus petit », « le meilleur », « le premier » — et s'il y en a deux ? | Le résultat dépend de l'ordre de lecture des données, donc de l'implémentation |
| `C-12` | **Toute entrée facultative a un comportement défini en cas d'absence**, et le sens du repli est explicite. | Le développeur choisit le repli, presque toujours le plus commode plutôt que le plus prudent |
| `C-13` | **Tout calcul itératif déclare** son critère d'arrêt, son nombre maximal d'itérations, **et ce qui est rapporté en cas de non-convergence**. | Le troisième point est presque toujours oublié : le programme renvoie son dernier itéré comme s'il avait convergé |

## Traçabilité

| Id | Règle | Ce qu'un échec révèle |
|---|---|---|
| `C-14` | **Chaque règle `RG-xxx` figure dans la table de couverture.** | Une règle non testée : soit elle est inutile, soit il manque un cas |
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
| `C-26` | **Toute exigence de réalisation `EX-xxx` porte un énoncé, une source, un propriétaire et un moyen de vérification.** | Sans source, c'est une préférence d'équipe déguisée en exigence. Sans vérification, c'est décoratif |
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
| `H-06` | **Les règles sont-elles celles que l'organisation veut appliquer ?** | Seul le propriétaire de la règle peut répondre. C'est la seule question que ni script ni IA n'approchera jamais |
| `H-07` | **L'immutabilité est-elle respectée ?** Un nom valorisé est-il réaffecté plus loin, au lieu qu'un nouveau nom porte la transformation ? | Se lit, ne se mécanise pas : deux branches d'un même `SI` qui affectent le même nom sont légitimes, une réaffectation séquentielle ne l'est pas. **C'est le contrôle où la passe IA apporte le plus** — un modèle lit correctement la structure des branches, un script non |

---

## Ajouter une règle

Ce catalogue est un bien commun. On y ajoute une règle **quand son absence a causé un
incident réel**, jamais par précaution. Une règle ajoutée doit préciser :

1. son identifiant, dans la bonne famille ;
2. sa formulation, en une phrase, sans ambiguïté ;
3. **ce qu'un échec révèle** — sans quoi personne ne saura quoi faire du verdict ;
4. si elle est mécanisable, et si oui, son implémentation dans `verifier.py`.

Une règle `C-xx` qui n'est pas mécanisable n'en est pas une : c'est un `H-xx`.
