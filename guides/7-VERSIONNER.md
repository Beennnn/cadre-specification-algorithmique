# Guide 7 — Versionner

*Transverse. Gouverne la vie d'une spécification après sa première validation.*

---

## Les trois questions auxquelles seul le versionnement répond

| Question | Qui la pose | Sans versionnement |
|---|---|---|
| **Quelle règle s'appliquait le 14 mars ?** | un auditeur, un client en litige | on ne sait pas |
| **Quelle version a produit ce chiffre ?** | la comptabilité, une analyse d'incident | on ne sait pas |
| **Ce changement casse-t-il quelque chose ?** | l'équipe qui s'apprête à modifier | on découvre en production |

## Ce qui se versionne — et ce n'est pas que la spécification

> **Une spécification seule ne suffit pas à rejouer un résultat.**

Il faut le **quadruplet** :

| Artefact | Pourquoi il est indispensable |
|---|---|
| **La spécification** | les règles |
| **Le glossaire** | le sens des termes. Changer le sens d'un mot change toutes les règles qui l'emploient **sans qu'aucune ne bouge** — c'est le changement le plus invisible qui soit |
| **Le jeu de paramètres** | les valeurs. Un seuil passé de 3 à 4 change les résultats sans toucher une règle |
| **Les contrats des fonctions dont elle dépend** | si `FN-002` change son contrat, `FN-001` change de comportement sans avoir été modifiée |

Un résultat n'est rejouable que s'il **enregistre les quatre versions**. C'est la raison
pour laquelle chaque spécification nomme, dans son en-tête, le glossaire et le jeu de
paramètres auxquels elle se réfère — avec leur version.

## L'incrément, en termes métier

| Incrément | Quand | Conséquence |
|---|---|---|
| **Majeur** — `2.0.0` | **Un résultat change** pour au moins un cas | Nouvelle date d'effet, information des consommateurs, décision de rejeu, **validation rejouée** |
| **Mineur** — `1.3.0` | Un cas nouveau est couvert, **sans changer** les résultats existants | Nouveaux cas de test, pas de rejeu |
| **Correctif** — `1.2.4` | Clarification de rédaction, **aucun** impact sur les résultats | Rien |

### Le test décisif, et il est mécanique

> **On rejoue le jeu d'essai de la version précédente sur la nouvelle version.
> Si un seul cas change de résultat attendu : c'est un majeur.**

Ce n'est pas une appréciation, c'est une exécution. Le jeu d'essai de la version N est
conservé ; il est rejoué contre la version N+1 ; le verdict tombe.

> « Ce n'est qu'une clarification » est la phrase à surveiller en revue. Neuf fois sur
> dix elle est vraie ; la dixième fois elle cache un changement de comportement — et le
> test ci-dessus la démasque en trente secondes.

Pour le **glossaire**, la même logique donne :

| Changement | Incrément |
|---|---|
| Ajouter un terme | mineur |
| Préciser une définition sans changer le sens | correctif |
| **Changer le sens** d'un terme | **majeur** |
| **Renommer** un terme | **majeur** — l'ancien nom devient un synonyme déprécié, daté |
| **Retirer** un terme | **majeur** — toute règle qui l'employait doit être reprise |

## Version et date d'effet sont deux axes indépendants

C'est la confusion la plus fréquente, et elle produit des rejeux faux.

| | Ce que ça date |
|---|---|
| **La version** | quand le **document** a changé |
| **La date d'effet** | à partir de quand la **règle** s'applique |

Une `2.0.0` publiée le 12 mars avec une date d'effet au 1ᵉʳ juin : pendant onze semaines,
la `1.x` fait foi pour les calculs, et la `2.0.0` existe, validée, en attente.

**Deux stratégies, et laquelle choisir :**

| Stratégie | Comment | Quand |
|---|---|---|
| **Une version par période d'effet** ⭐ | la `2.0.0` remplace la `1.x` à sa date d'effet ; on retrouve l'ancienne par son étiquette | par défaut — les changements de règles sont rares |
| **Des règles datées dans un même document** | chaque `RG-xxx` porte sa période de validité | uniquement pour les **paramètres**, qui changent souvent |

Mélanger les deux dans un même document le rend illisible en deux ans.

## Geler une version

| Mécanisme | Rôle |
|---|---|
| Le champ **Version** de l'en-tête | la source déclarative — c'est lui qui fait foi |
| Une **étiquette Git** `SPEC-NRG-001/v2.0.1` | le gel technique : elle rend le document retrouvable à l'octet près |
| Un **contrôle automatique** | vérifie que l'en-tête, la dernière ligne de l'historique et l'étiquette coïncident (`C-19`) |

**Le numéro de version ne va jamais dans le nom du fichier.** `SPEC-NRG-001-v2.md`
crée un second document qui divergera. Le fichier est unique ; c'est Git qui porte
l'histoire.

## Le code déclare la version qu'il implémente

Deux obligations, et la seconde est celle qu'on oublie :

1. Le code **déclare** la version de spécification qu'il implémente — une constante, un
   manifeste, peu importe la forme.
2. **Tout résultat persisté embarque les versions** qui l'ont produit : spécification,
   glossaire, jeu de paramètres, date qui fait foi ([guide 3](3-DONNEES.md)).

Sans le second point, l'audit est impossible et le rejeu est faux : le programme relira
sagement les paramètres **courants** et produira un chiffre différent de l'original.

## Les dépendances entre spécifications

Une spécification déclare les fonctions dont elle dépend **et la version de leur contrat**.

**Règle de propagation :** un majeur chez une dépendance oblige à **réexaminer**, pas
forcément à changer. Le réexamen est tracé — même s'il conclut « aucun impact », il doit
laisser une ligne dans l'historique. C'est cette ligne qui, dans trois ans, évitera de
refaire l'analyse.

## Ce qu'on ne supprime jamais

| | Pourquoi |
|---|---|
| Une **règle abrogée** | reste, barrée et datée : un rejeu antérieur s'y réfère |
| Un **identifiant** | jamais réutilisé, même après suppression |
| Une **question fermée** | reste avec sa décision et sa date : c'est ce qui évite de rouvrir le débat |
| Un **terme retiré** du glossaire | devient un synonyme déprécié, daté |

Conséquence assumée : **le document ne rétrécit jamais.** C'est acceptable parce qu'on lit
la version courante et que l'historique ne sert qu'à l'audit — à condition que les
éléments périmés soient visuellement marqués, jamais mélangés aux vivants.

## Anti-patterns

| Anti-pattern | À quoi on le reconnaît | Pourquoi c'est grave |
|---|---|---|
| **La version qui ne bouge jamais** | `1.0.0` depuis deux ans sur un domaine actif | le document a cessé d'être tenu, donc d'être vrai |
| **« Ce n'est qu'une clarification »** | un correctif qui change un résultat | les consommateurs ne sont pas prévenus, le rejeu n'est pas fait |
| **Le document versionné, les paramètres non** | l'historique ne mentionne jamais de changement de valeur | on ne peut pas rejouer, alors qu'on croit pouvoir |
| **La date d'effet absente** | seule la date de publication figure | on ne sait pas quelle règle s'appliquait quand |
| **Le rejeu qui relit le courant** | aucune capture des paramètres à la date de l'événement | les chiffres de l'an dernier ne se retrouvent pas — **le défaut le plus fréquent** |
| **La version dans le nom du fichier** | `SPEC-…-v2.md` | deux documents, qui divergeront |
| **Le glossaire non versionné** | il n'a pas d'en-tête de version | le changement le plus invisible devient intraçable |

---

## Voir sur le fil rouge

L'historique de [`SPEC-NRG-001`](../exemples/fil-rouge/5-SPEC-NRG-001-autonomie.md)
montre les trois incréments dans l'ordre :

- `1.0.0` → **`2.0.0`** : l'arrondi du point d'autonomie passe vers le bas. Le résultat
  change, fût-ce d'un mètre → **majeur**, sans discussion.
- `2.0.0` → **`2.0.1`** : deux règles reformulées après le retrait d'un terme au
  glossaire. Aucun résultat ne change → **correctif**.

Et le [glossaire du fil rouge](../exemples/fil-rouge/2-GLOSSAIRE.md) est passé en
**`2.0.0`** parce qu'un terme en a été **retiré** — ce qui a forcé la reprise de deux
règles de la spécification. Un glossaire qui passe en majeur, c'est une spécification qui
bouge.
